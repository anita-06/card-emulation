package com.reactnativecardemulation.apdu;

import android.annotation.TargetApi;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.reactnativecardemulation.CardEmulationModule;
import com.reactnativecardemulation.model.TagAndValue;
import com.reactnativecardemulation.util.HexUtil;
import com.reactnativecardemulation.util.TagValueUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Terminology
 * APDU - Proximity Payment System Environment
 * PPSE - Proximity Payment System Environment
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ApduService extends HostApduService {

  boolean isProcessing = false;

  public static CountDownLatch latch;

  public static String pan;
  public static String expiry;
  public static String serviceCode;
  public static String customerName;

  public static String cvvT1;
  public static String cvvT2;
  public static String atc;

  public static CardEmulationModule cardEmulationModule;

  private boolean isPaymentSuccessful = false;

  @Override
  public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

    isPaymentSuccessful = false;

    if (!isProcessing) {
      startPaymentActivityInBackgroundThread();
      isProcessing = true;
    }

    String inboundApduDescription = "Unknown Command";
    byte[] responseApdu = ApduCommands.ISO7816_UNKNOWN_ERROR_RESPONSE;

    try {
      if (Arrays.equals(ApduCommands.PPSE_APDU_SELECT, commandApdu)) {

        cardEmulationModule.sendEvent("PaymentStarted", null);

        inboundApduDescription = "Received PPSE select: ";
        responseApdu = ApduCommands.PPSE_APDU_SELECT_RESP;
      } else if (Arrays.equals(ApduCommands.MASTERCARD_MSD_SELECT, commandApdu)) {
        inboundApduDescription = "Received Visa-MSD select: ";
        responseApdu = ApduCommands.MASTERCARD_MSD_SELECT_RESPONSE;
      } else if (ApduCommands.isGpoCommand(commandApdu)) {
        inboundApduDescription = "Received GPO (get processing options): ";

        cardEmulationModule.sendEvent("IncrementATC", null);
        latchWait();

        responseApdu = ApduCommands.GPO_COMMAND_RESPONSE;
      } else if (Arrays.equals(ApduCommands.READ_REC_COMMAND, commandApdu)) {
        inboundApduDescription = "Received READ REC: ";

        cardEmulationModule.sendEvent("ReadRecord", null);

        latchWait();

        //generate read record response
        responseApdu = getReadRecordResponse();

      } else if (ApduCommands.isComputeCryptographicChecksumCommand(commandApdu)) {
        inboundApduDescription = "Received Compute cryptographic checksum command";

        byte[] unpredictedNumberBytes = Arrays.copyOfRange(commandApdu, 5, 9);
        String unpredictedNumber = HexUtil.byteArrayToHex(unpredictedNumberBytes);

        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("unpredictedNumber", unpredictedNumber);

        cardEmulationModule.sendEvent("ComputeCryptoChecksum", writableMap);

        latchWait();

        responseApdu = getComputeCryptographicChecksum();
      }
    } catch (Exception e) {
      e.printStackTrace();
      inboundApduDescription = "Received Unhandled APDU: ";
      responseApdu = ApduCommands.ISO7816_UNKNOWN_ERROR_RESPONSE;
    }

    String inputHex = HexUtil.byteArrayToHex(commandApdu);
    String outputHex = HexUtil.byteArrayToHex(responseApdu);

    System.out.println(inboundApduDescription);

    System.out.println("Input Hex\n" + inputHex);
    System.out.println("Output Hex\n" + outputHex);

    return responseApdu;
  }

  private void latchWait() {
    latch = new CountDownLatch(1);
    try {
      latch.await(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private byte[] getComputeCryptographicChecksum() {
    byte[] cvc3T1Bytes = HexUtil.hexToByteArray(cvvT1);
    byte[] cvc3T2Bytes = HexUtil.hexToByteArray(cvvT2);
    byte[] atcBytes = HexUtil.hexToByteArray(atc);

    TagAndValue cv3T1 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x60}, cvc3T1Bytes);
    TagAndValue cv3T2 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x61}, cvc3T2Bytes);
    TagAndValue atc = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x36}, atcBytes);

    TagAndValue cryptoTag = new TagAndValue(new byte[]{0x77}, TagValueUtils.merge(cv3T1, cv3T2, atc));
    byte[] tlvBytes = cryptoTag.getTLVBytes();

    ByteBuffer bb = ByteBuffer.allocate(tlvBytes.length + 2);// Track 2 data length
    bb.put(tlvBytes);           // Track 2 equivalent data
    bb.put((byte) 0x90);                            // SW1
    bb.put((byte) 0x00);

    isPaymentSuccessful = true;

    return bb.array();
  }

  @Override
  public void onDeactivated(int reason) {
    isProcessing = false;

    if (isPaymentSuccessful) {
      cardEmulationModule.sendEvent("PaymentSuccessful", null);
    } else
      cardEmulationModule.sendEvent("PaymentFailed", null);
  }


  private void startPaymentActivityInBackgroundThread() {
   /* new Runnable() {
      @Override
      public void run() {

        //Start Payment Activity
        Intent intent = new Intent(getApplicationContext(), PayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getApplicationContext().startActivity(intent);

      }
    }.run();*/
  }

  public byte[] getReadRecordResponse() {
    TagAndValue mgStripeVersionNum = new TagAndValue(
      new byte[]{(byte) 0x9F, (byte) 0x6C}, new byte[]{(byte) 0x00, (byte) 0x01});

    // Track 1 data
    String panString = pan;
    String expiryString = expiry;
    String serviceCodeString = serviceCode;
    String nameString = customerName;

    System.out.println(panString);
    System.out.println(expiryString);
    System.out.println(serviceCodeString);
    System.out.println(nameString);

    String track1String = "B" + panString + "^" + nameString + "^" + expiryString + serviceCodeString + "330003330002222200011110";
    byte[] track1Bytes = track1String.getBytes();

    TagAndValue track1 = new TagAndValue(
      new byte[]{(byte) 0x56},
      track1Bytes
    );

    TagAndValue natcT1 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x64}, new byte[]{(byte) 0x03});
    TagAndValue natcT2 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x67}, new byte[]{(byte) 0x03});
    TagAndValue pcvc3T1 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x62}, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x00, (byte) 0x00});
    TagAndValue punatcT1 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x63}, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xE0, (byte) 0xE0});
    TagAndValue pcvc3T2 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x65}, new byte[]{(byte) 0x00, (byte) 0x0E});
    TagAndValue punatcT2 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x66}, new byte[]{(byte) 0x0E, (byte) 0x70});


    // Track 2
    String track2String = panString + "D" + expiryString + serviceCodeString + "9000990000000F";

    byte[] track2Bytes = HexUtil.hexToByteArray(track2String);

    TagAndValue track2 = new TagAndValue(new byte[]{(byte) 0x9F, (byte) 0x6B}, track2Bytes);

    byte[] tlvBytes = TagValueUtils.merge(
      mgStripeVersionNum,
      track1,
      natcT1,
      pcvc3T1,
      punatcT1,
      pcvc3T2,
      punatcT2,
      track2,
      natcT2
    );

    ByteBuffer bb = ByteBuffer.allocate(tlvBytes.length + 4);
    bb.put((byte) 0x70);                            // EMV Record Template tag
    bb.put((byte) (tlvBytes.length));        // Length with track 2 tag
    bb.put(tlvBytes);           // Track 2 equivalent data
    bb.put((byte) 0x90);                            // SW1
    bb.put((byte) 0x00);                            // SW2

    return bb.array();
  }
}
