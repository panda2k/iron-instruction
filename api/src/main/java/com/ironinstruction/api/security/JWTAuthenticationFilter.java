package com.ironinstruction.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.responses.JWTResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private CustomAuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(CustomAuthenticationManager authenticationManager, AuthenticationFailureHandler failureHandler) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/api/v1/login"); // for some reason this doesn't work as intended
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // object mapper will map the request to the LoginRequest object
            // this is the same concept as defining @RequestBody I think
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
        // create JWT token with 15 minute expiration time
        Date expirationTime = new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME);
        String token = JWT.create()
                .withSubject((String) authResult.getPrincipal())
                .withExpiresAt(expirationTime)
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));

        JWTResponse body = new JWTResponse(token, expirationTime.getTime(), "");
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.getWriter().flush(); // commits the response written above
    }
}
