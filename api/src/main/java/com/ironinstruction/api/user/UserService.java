package com.ironinstruction.api.user;

import com.ironinstruction.api.utils.PasswordManager;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordManager = new PasswordManager();
    }

    public User findUserByEmail(String email) throws NoSuchElementException {
        return userRepository.findByEmail(email).get();
    }

    public User createUser(String name, String email, String password, UserType userType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String salt = passwordManager.createSalt();
        final String hash = passwordManager.hash(password, salt);

        if (userType == UserType.ATHLETE) {
            return userRepository.insert(new Athlete(name, email, hash, salt));
        } else {
            return userRepository.insert(new Coach(name, email, hash, salt));
        }
    }
}
