## React Native Android Library Device Information
Tjis project will give you device unique ID , current position, wifi connected information, available wifi connection.
## Getting started
 
  

## Installing it as a library in your main project
	
	npm i rn-device-information
		or
	yarn add rn-device-information
	
 1. Link the library:
    * Add the following to `android/settings.gradle`:
        ```
        include ':rn-device-information'
        project(':rn-device-information').projectDir = new File(settingsDir, '../node_modules/rn-device-information/android')
        ```

    * Add the following to `android/app/build.gradle`:
        ```xml
        ...

        dependencies {
            ...
            compile project(':rn-device-information')
        }
        ```
    * Add the following to `android/app/src/main/java/**/MainApplication.java`:
        ```java
        package com.motivation;

        import  package com.example.mylibrary.Package;

        public class MainApplication extends Application implements ReactApplication {

            @Override
            protected List<ReactPackage> getPackages() {
                return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new Package()    
                );
            }
        }
        ```
4. Simply `import/require` it by the name defined in your library's `package.json`:

    ```javascript
    import nativeCalls from 'rn-device-information'; 

EXAMPLE HERE****************************
    ```
    import React, { useState, useEffect } from 'react';
    import {
        ScrollView,
        View,
        Text,
        PermissionsAndroid
    } from 'react-native';


    import nativeCalls from 'rn-device-information';


    const App = () => {


    const [devicename, setDevieName] = useState("");
    const [devicenMacAddress, setDevieMacAddress] = useState("");
    const [latlong, setLatLong] = useState("");
    const [connectedTo, setConnectedTo] = useState("");
    const [connectedDeviceInfo, setConnectedDeviceInfo] = useState('');
    const [availableConnection, setAvailableConnection] = useState([]);


    const persmission = async () => {
        try {
        // permission to access location to set wifi connection
        const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION)
            .then(res => {
            if (res === "granted") {
                console.log(" permission!");
                nativeCalls.deviceNativeCall.loadWifiList(async (wifiStringList) => {
                var wifiArray = await JSON.parse(wifiStringList);
                setAvailableConnection(wifiArray);

                },
                (error) => {
                    console.log(error);
                })

                nativeCalls.wifiConnection.fetch().then(connection => {
                if (connection !== undefined) {
                    setConnectedTo(connection)
                }

                }).catch(err => {
                console.log("TCL: App -> err", err)

                })
            } else {
                console.log("You will not able to retrieve wifi available networks list");
            }
            });
        } catch (err) {
        console.warn(err)
        }
    }

    useEffect(() => {
        persmission();
        nativeCalls.deviceNativeCall.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000,
        })
        .then(async location => {
            setLatLong({ location })
        }).catch(error => {
            const { code, message } = error;
            console.warn(code, message);
        });
        nativeCalls.deviceNativeCall.getDeviceName((err, name) => {
        setDevieName(name);
        });
            <!-- specific to android -->
        nativeCalls.deviceNativeCall.getMacAddress((err, deviceMacAddress) => {
        setDevieMacAddress(deviceMacAddress);
        });

            <!-- specific to android -->
        nativeCalls.deviceNativeCall.getClientList((err, clientList) => {
        setConnectedDeviceInfo(clientList);

        });

    }, []);

    return (
        <ScrollView>
        <View style={{ margin: 10 }}>

            <Text>
            {"Device name: " + devicename + "\n"}
            </Text>

            <Text>
            {"Device mac address: " + devicenMacAddress + "\n"}
            </Text>
            {latlong.location !== undefined
            ? <Text>
                {"latitude: " + latlong.location.latitude + " \nlongitude: " + latlong.location.longitude + " \ntime: " + latlong.location.time + "\n"}
            </Text>
            : <Text>Wait</Text>}

            {connectedTo !== "" && connectedTo.type === 'wifi'
            ? <View>
                <Text>{"connected type:   " + connectedTo.type}</Text>
                <Text>{"isConnected:   " + connectedTo.isConnected}</Text>
                <Text>{"isInternetReachable:   " + connectedTo.isInternetReachable}</Text>
                <Text>{"isWifiEnabled:   " + connectedTo.isWifiEnabled}</Text>

                <Text>{"bssid:   " + connectedTo.details.bssid}</Text>
                <Text>{"frequency:   " + connectedTo.details.frequency}</Text>
                <Text>{"ipAddress:   " + connectedTo.details.ipAddress}</Text>
                <Text>{"isConnectionExpensive:   " + connectedTo.details.isConnectionExpensive}</Text>
                <Text>{"ssid:   " + connectedTo.details.ssid}</Text>
                <Text>{"strength:   " + connectedTo.details.strength}</Text>
                <Text>{"subnet:   " + connectedTo.details.bssid + "\n"}</Text>
            </View>

            : connectedTo !== "" && connectedTo.type === 'cellular'
                ? <View>
                <Text>{"connected to:   " + connectedTo.type}</Text>

                <Text>{"carrier:   " + connectedTo.details.carrier}</Text>
                <Text>{"cellularGeneration:   " + connectedTo.details.cellularGeneration}</Text>
                <Text>{"isConnectionExpensive:   " + connectedTo.details.isConnectionExpensive + "\n"}</Text>


                <Text>{"List of device connected to mobile hotspot:\n"}</Text>
                <Text style={{ fontSize: 12 }}>{connectedDeviceInfo}</Text>
                </View>
                : <Text>no connection found</Text>}

            <Text >{"Available wifi Connection:\n"}</Text>
            {availableConnection.length > 0
            ? availableConnection.map((list, i) => {
                return (
                <View key={i}>
                    <Text>{"BSSID:  " + list.BSSID}</Text>
                    <Text>{"SSID:   " + list.SSID}</Text>
                    <Text>{"capabilities:   " + list.capabilities}</Text>
                    <Text>{"frequency:  " + list.frequency}</Text>
                    <Text>{"level:  " + list.level}</Text>
                    <Text>{"timestamp:  " + list.timestamp + "\n"}</Text>
                </View>
                )
            })

            : <Text>No connection available</Text>}

        </View>
        </ScrollView>

        );
    };

    export default App;

 ```