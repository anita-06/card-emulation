package com.reactnativecardemulation.model;

import com.reactnativecardemulation.util.HexUtil;

import java.util.Arrays;

public class TagAndValue {
    private byte[] tag;
    private byte[] value;

    public TagAndValue(final byte[] tag, final byte[] value) {
        this.tag = tag;
        this.value = value;
    }

    public byte[] getTag() {
        return tag;
    }

    public byte[] getValue() {
        return value;
    }

    public byte[] getTLVBytes() {
        int length = value.length;
        byte[] tlvBytes = Arrays.copyOf(tag, tag.length + 1 + value.length);
        tlvBytes[tag.length] = (byte) length;
        System.arraycopy(value, 0, tlvBytes, tag.length + 1, value.length);
        return tlvBytes;
    }

    @Override
    public String toString() {
        return "tag: " + HexUtil.byteArrayToHex(tag) + " value: " + HexUtil.byteArrayToHex(value);
    }

}
