package com.broadcom.wbi.security.filter;

import com.broadcom.wbi.security.jwt.JwtTokenHandler;
import com.broadcom.wbi.security.model.JwtUser;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private RequestMatcher requestMatcher;

    public JwtTokenAuthenticationFilter(String path) {
        this.requestMatcher = new AntPathRequestMatcher(path);
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String token = JwtTokenHandler.getToken(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        Authentication auth = buildAuthenticationFromJwt(token, request);
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
        SecurityContextHolder.clearContext();
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);
        String token = authHeader.substring("Bearer ".length());
        return token;
    }

    private String checkAuthenticationAndValidity(String token) {
        JwtTokenHandler.assertValidSignature(token);

        Claims claims = JwtTokenHandler.getClaimsFromToken(token);

        JwtTokenHandler.assertNotExpired(claims);

        return token;
    }

    public Authentication buildAuthenticationFromJwt(String token, HttpServletRequest request) {

        try {
            Claims claims = JwtTokenHandler.getClaimsFromToken(token);
            //get username
            String username = claims.getSubject();
            //get roles

            Collection<? extends GrantedAuthority> authorities = JwtTokenHandler.parseRolesFromToken(claims);

            Date creationDate = JwtTokenHandler.parseCreationDateFromToken(claims);
            //get issue date
            JwtUser userDetails = new JwtUser(username, creationDate, authorities);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            return authentication;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }


        return null;
    }


}
