package com.reactnativecardemulation;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativecardemulation.apdu.ApduService;

public class CardEmulationModule extends ReactContextBaseJavaModule {

  CardEmulationModule(ReactApplicationContext context) {
    super(context);
    ApduService.cardEmulationModule = this;
  }

  @Override
  public String getName() {
    return "CardEmulation";
  }

  public void sendEvent(
    String eventName,
    @Nullable WritableMap params) {
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @ReactMethod
  public void initialize() {
  }

  @ReactMethod
  public void setReadRecordData(String pan, String expiry, String serviceCode, String name) {
    ApduService.pan = pan;
    ApduService.expiry = expiry;
    ApduService.serviceCode = serviceCode;
    ApduService.customerName = name;
    ApduService.latch.countDown();
  }

  @ReactMethod
  public void setCryptographicData(String cvvT1, String cvvT2, String atc) {
    ApduService.cvvT1 = cvvT1;
    ApduService.cvvT2 = cvvT2;
    ApduService.atc = atc;
    ApduService.latch.countDown();
  }

  @ReactMethod
  public void atcIncremented() {
    ApduService.latch.countDown();
  }
}
