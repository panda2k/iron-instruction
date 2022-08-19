package com.ironinstruction.api.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironinstruction.api.errors.InvalidToken;
import com.ironinstruction.api.security.SecurityConstants;

public class TokenManager {
    public TokenManager() { }

    private static Algorithm getAlgorithm(TokenType tokenType) {
        return Algorithm.HMAC512((tokenType == TokenType.ACCESS ? SecurityConstants.ACCESS_SECRET : SecurityConstants.REFRESH_SECRET).getBytes());
    }

    public static String generateJWT(String body, TokenType tokenType)  {
        Calendar calendar = new GregorianCalendar(); 
        calendar.setTime(new Date(System.currentTimeMillis()));
        // super super edge case but if a refresh token is exchanged 
        // instantly after its creation, an identical token will be returned
        // therefore, add a random amount of minutes (1-15) to the expiration
        calendar.add(Calendar.MINUTE, (int) Math.ceil(Math.random() * 15) + (tokenType == TokenType.ACCESS ? SecurityConstants.ACCESS_EXPIRATION_TIME_MINUTES : SecurityConstants.REFRESH_EXPIRATION_TIME_MINUTES));
        return JWT.create().withSubject(body).withExpiresAt(calendar.getTime()).sign(getAlgorithm(tokenType));
    }

    public static String generateJWT(String body, TokenType tokenType, Date expiration)  {
        return JWT.create().withSubject(body).withExpiresAt(expiration).sign(getAlgorithm(tokenType));
    }

    public static DecodedJWT verifyJWT (String token, TokenType tokenType) throws InvalidToken {
        try {
            return JWT.require(getAlgorithm(tokenType)).build().verify(token);
        } catch (JWTDecodeException | SignatureVerificationException e) {
            throw new InvalidToken("Invalid token");
        } catch (TokenExpiredException e) {
            throw new InvalidToken("Token expired");
        }
    }
}
