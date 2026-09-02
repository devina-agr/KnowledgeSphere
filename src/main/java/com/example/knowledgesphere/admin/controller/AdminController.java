package com.example.knowledgesphere.admin.controller;

import com.example.knowledgesphere.admin.service.AdminService;
import com.example.knowledgesphere.dto.admin.AdminDashboardResponse;
import com.example.knowledgesphere.dto.admin.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PutMapping("/users/{id}/role")
    public UserResponse updateUserRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        return adminService.updateUserRole(id, role);
    }

    @PutMapping("/users/{id}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return adminService.updateUserStatus(id, enabled);
    }

}
