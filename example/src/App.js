import * as React from 'react';

import { StyleSheet, View, Text, NativeEventEmitter } from 'react-native';
import CardEmulation from 'react-native-card-emulation';


class App extends React.Component {
  componentDidMount() {
    this.initModule();

    const eventEmitter = new NativeEventEmitter(CardEmulation);
    eventEmitter.addListener('ReadRecord', () => {
      // Call api to get record info
      // Api should return: PAN, expity, serviceCode, User name

      this.setState({ status: 'Reading record.' });

      console.log('Read record event received on js')

      const pan = "1212121212121212";
      const expiry = "2104";
      const serviceCode = "101";
      const name = "BASTOLA/SANT";


      CardEmulation.setReadRecordData(pan, expiry, serviceCode, name);
    });

    eventEmitter.addListener('ComputeCryptoChecksum', (unpredictedNumber) => {
      // Call api to compute cryptograhic checksum
      // Api should return cvv for Track1, cvv for Track2 and ATC
      this.setState({ status: 'Computing cryptographic checksum' });

      console.log('Compute cryptographic checksum event received on js');
      console.log('Unpredicted number: ', unpredictedNumber);

      const cvv3T1 = "FBC7";
      const cvv3T2 = "B892";
      const atc = "0001";


      CardEmulation.setCryptographicData(cvv3T1, cvv3T2, atc);
    });

    eventEmitter.addListener('PaymentStarted', () => {
      console.log('Payment Started');

      this.setState({ status: 'Payment Started.' });
    });


    eventEmitter.addListener('PaymentSuccessful', () => {
      console.log('Payment Successful');
      this.setState({ status: 'Payment Successful' });
    });

    eventEmitter.addListener('PaymentFailed', () => {
      console.log('Payment Failed');
      this.setState({ status: 'Payment Failed.' });
    });

    eventEmitter.addListener('IncrementATC', () => {
      // Call an Api to increment atc

      console.log('Increment ATC');
      this.setState({ status: 'Increment ATC' });

      CardEmulation.atcIncremented();
    });
  }

  state = {
    status: 'Card is ready for payment'
  }

  initModule = () => {
    CardEmulation.initialize();
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={{ fontSize: 16 }}>{this.state.status}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  }
});


export default App;
