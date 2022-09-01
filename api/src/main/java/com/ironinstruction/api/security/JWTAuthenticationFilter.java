package com.ironinstruction.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.refreshtoken.RefreshToken;
import com.ironinstruction.api.refreshtoken.RefreshTokenService;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private CustomAuthenticationManager authenticationManager;
    private RefreshTokenService refreshTokenService; 

    public JWTAuthenticationFilter(CustomAuthenticationManager authenticationManager, AuthenticationFailureHandler failureHandler, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        // doesn't lock the filter to the specified url
        setFilterProcessesUrl("/api/v1/login"); // for some reason this doesn't work as intended, or I misunderstand its use
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // create JWT access token 
        String accessToken = TokenManager.generateJWT((String) authResult.getPrincipal(), TokenType.ACCESS);
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        
        // create JWT refresh token 
        RefreshToken refreshToken = new RefreshToken(TokenManager.generateJWT((String) authResult.getPrincipal(), TokenType.REFRESH));
        refreshTokenService.saveRefreshToken(refreshToken);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setHttpOnly(true);

        // send the final response
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        response.getWriter().flush(); // commits the response written above
    }
}
