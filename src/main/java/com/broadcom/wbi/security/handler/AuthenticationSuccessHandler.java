package com.broadcom.wbi.security.handler;

import com.broadcom.wbi.security.jwt.JwtTokenHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    ObjectMapper objectMapper;
    @Value("${cookie.jwt}")
    private String TOKEN_COOKIE;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        clearAuthenticationAttributes(request);

        String token = JwtTokenHandler.generateTokenFromUser(authentication.getName(), authentication.getAuthorities());

//        String token = JwtTokenHandler.generateTokenFromUser(userContext.getUsername(), userContext.getAuthorities());
//
        Cookie authCookie = new Cookie(TOKEN_COOKIE, token);
        authCookie.setPath("/");
        authCookie.setMaxAge(28 * 24 * 60 * 60); //30 days

//        System.out.println("authenticate success");
        response.addCookie(authCookie);
//        getRedirectStrategy().sendRedirect(request, response, "/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}