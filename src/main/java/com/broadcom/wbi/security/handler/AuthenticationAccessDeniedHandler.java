package com.broadcom.wbi.security.handler;

import com.broadcom.wbi.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException, ServletException {
        SecurityUtils.sendError(response, exception, HttpServletResponse.SC_FORBIDDEN, "No Authority");
//        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
//        redirectStrategy.sendRedirect(request, response, "/login");
    }
}