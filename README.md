# react-native-card-emulation

Host card emulation through react native

## Installation

```sh
npm install react-native-card-emulation
```

## Usage

```js
import CardEmulation from "react-native-card-emulation";

// ...

```

### 1. Initialize the module

```js

componentDidMount() {
  // ...
  CardEmulation.initialize();
}

```

### 2. Add event listeners

```js

componentDidMount() {
    // ...

    CardEmulation.initialize();

    const eventEmitter = new NativeEventEmitter(CardEmulation);
    eventEmitter.addListener('ReadRecord', () => {
      // Call api to get record info
      // Api should return: PAN, expity, serviceCode, User name

      console.log('Read record event received on js')

      const pan = "1212121212121212";
      const expiry = "2104";
      const serviceCode = "101";
      const name = "BASTOLA/SANT";


      CardEmulation.setReadRecordData(pan, expiry, serviceCode, name);
    });

    eventEmitter.addListener('ComputeCryptoChecksum', () => {
      // Call api to compute cryptograhic checksum
      // Api should return cvv3 for Track1, cvv3 for Track2 and ATC

      console.log('Compute cryptographic checksum event received on js')

      const cvv3T1 = "FBC7";
      const cvv3T2 = "B892";
      const atc = "0001";


      CardEmulation.setCryptographicData(cvv3T1, cvv3T2, atc);
    });

    eventEmitter.addListener('PaymentStarted', () => {
      console.log('Payment Started');
    });


    eventEmitter.addListener('PaymentSuccessful', () => {
      console.log('Payment Successful');
    });

    eventEmitter.addListener('PaymentFailed', () => {
      console.log('Payment Failed');
    });

    eventEmitter.addListener('IncrementATC', () => {
      // Call an Api to increment atc

      console.log('Increment ATC');

      CardEmulation.atcIncremented();
    });
  }

```

## Android setup

### 1. Add this on AndroidManifest.xml
```xml

<uses-permission android:name="android.permission.NFC" />

<uses-feature
  android:name="android.hardware.nfc"
  android:required="true" />

<application>

// ...
  <service
    android:name="com.reactnativecardemulation.apdu.ApduService"
    android:exported="true"
    android:permission="android.permission.BIND_NFC_SERVICE">
    <intent-filter>
      <action android:name="android.nfc.cardemulation.action.HOST_APDU_SERVICE" />

      <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>

    <meta-data
      android:name="android.nfc.cardemulation.host_apdu_service"
      android:resource="@xml/apdu_config" />
  </service>

</application>

```

### 2. Create a new file apdu_config.xml on android/app/src/main/res/xml and paste this code.

```xml
<?xml version="1.0" encoding="utf-8"?>
<host-apdu-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:apduServiceBanner="@mipmap/ic_launcher"
    android:description="@string/apdu_description"
    android:requireDeviceUnlock="false">

    <aid-group
        android:category="payment"
        android:description="@string/aid_group_description">
        <aid-filter
            android:name="325041592E5359532E4444463031"
            android:description="@string/aid_ppse_description" />
        <aid-filter
            android:name="A0000000041010"
            android:description="@string/aid_mastercard_description" />
    </aid-group>

</host-apdu-service>

```

### 3. Add this on android/app/src/main/res/values/strings.xml

```xml
<!-- HCE Configuration -->
<string name="apdu_description">Contactless Pay</string>
<string name="aid_group_description">Contactless Pay is the best.</string>
<string name="aid_ppse_description">Proximity Payment System Environment (PPSE)</string>
<string name="aid_mastercard_description">MasterCard</string>
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
