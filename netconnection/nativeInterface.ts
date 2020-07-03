
import {NativeEventEmitter} from 'react-native';
import RNCNetInfo from './nativeModule';

// Produce an error if we don't have the native module
if (!RNCNetInfo) {
  throw new Error("something went wrong");
}

/**
 * We export the native interface in this way to give easy shared access to it between the
 * JavaScript code and the tests
 */
let nativeEventEmitter: NativeEventEmitter | null = null;
export default {
  ...RNCNetInfo,
  get eventEmitter(): NativeEventEmitter {
    if (!nativeEventEmitter) {
      /// @ts-ignore
      nativeEventEmitter = new NativeEventEmitter(RNCNetInfo);
    }
    /// @ts-ignore
    return nativeEventEmitter;
  },
};
