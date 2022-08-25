package com.ironinstruction.api.refreshtoken;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironinstruction.api.errors.ErrorResponse;
import com.ironinstruction.api.errors.InvalidToken;
import com.ironinstruction.api.requests.RefreshTokenRequest;
import com.ironinstruction.api.responses.JWTResponse;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public JWTResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null) {
            throw new InvalidToken("Missing token");
        }
        DecodedJWT token = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        
        // kind of a useless check imo because verifyRefreshToken() only returns true or throws error
        refreshTokenService.deleteRefreshToken(request.getRefreshToken()); 
        String refreshToken = TokenManager.generateJWT(token.getSubject(), TokenType.REFRESH);
        String accessToken = TokenManager.generateJWT(token.getSubject(), TokenType.ACCESS);

        refreshTokenService.saveRefreshToken(new RefreshToken(refreshToken));

        return new JWTResponse(accessToken, refreshToken);
    }
}
