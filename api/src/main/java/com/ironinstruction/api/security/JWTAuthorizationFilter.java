package com.ironinstruction.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.ironinstruction.api.errors.AccessDenied;
import com.ironinstruction.api.errors.InvalidToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private AuthenticationFailureHandler failureHandler;
    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, AuthenticationFailureHandler failureHandler) {
        super(authenticationManager);
        this.failureHandler = failureHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return (request.getServletPath().equals(SecurityConstants.SIGN_UP_URL) || request.getServletPath().equals(SecurityConstants.LOGIN_URL)) && request.getMethod().equals("POST");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            this.failureHandler.onAuthenticationFailure(request, response, new InvalidToken("No token supplied"));
            return;
        }
        try {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } catch (AuthenticationException e) {
            this.failureHandler.onAuthenticationFailure(request, response, e);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);

        if (token != null) {
            String userEmail;
            try {
                userEmail = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()))
                        .build()
                        .verify(token.replace(SecurityConstants.TOKEN_PREFIX, "").replaceAll(" ", ""))
                        .getSubject();
            } catch (JWTDecodeException | SignatureVerificationException e) {
                throw new InvalidToken("Invalid token");
            } catch (TokenExpiredException e) {
                throw new InvalidToken("Token expired");
            }

            if (userEmail != null) {
                // if they are requesting a user's specific info, make sure they are that user
                String requestUrl = request.getRequestURL().toString();

                if (requestUrl.contains("@")) {
                    Pattern emailRegex = Pattern.compile("[a-zA-Z0-9-_.]+@[a-zA-Z0-9-_.]+");
                    Matcher matchEmail = emailRegex.matcher(requestUrl);
                    if (matchEmail.find() && !userEmail.equals(matchEmail.group())) {
                        throw new AccessDenied("Account doesn't have permission to access requested resource");
                    }
                }
                return new UsernamePasswordAuthenticationToken(userEmail, null);
            }
            throw new InvalidToken("Invalid token"); // don't think this ever gets reached
        }
        throw new InvalidToken("No token supplied");
    }
}
