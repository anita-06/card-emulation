package com.reactnativecardemulation.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.reactnativecardemulation.apdu.ApduService;

/**
 * Created by handstandtech on 7/24/15.
 */
public class DefaultPaymentAppUtil {

  public static final int REQUEST_CODE_DEFAULT_PAYMENT_APP = 1;

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static void ensureSetAsDefaultPaymentApp(Activity context) {
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    CardEmulation cardEmulation = CardEmulation.getInstance(nfcAdapter);
    ComponentName componentName = new ComponentName(context, ApduService.class);
    boolean isDefault = cardEmulation.isDefaultServiceForCategory(componentName, CardEmulation.CATEGORY_PAYMENT);

    if (!isDefault) {
      Intent intent = new Intent(CardEmulation.ACTION_CHANGE_DEFAULT);
      intent.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT);
      intent.putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, componentName);
      context.startActivityForResult(intent, REQUEST_CODE_DEFAULT_PAYMENT_APP);
    }
  }
}
