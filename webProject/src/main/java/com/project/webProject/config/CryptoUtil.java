package com.project.webProject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Component
public class CryptoUtil{
    @Value("${encryption.secret-key}")
    private String secretKey;
    private static final String AES = "AES";

    public String encrypt(String data) throws Exception{
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(this.secretKey.getBytes(),AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedData) throws Exception{
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(this.secretKey.getBytes(),AES);
        cipher.init(Cipher.DECRYPT_MODE,secretKey);
        byte[] decrypted = cipher.doFinal(encryptedData.getBytes());
        return new String(decrypted);
    }

}
