package com.ironinstruction.api;

import com.ironinstruction.api.utils.PasswordManager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SpringBootTest
public class PasswordManagerTests {
    private static final PasswordManager manager = new PasswordManager();
    
    @Test
    public void testHash() throws Exception {
        String password = "hello";

        try {
            String saltOne = manager.createSalt();
            String saltTwo = manager.createSalt();
            String hashOne = manager.hash(password, saltOne);
            String hashTwo = manager.hash(password, saltTwo);
            assertTrue(!hashOne.equals(hashTwo));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testValidation() throws Exception {
        String password = "hello";

        try {
            String salt = manager.createSalt();
            String hash = manager.hash(password, salt);
            String duplicateHash = manager.hash(password, salt);
            assertTrue(hash.equals(duplicateHash));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testInvalidation() throws Exception {
        String password = "hello";
        String incorrectPassword = "nothello";
        try {
            String salt = manager.createSalt();
            String hash = manager.hash(password, salt);
            String incorrectHash = manager.hash(incorrectPassword, salt);
            assertTrue(!incorrectHash.equals(hash));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
