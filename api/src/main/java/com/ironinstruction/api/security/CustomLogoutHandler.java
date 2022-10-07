package com.ironinstruction.api.security;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class CustomLogoutHandler implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        // don't set acccess token maxage to let client differentiate between a deleted cookie from logout and an expired access

        // create JWT refresh token 
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/api/v1/refreshtoken");
        refreshTokenCookie.setMaxAge(0);

        // send the final response
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
}
