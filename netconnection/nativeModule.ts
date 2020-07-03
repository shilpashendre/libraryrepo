import {NativeModules} from 'react-native';
import {NetInfoNativeModule} from './privateTypes';

const RNCNetInfo: NetInfoNativeModule = NativeModules.DeviceInformation;

export default RNCNetInfo;
