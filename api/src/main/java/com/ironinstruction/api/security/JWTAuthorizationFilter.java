package com.ironinstruction.api.security;

import com.ironinstruction.api.errors.AccessDenied;
import com.ironinstruction.api.errors.InvalidToken;
import com.ironinstruction.api.errors.ResourceNotFound;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;
import com.ironinstruction.api.program.ProgramService;
import com.ironinstruction.api.program.Program;
import com.ironinstruction.api.user.UserType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private AuthenticationFailureHandler failureHandler;
    private ProgramService programService;

    public JWTAuthorizationFilter(ProgramService programService, AuthenticationManager authenticationManager, AuthenticationFailureHandler failureHandler) {
        super(authenticationManager);
        this.failureHandler = failureHandler;
        this.programService = programService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // for some reason getServletPath doesn't work in the test cases so 
        // get path from request uri instead
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return ((path.equals(SecurityConstants.REFRESH_URL)) || path.equals(SecurityConstants.SIGN_UP_URL) || path.equals(SecurityConstants.LOGIN_URL)) && request.getMethod().equals("POST");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request, response);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } catch (AuthenticationException e) {
            this.failureHandler.onAuthenticationFailure(request, response, e);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String token;
        try {
            token = (WebUtils.getCookie(request, "accessToken")).getValue();
        } catch (NullPointerException e) {
            throw new InvalidToken("No token supplied");
        }

        if (token.length() != 0) {
            // token manager throws appropriate errors if failed decode
            String subject[] = TokenManager.verifyJWT(token, TokenType.ACCESS).getSubject().split(";");
            String userEmail = subject[0];
            UserType userType = UserType.valueOf(subject[1]);

            if (userEmail != null) {
                // if they are requesting a user's specific info, make sure they are that user
                String requestUrl = request.getRequestURI().substring(request.getContextPath().length());
                // no need to check specific security on me urls
                if (!requestUrl.contains("me")) {
                    if (requestUrl.contains("programs")) {
                        if (requestUrl.equals("/api/v1/programs")) {
                            if (userType != UserType.COACH) {
                                throw new AccessDenied("Only coach accounts can create a program");
                            }
                        } else {
                            Pattern programIdRegex = Pattern.compile("(?<=\\/programs\\/)[a-z0-9]+");
                            Matcher matchProgramId = programIdRegex.matcher(requestUrl);
                            matchProgramId.find();
                            String programId = matchProgramId.group();
                            Program program;
                            try {
                                program = programService.findById(programId);
                            } catch (ResourceNotFound e) { // kind of a hacky solution
                                throw new AccessDenied("Invalid resource requested");
                            }
                            // VALID COACH EMAIL -> NOT PATCH REQUEST -> GOOD
                            // INVALID COACH EMAIL -> VALID ATHLETE -> NON-POST-METHOD -> GOOD
                            if (!program.getCoachEmail().equals(userEmail)) {
                                // ALL POST REQUESTS ARE DONE BY COACHES
                                // ATHLETES DO PUTS
                                if (program.getAthleteEmail().equals(userEmail)) {
                                    if (request.getMethod().equals("POST")) {
                                        throw new AccessDenied("Only coaches can create new resources");
                                    } else if (request.getMethod().equals("DELETE")) {
                                        throw new AccessDenied("Only coaches can delete resources");
                                    } else if (request.getMethod().equals("PUT")) {
                                        throw new AccessDenied("Only coaches can put new resources");
                                    }
                                } else {
                                    throw new AccessDenied("Account doesn't have permission to access requested resource");
                                }
                            } else if (request.getMethod().equals("PATCH")) {
                                throw new AccessDenied("Coaches can't use patch requests");
                            }
                        }
                    } 
                } else if (requestUrl.contains("athlete")) {
                    if (userType != UserType.ATHLETE) {
                        throw new AccessDenied("User must be an athlete");
                    }
                }
                return new UsernamePasswordAuthenticationToken(userEmail, null);
            }
            throw new InvalidToken("Invalid token"); // don't think this ever gets reached
        }
        throw new InvalidToken("No token supplied");
    }
}
