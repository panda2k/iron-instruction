package com.ironinstruction.api.refreshtoken;

import java.util.NoSuchElementException;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironinstruction.api.errors.InvalidToken;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken getRefreshtoken(String token) throws NoSuchElementException {
        return refreshTokenRepository.findById(token).get();
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteById(token);
        return;
    }

    public void saveRefreshToken(RefreshToken token) {
        refreshTokenRepository.save(token);
        return;
    }
    
    // not sure if having a boolean function that only returns true or throws error
    // is a good pattern
    public DecodedJWT verifyRefreshToken(String token) throws InvalidToken {
        DecodedJWT decodedToken = TokenManager.verifyJWT(token, TokenType.REFRESH);
        // error is thrown if the token has already been cycled 
        try {
            getRefreshtoken(token);
        } catch (NoSuchElementException e) {
            throw new InvalidToken("Invalid token");
        }

        return decodedToken;
    }
}

