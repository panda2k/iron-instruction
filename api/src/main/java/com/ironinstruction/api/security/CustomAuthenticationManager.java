package com.ironinstruction.api.security;

import com.ironinstruction.api.user.User;
import com.ironinstruction.api.user.UserService;
import com.ironinstruction.api.utils.PasswordManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;

public class CustomAuthenticationManager implements AuthenticationManager {
    private UserService userService;
    private PasswordManager passwordManager;

    public CustomAuthenticationManager(UserService userService) {
        this.userService = userService;
        this.passwordManager = new PasswordManager();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            User user = userService.findUserByEmail((String) authentication.getPrincipal());
            try {
                if (user.getPasswordHash().equals(passwordManager.hash((String) authentication.getCredentials(), user.getPasswordSalt()))) {
                    return new UsernamePasswordAuthenticationToken(authentication.getPrincipal() + ";" + user.getUserType(), authentication.getCredentials());
                } else {
                    throw new BadCredentialsException("Incorrect password");
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchElementException e) {
            throw new BadCredentialsException("No user found with email '" + authentication.getPrincipal() + "'");
        }
    }
}
