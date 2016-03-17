package com.mdt.sso.view;

import javax.xml.bind.DatatypeConverter;


public class PasswordEncoderUtil {
    public PasswordEncoderUtil() {
        super();
    }

    public static String encodeStr(String str) {
        return new String(DatatypeConverter.printBase64Binary(str.getBytes()));
    }

    public static String decodeStr(String str) {
        return new String(DatatypeConverter.parseBase64Binary(str));
    }

    public static void main(String[] args) {
        String str = "SANTOSH";
        String encodedStr = encodeStr(str);
        System.out.println("Encode Str-->" + encodedStr);
        String decodedStr = decodeStr(encodedStr);
        System.out.println("Decoded Str-->" + decodedStr);
    }
}
