package com.ironinstruction.api.user;

import com.ironinstruction.api.utils.PasswordManager;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    public User cleanseUser(User user) {
        user.setPasswordHash(null);
        user.setPasswordSalt(null);

        return user;
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordManager = new PasswordManager();
    }

    public User findByEmail(String email) throws NoSuchElementException {
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

    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
        return;
    }

    public Athlete updateAthleteInfoByEmail(String email, String weightClass, float weight, Date dob, float squatMax, float benchMax, float deadliftMax, float height) throws NoSuchElementException {
        Athlete user = (Athlete) findByEmail(email); 
         
        user.setWeightClass(weightClass);
        user.setWeight(weight);
        user.setDob(dob);
        user.setSquatMax(squatMax);
        user.setBenchMax(benchMax);
        user.setDeadliftMax(deadliftMax);
        user.setHeight(height);

        return userRepository.save(user);
    }
}
