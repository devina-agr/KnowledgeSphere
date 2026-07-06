package com.example.knowledgesphere.dto.admin;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private Long totalUsers;

    private Long totalDocuments;

    private Long totalChats;

}