package com.example.knowledgesphere.auth.controller;

import com.example.knowledgesphere.auth.service.AuthService;
import com.example.knowledgesphere.dto.auth.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(

            @RequestBody
            @Valid
            RegisterRequest request

    ){

        return authService.register(request);

    }

    @PostMapping("/login")
    public AuthResponse login(

            @RequestBody
            @Valid
            LoginRequest request

    ){

        return authService.login(request);

    }

}