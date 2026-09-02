package com.example.knowledgesphere.user.controller;

import com.example.knowledgesphere.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public User me(

            @AuthenticationPrincipal
            User user){

        return user;

    }

}