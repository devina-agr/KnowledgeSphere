package com.example.knowledgesphere.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());

    }

    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()

                .subject(userDetails.getUsername())

                .issuedAt(new Date())

                .expiration(new Date(System.currentTimeMillis() + expiration))

                .signWith(getKey())

                .compact();

    }

    public String extractUsername(String token) {

        return extractClaims(token).getSubject();

    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        return extractUsername(token)

                .equals(userDetails.getUsername())

                && !isExpired(token);

    }

    private boolean isExpired(String token) {

        return extractClaims(token)

                .getExpiration()

                .before(new Date());

    }

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(getKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

    }

}