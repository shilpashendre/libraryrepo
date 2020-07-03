package com.example.mylibrary;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mylibrary.SettingsUtil;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Module extends ReactContextBaseJavaModule implements AmazonFireDeviceConnectivityPoller.ConnectivityChangedCallback {

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    private LocationManager locationManager;
    private GetLocation getLocation;
    WifiManager wifi;
    private final ConnectivityReceiver mConnectivityReceiver;
    private final AmazonFireDeviceConnectivityPoller mAmazonConnectivityChecker;

    public Module(ReactApplicationContext reactContext) {
        super(reactContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mConnectivityReceiver = new NetworkCallbackConnectivityReceiver(reactContext);
        } else {
            mConnectivityReceiver = new BroadcastReceiverConnectivityReceiver(reactContext);
        }
        mAmazonConnectivityChecker = new AmazonFireDeviceConnectivityPoller(reactContext, this);
        wifi = (WifiManager)reactContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            locationManager = (LocationManager) reactContext.getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "DeviceInformation";
    }

    public WifiInfo getWifiInfo() {
        WifiManager manager = (WifiManager) getReactApplicationContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            return manager.getConnectionInfo();
        }
        return null;
    }

    // Custom function that we are going to export to JS
    @ReactMethod
    public void getDeviceName(Callback cb) {
        try {
            cb.invoke(null, android.os.Build.MODEL);
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String getMacAddressSync() {
        WifiInfo wifiInfo = getWifiInfo();
        String macAddress = "";
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
        }

        String permission = "android.permission.INTERNET";
        int res = getReactApplicationContext().checkCallingOrSelfPermission(permission);

        if (res == PackageManager.PERMISSION_GRANTED) {
            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (!nif.getName().equalsIgnoreCase("wlan0"))
                        continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        macAddress = "";
                    } else {

                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X:", b));
                        }

                        if (res1.length() > 0) {
                            res1.deleteCharAt(res1.length() - 1);
                        }

                        macAddress = res1.toString();
                    }
                }
            } catch (Exception ex) {
                // do nothing
            }
        }

        return macAddress;
    }

    @ReactMethod
    public void getMacAddress(Callback cb) {
        try {
            cb.invoke(null, getMacAddressSync());
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    @ReactMethod
    public void getInfo(Callback cb) {
        try {
            cb.invoke(null, getWifiInfo());
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    @ReactMethod
    public void openWifiSettings(final Promise primise) {
        try {
            SettingsUtil.openWifiSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openCelularSettings(final Promise primise) {
        try {
            SettingsUtil.openCelularSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openGpsSettings(final Promise primise) {
        try {
            SettingsUtil.openGpsSettings(getReactApplicationContext());
            primise.resolve(null);
        } catch (Throwable ex) {
            primise.reject(ex);
        }
    }

    @ReactMethod
    public void openAppSettings(final Promise promise) {
        try {
            SettingsUtil.openAppSettings(getReactApplicationContext());
            promise.resolve(null);
        } catch (Throwable ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, Promise promise) {
        if (getLocation != null) {
            getLocation.cancel();
        }
        getLocation = new GetLocation(locationManager);
        getLocation.get(options, promise);
    }

    @ReactMethod
    public String getClients() {
        BufferedReader br = null;
        String data = "";
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line != null) {
                    data = line;
                }
            }
        } catch (Exception e) {

        }
        return data;
    }

    @ReactMethod
    public void getClientList(Callback cb) {
        try {
            cb.invoke(null, getClients());
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    // Method to load wifi list into string via Callback. Returns a stringified
    // JSONArray
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @ReactMethod
    public void loadWifiList(Callback successCallback, Callback errorCallback) {
        try {
            List<ScanResult> results = wifi.getScanResults();
            JSONArray wifiArray = new JSONArray();

            for (ScanResult result : results) {
                JSONObject wifiObject = new JSONObject();
                if (!result.SSID.equals("")) {
                    try {
                        wifiObject.put("SSID", result.SSID);
                        wifiObject.put("BSSID", result.BSSID);
                        wifiObject.put("capabilities", result.capabilities);
                        wifiObject.put("frequency", result.frequency);
                        wifiObject.put("level", result.level);
                        wifiObject.put("timestamp", result.timestamp);
                        // Other fields not added
                        // wifiObject.put("operatorFriendlyName", result.operatorFriendlyName);
                        // wifiObject.put("venueName", result.venueName);
                        // wifiObject.put("centerFreq0", result.centerFreq0);
                        // wifiObject.put("centerFreq1", result.centerFreq1);
                        // wifiObject.put("channelWidth", result.channelWidth);
                    } catch (JSONException e) {
                        errorCallback.invoke(e.getMessage());
                    }
                    wifiArray.put(wifiObject);
                }
            }
            successCallback.invoke(wifiArray.toString());
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        }
    }

    @Override
    public void initialize() {
        mConnectivityReceiver.register();
        mAmazonConnectivityChecker.register();
    }

    @Override
    public void onCatalystInstanceDestroy() {
        mAmazonConnectivityChecker.unregister();
        mConnectivityReceiver.unregister();
    }

    @ReactMethod
    public void getCurrentState(final String requestedInterface, final Promise promise) {
        mConnectivityReceiver.getCurrentState(requestedInterface, promise);
    }

    @Override
    public void onAmazonFireDeviceConnectivityChanged(boolean isConnected) {
        mConnectivityReceiver.setIsInternetReachableOverride(isConnected);
    }

}
