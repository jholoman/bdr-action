package com.cloudera.manager.client.util;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.*;
/**
 * Created by jholoman on 5/24/14.
 */
public class AESencryption {

    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'a', '6', 'T', 'z', '!', 'r', 'B',
                    '4', 'q', 'X', 'r', '2', 'C', 'L', 'r', 'a'};

    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());

        return new BASE64Encoder().encode(encVal);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);

        return new String(decValue);
    }

    private static Key generateKey() {

        return new SecretKeySpec(keyValue, ALGO);
    }
}
