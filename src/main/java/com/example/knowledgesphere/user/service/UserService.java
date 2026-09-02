package com.example.knowledgesphere.user.service;

import com.example.knowledgesphere.entity.User;
import com.example.knowledgesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User getCurrentUser(String email){

        return repository.findByEmail(email)

                .orElseThrow();

    }

}