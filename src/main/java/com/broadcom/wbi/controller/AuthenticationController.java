package com.broadcom.wbi.controller;

import com.broadcom.wbi.security.jwt.JwtTokenHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationController {

    @Value("${cookie.jwt}")
    private String TOKEN_COOKIE;

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {

        String authToken = JwtTokenHandler.getToken(request);
        if (authToken != null && JwtTokenHandler.canTokenBeRefreshed(authToken)) {
            // TODO check user password last update
            String refreshedToken = JwtTokenHandler.refreshToken(authToken);
            Cookie authCookie = new Cookie(TOKEN_COOKIE, (refreshedToken));
            authCookie.setPath("/");
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(28 * 24 * 60 * 60); //30 days
            // Add cookie to response
            response.addCookie(authCookie);

            return ResponseEntity.ok(refreshedToken);
        } else {
            return ResponseEntity.accepted().body(authToken);
        }
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Principal user(Principal user) {
        return user;
    }
}