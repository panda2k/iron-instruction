package com.ironinstruction.api;

import com.ironinstruction.api.utils.PasswordManager;
import org.apache.tomcat.util.buf.HexUtils;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PasswordManagerTest {
    private static final PasswordManager manager = new PasswordManager();

    private static void testHash() {
        String password = "hello";

        try {
            String saltOne = manager.createSalt();
            String saltTwo = manager.createSalt();
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
            String salt = manager.createSalt();
            String hash = manager.hash(password, salt);
            String duplicateHash = manager.hash(password, salt);
            System.out.println("Passwords can be validated: " + hash.equals(duplicateHash));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static void testInvalidation() {
        String password = "hello";
        String incorrectPassword = "nothello";
        try {
            String salt = manager.createSalt();
            String hash = manager.hash(password, salt);
            String incorrectHash = manager.hash(incorrectPassword, salt);
            System.out.println("Passwords can be invalidated: " + !incorrectHash.equals(hash));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        testHash();
        testValidation();
        testInvalidation();
    }
}
