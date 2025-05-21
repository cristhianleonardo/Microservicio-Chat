package com.subaston.chat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private MessageType type;
    
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        SYSTEM
    }
} 