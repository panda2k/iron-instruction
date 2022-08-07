package com.ironinstruction.api.utils;

import org.apache.tomcat.util.buf.HexUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PasswordManager {
    private final int strength;
    private final int keyLength;

    public PasswordManager () {
        this.strength = 100000;
        this.keyLength = 128;
    }
    public PasswordManager(int strength, int keyLength) {
        this.strength = strength;
        this.keyLength = keyLength;
    }

    public String hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, this.strength,this.keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return HexUtils.toHexString(factory.generateSecret(spec).getEncoded()); // I don't see any point in returning the byte array for now
    }

    public byte[] createSalt(int size) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[size];
        random.nextBytes(salt);

        return salt;
    }

    public byte[] createSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }
}
