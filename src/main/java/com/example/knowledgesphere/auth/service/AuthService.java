package com.example.knowledgesphere.auth.service;

import com.example.knowledgesphere.dto.auth.*;
import com.example.knowledgesphere.entity.Role;
import com.example.knowledgesphere.entity.User;
import com.example.knowledgesphere.exception.custom.BadRequestException;
import com.example.knowledgesphere.repository.UserRepository;
import com.example.knowledgesphere.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request){

        if(repository.existsByEmail(request.getEmail())){

            throw new BadRequestException("Email already exists.");

        }

        User user = User.builder()

                .fullName(request.getFullName())

                .email(request.getEmail())

                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(Role.ROLE_USER)

                .enabled(true)

                .build();

        repository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()

                .accessToken(token)

                .email(user.getEmail())

                .fullName(user.getFullName())

                .role(user.getRole().name())

                .build();

    }

    public AuthResponse login(LoginRequest request){

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),

                        request.getPassword()

                )

        );

        User user = repository.findByEmail(request.getEmail())

                .orElseThrow();

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()

                .accessToken(token)

                .email(user.getEmail())

                .fullName(user.getFullName())

                .role(user.getRole().name())

                .build();

    }

}