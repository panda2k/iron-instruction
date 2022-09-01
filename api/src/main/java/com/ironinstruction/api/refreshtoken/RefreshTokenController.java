package com.ironinstruction.api.refreshtoken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironinstruction.api.errors.ErrorResponse;
import com.ironinstruction.api.errors.InvalidToken;
import com.ironinstruction.api.security.SecurityConstants;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refreshtoken")
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    public RefreshTokenController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @ResponseBody 
    @ResponseStatus(value=HttpStatus.FORBIDDEN)
    @ExceptionHandler(InvalidToken.class)
    public ErrorResponse invalidToken (InvalidToken e) {
        return new ErrorResponse(e.getMessage());
    }

    @PostMapping()
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", defaultValue = "") String refreshToken, HttpServletResponse response) {
        if (refreshToken.isBlank()) {
            throw new InvalidToken("No token provided");
        }
        DecodedJWT token = refreshTokenService.verifyRefreshToken(refreshToken);
        
        refreshTokenService.deleteRefreshToken(refreshToken); 
        Cookie refreshTokenCookie = new Cookie("refreshToken", TokenManager.generateJWT(token.getSubject(), TokenType.REFRESH));
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(SecurityConstants.REFRESH_EXPIRATION_TIME_MINUTES * 60);
        refreshTokenCookie.setPath("/api/v1/refreshtoken");
        refreshTokenService.saveRefreshToken(new RefreshToken(refreshTokenCookie.getValue()));

        Cookie accessTokenCookie = new Cookie("accessToken", TokenManager.generateJWT(token.getSubject(), TokenType.ACCESS));
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setMaxAge(SecurityConstants.ACCESS_EXPIRATION_TIME_MINUTES * 60);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
