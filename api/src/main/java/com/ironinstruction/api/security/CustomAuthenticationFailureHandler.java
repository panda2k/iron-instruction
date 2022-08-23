package com.ironinstruction.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.errors.AccessDenied;
import com.ironinstruction.api.errors.ErrorResponse;
import com.ironinstruction.api.errors.InvalidEmail;
import com.ironinstruction.api.errors.InvalidToken;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");

        if (exception instanceof AccessDenied) {
            response.setStatus(403);
        } else if (exception instanceof InvalidEmail) { // could group the following two if statements together but will leave them separate for now
            response.setStatus(400);
        } else if (exception instanceof InvalidToken) {
            response.setStatus(400);
        } else if (exception instanceof BadCredentialsException) {
            response.setStatus(400);
        }

        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorResponse(exception.getMessage())));
        response.getWriter().flush();
    }
}
