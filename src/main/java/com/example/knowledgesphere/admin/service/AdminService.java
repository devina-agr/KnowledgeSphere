package com.example.knowledgesphere.admin.service;

import com.example.knowledgesphere.dto.admin.AdminDashboardResponse;
import com.example.knowledgesphere.dto.admin.UserResponse;
import com.example.knowledgesphere.entity.Role;
import com.example.knowledgesphere.entity.User;
import com.example.knowledgesphere.exception.custom.ResourceNotFoundException;
import com.example.knowledgesphere.repository.ConversationRepository;
import com.example.knowledgesphere.repository.DocumentRepository;
import com.example.knowledgesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ConversationRepository conversationRepository;

    public AdminDashboardResponse getDashboardStats() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalDocuments(documentRepository.count())
                .totalChats(conversationRepository.count())
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            Role role = Role.valueOf(roleName);
            user.setRole(role);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new com.example.knowledgesphere.exception.custom.BadRequestException("Invalid role name: " + roleName);
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(enabled);
        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
