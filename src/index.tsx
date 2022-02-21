import { NativeModules } from 'react-native';

type CardEmulationType = {
  setReadRecordData(pan: String, expiry: String, serviceCode: String, name: String): void;
  setCryptographicData(cvvT1: String, cvvT2: String, atc: String): void;
};

const { CardEmulation } = NativeModules;

export default CardEmulation as CardEmulationType;
