package com.broadcom.wbi.security.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
		 RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		 redirectStrategy.sendRedirect(request, response, "/login?error=true&message=Authentication failed");
//        super.onAuthenticationFailure(request, response, exception);
    }
}