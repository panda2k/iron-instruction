package com.ironinstruction.api;

import com.ironinstruction.api.utils.PasswordManager;
import org.apache.tomcat.util.buf.HexUtils;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PasswordManagerTest {
    private static PasswordManager manager = new PasswordManager();

    private static void testHash() {
        String password = "hello";

        try {
            byte[] saltOne = manager.createSalt();
            byte[] saltTwo = manager.createSalt();
            String hashOne = manager.hash(password, saltOne);
            String hashTwo = manager.hash(password, saltTwo);
            System.out.println("Two identical passwords are different hashes: " + !hashOne.equals(hashTwo));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static void testValidation() {
        String password = "hello";

        try {
            byte[] salt = manager.createSalt();
            String storedSalt = HexUtils.toHexString(salt); // simulate storing a salt as hex in db
            String hash = manager.hash(password, salt);
            String duplicateHash = manager.hash(password, DatatypeConverter.parseHexBinary(storedSalt)); // then parse db stored salt
            System.out.println("Passwords can be validated: " + hash.equals(duplicateHash));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        testHash();
        testValidation();
    }
}
