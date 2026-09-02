package com.example.knowledgesphere.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (!StringUtils.hasText(header)
                || !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);

            return;

        }

        String token = header.substring(7);

        String username = jwtService.extractUsername(token);

        if (username != null
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            UserDetails user =

                    userDetailsService

                            .loadUserByUsername(username);

            if (jwtService.isTokenValid(token, user)) {

                UsernamePasswordAuthenticationToken authentication =

                        new UsernamePasswordAuthenticationToken(

                                user,

                                null,

                                user.getAuthorities()

                        );

                authentication.setDetails(

                        new WebAuthenticationDetailsSource()

                                .buildDetails(request)

                );

                SecurityContextHolder

                        .getContext()

                        .setAuthentication(authentication);

            }

        }

        filterChain.doFilter(request, response);

    }

}