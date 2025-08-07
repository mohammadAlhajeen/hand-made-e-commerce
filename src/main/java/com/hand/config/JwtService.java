package com.hand.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private  String secretKey;
    private final int expireTime = 1000 * 60 * 60 * 10;

    public Claims getAllClaimsFromToken(String jwtToken) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            return claims;
        } catch (Exception e) {

            throw new RuntimeException("Invalid token", e);
        }

    }

    public String getUsernameFromToken(String jwtToken) {

        return extractClaims(jwtToken, Claims::getSubject);

    }

    public <T> T extractClaims(String jwtToken, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(jwtToken);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        try {
            final String username = userDetails.getUsername();
            return username.equals(getUsernameFromToken(jwtToken)) && !isExpired(jwtToken);
        } catch (Exception e) {
            throw new RuntimeException("Token is invalid or expired", e);
        }
    }

    private boolean isExpired(String jwtToken) {
        return getExpiredDate(jwtToken).before(new Date());
    }

    private Date getExpiredDate(String jwtToken) {
        return extractClaims(jwtToken, Claims::getExpiration);
    }

    public String jwtGenerator(UserDetails userDetails) {
        return jwtGenerator(new HashMap<>(), userDetails);
    }

    public String jwtGenerator(Map<String, Object> claims, UserDetails userDetails) {

        return Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public SecretKey getSigningKey() {
        byte[] signingKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(signingKey);
    }
}
