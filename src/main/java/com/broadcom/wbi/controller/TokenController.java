package com.broadcom.wbi.controller;


import com.broadcom.wbi.security.jwt.JwtTokenHandler;
import com.broadcom.wbi.security.model.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(value = "/api/token")
public class TokenController {

    private static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";

    private static final String HEADER_PREFIX = "Bearer ";

    @Value("${cookie.jwt}")
    private String TOKEN_COOKIE;


    private String tokenExtractFromHeader(String header) {
        if (StringUtils.isBlank(header)) {
            throw new IllegalArgumentException("Authorization header cannot be blank!");
        }
        if (header.length() < HEADER_PREFIX.length()) {
            throw new IllegalArgumentException("Invalid authorization header size.");
        }
        return header.substring(HEADER_PREFIX.length(), header.length());
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //get token from header
        String tokenPayload = tokenExtractFromHeader(request.getHeader(JWT_TOKEN_HEADER_PARAM));
        //check to see if access token is still valid

        //generate the new access token

        //send back and write to user cookie


        return null;


//        RefreshToken refreshToken = RefreshToken.create(rawToken, jwtSettings.getTokenSigningKey()).orElseThrow(() -> new InvalidJwtToken());
//
//        String jti = refreshToken.getJti();
//        if (!tokenVerifier.verify(jti)) {
//            throw new InvalidJwtToken();
//        }
//
//        String subject = refreshToken.getSubject();
//        User user = userService.getByUsername(subject).orElseThrow(() -> new UsernameNotFoundException("User not found: " + subject));
//
//        if (user.getRoles() == null) throw new InsufficientAuthenticationException("User has no roles assigned");
//        List<GrantedAuthority> authorities = user.getRoles().stream()
//                .map(authority -> new SimpleGrantedAuthority(authority.getRole().authority()))
//                .collect(Collectors.toList());
//
//        UserContext userContext = UserContext.create(user.getUsername(), authorities);
//
//        return tokenFactory.createAccessJwtToken(userContext);
    }

    @RequestMapping(value = "/parse", method = RequestMethod.GET)
    public ResponseEntity<?> getUserName(HttpServletRequest request, HttpServletResponse response) {
//
        String token = JwtTokenHandler.getToken(request);
        Claims claims = JwtTokenHandler.getClaimsFromToken(token);

        String username = JwtTokenHandler.parseUserFromToken(claims);

        System.out.println(JwtTokenHandler.parseRolesFromToken(claims));

        System.out.println(JwtTokenHandler.parseExpirationDateFromToken(claims));

//        System.out.println(auth.getAuthorities());
        // Return the token
//        return ResponseEntity.ok(auth.getPrincipal());
        return null;
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
        String authToken = JwtTokenHandler.getToken(request);
        if (authToken != null && JwtTokenHandler.canTokenBeRefreshed(authToken)) {
            // TODO check user password last update
            String authHeader = request.getHeader(AUTHORIZATION);
            String token = authHeader.substring("Bearer ".length());

            String refreshedToken = JwtTokenHandler.refreshToken(token);
            DateTime currentdt = new DateTime();

            Cookie authCookie = new Cookie(TOKEN_COOKIE, (refreshedToken));
            authCookie.setPath("/");
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(30 * 24 * 60 * 60);
            // Add cookie to response
            response.addCookie(authCookie);
            return ResponseEntity.ok(new AuthenticationResponse(token));
        } else {
            return ResponseEntity.accepted().body(null);
        }
    }
}
