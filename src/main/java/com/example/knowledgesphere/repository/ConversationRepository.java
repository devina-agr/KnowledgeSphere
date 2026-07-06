package com.example.knowledgesphere.repository;

import com.example.knowledgesphere.entity.Conversation;
import com.example.knowledgesphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);

}