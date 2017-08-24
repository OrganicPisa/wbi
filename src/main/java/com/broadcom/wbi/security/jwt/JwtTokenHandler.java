package com.broadcom.wbi.security.jwt;


import com.broadcom.wbi.security.exception.JwtBadSignatureException;
import com.broadcom.wbi.security.exception.JwtExpirationException;
import io.jsonwebtoken.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public final class JwtTokenHandler {

    private final static String AUDIENCE_UNKNOWN = "unknown";
    private final static String AUDIENCE_WEB = "web";
    private final static String AUDIENCE_MOBILE = "mobile";
    private final static String AUDIENCE_TABLET = "tablet";
    private final static String ROLES_CLAIM = "roles";
    private final static String SCOPE_REFRESH = "ROLE_REFRESH_TOKEN";

    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    private static String secretKey;

    private static String issuer;

    public static String generateTokenFromUser(String username, Collection<? extends GrantedAuthority> roles) {
        return generateToken(username, AuthorityListToCommaSeparatedString(roles));
    }

    private static String generateToken(String username, String roles) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Cannot create Access Token without username");
        }
        if (StringUtils.isBlank(roles)) {
            throw new IllegalArgumentException("User Does not have any roles");
        }

        DateTime current = new DateTime();
        DateTime expiration = current.plusDays(30);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        claims.setSubject(username)
                .setExpiration(expiration.toDate())
                .setIssuedAt(current.toDate())
                .setIssuer(issuer)
                .setAudience(AUDIENCE_WEB);

        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .signWith(signatureAlgorithm, secretKey)
                .setClaims(claims)
                .compact();

        return token;
    }

    public static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public static String parseUserFromToken(Claims claims) {
        return claims.getSubject();
    }

    public static Collection<? extends GrantedAuthority> parseRolesFromToken(Claims claims) {
        Collection<? extends GrantedAuthority> authorities;
        String roles = claims.get(ROLES_CLAIM).toString();

        authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
        return authorities;
    }

    public static Date parseExpirationDateFromToken(Claims claims) {
        return claims.getExpiration();
    }

    public static Date parseCreationDateFromToken(Claims claims) {
        return claims.getIssuedAt();
    }

    public static void assertNotExpired(Claims claims) {
        try {
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                throw new JwtExpirationException("Token already Expired");
            }
        } catch (SignatureException ex) {
            throw new JwtBadSignatureException("Signature is not valid");
        }
    }

    public static void assertValidSignature(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
        } catch (SignatureException ex) {
            throw new JwtBadSignatureException("Signature is not valid");
        }
    }

    private static String AuthorityListToCommaSeparatedString(Collection<? extends GrantedAuthority> authorities) {
        Set<String> authoritiesAsSetOfString = AuthorityUtils.authorityListToSet(authorities);
        return StringUtils.join(authoritiesAsSetOfString, ", ");
    }

    public static Boolean canTokenBeRefreshed(String token) {
        Claims claims = getClaimsFromToken(token);
        try {
            final Date expirationDate = parseExpirationDateFromToken(claims);
            return expirationDate.compareTo(new Date()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String refreshToken(String token) {
        String refreshedToken;
        try {
            DateTime current = new DateTime();
            DateTime expiration = current.plusDays(30);
            Claims claims = getClaimsFromToken(token);

            claims
                    .setExpiration(expiration.toDate())
                    .setIssuedAt(current.toDate())
                    .setIssuer(issuer);

            refreshedToken = Jwts.builder()
                    .signWith(signatureAlgorithm, secretKey)
                    .setClaims(claims)
                    .compact();
            return refreshedToken;

        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    public static String getToken(HttpServletRequest request) {
        /**
         *  Getting the token from Cookie store
         */
        Cookie authCookie = getCookieValueByName(request, "AUTH-TOKEN");
        if (authCookie != null) {
            return authCookie.getValue();
        }
        /**
         *  Getting the token from Authentication header
         *  e.g Bearer your_token
         */
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length());
        }

        return null;
    }

    private static Cookie getCookieValueByName(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (int i = 0; i < request.getCookies().length; i++) {
            if (request.getCookies()[i].getName().equalsIgnoreCase(name)) {
                return request.getCookies()[i];
            }
        }
        return null;
    }

    @Value("${jwt.privateSecretKey}")
    public void setSecretKey(String secret) {
        secretKey = secret;
    }

    @Value("${jwt.issuer}")
    public void setIssuer(String iss) {
        issuer = iss;
    }

    @Value("${cookie.jwt}")
    public void setAuth_cookie(String auth_cookie) {
        auth_cookie = auth_cookie;
    }

}