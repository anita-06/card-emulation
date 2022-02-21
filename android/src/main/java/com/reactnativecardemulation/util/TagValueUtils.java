package com.reactnativecardemulation.util;

import com.reactnativecardemulation.model.TagAndValue;

import java.util.ArrayList;
import java.util.List;

public class TagValueUtils {

    public static byte[] merge(TagAndValue... tvArr) {
        List<Byte> bytesList = new ArrayList<>();
        for (TagAndValue tagAndValue : tvArr) {
            byte[] tlv = tagAndValue.getTLVBytes();
            for (byte b : tlv)
                bytesList.add(b);
        }

        byte[] result = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            result[i] = bytesList.get(i);
        }

        return result;
    }
}
