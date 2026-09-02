package com.example.knowledgesphere.conversation.service;

import com.example.knowledgesphere.conversation.dto.ConversationDetailsResponse;
import com.example.knowledgesphere.conversation.mapper.ConversationMapper;
import com.example.knowledgesphere.entity.*;
import com.example.knowledgesphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;

    private final ConversationRepository conversationRepository;

    private final MessageRepository messageRepository;

    public Conversation createConversation(User user) {
        return createConversation(user, "New Chat");
    }

    public Conversation createConversation(User user, String title) {
        Conversation conversation = Conversation.builder()
                .title(title)
                .user(user)
                .build();
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Conversation getConversationForUser(Long id, User user) {
        return conversationRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new com.example.knowledgesphere.exception.custom.ResourceNotFoundException(
                        "Conversation not found or access denied."
                ));
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(Conversation conversation) {
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }

    @Transactional
    public void saveMessage(
            Conversation conversation,
            MessageRole role,
            String content
    ) {
        Message message = Message.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .build();

        messageRepository.save(message);
        conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversations(User user) {
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public ConversationDetailsResponse getConversationDetails(Long id, User user) {
        Conversation conversation = getConversationForUser(id, user);
        List<Message> messages = getMessages(conversation);
        return conversationMapper.toDetails(conversation, messages);
    }

}