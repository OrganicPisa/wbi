package com.broadcom.wbi.security.handler;

import com.broadcom.wbi.security.SecurityUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationUnauthorizedEntryPointHandler implements AuthenticationEntryPoint {

    /**
     * Always returns a 401 error code to the client.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
//        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
//        redirectStrategy.sendRedirect(request, response, "/login");

        SecurityUtils.sendError(response, exception, HttpServletResponse.SC_UNAUTHORIZED, "No authorization");

    }
}