package com.subaston.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatMessage = new ChatMessage();
    }

    @Test
    void testChatMessageCreation() {
        // Arrange
        Long id = 1L;
        String roomId = "room123";
        String senderId = "user123";
        String content = "Hola, ¿cómo estás?";
        LocalDateTime timestamp = LocalDateTime.now();
        ChatMessage.MessageType type = ChatMessage.MessageType.CHAT;

        // Act
        chatMessage.setId(id);
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(senderId);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(timestamp);
        chatMessage.setType(type);

        // Assert
        assertEquals(id, chatMessage.getId());
        assertEquals(roomId, chatMessage.getRoomId());
        assertEquals(senderId, chatMessage.getSenderId());
        assertEquals(content, chatMessage.getContent());
        assertEquals(timestamp, chatMessage.getTimestamp());
        assertEquals(type, chatMessage.getType());
    }

    @Test
    void testMessageTypeEnum() {
        // Test all enum values
        assertEquals(ChatMessage.MessageType.CHAT, ChatMessage.MessageType.valueOf("CHAT"));
        assertEquals(ChatMessage.MessageType.JOIN, ChatMessage.MessageType.valueOf("JOIN"));
        assertEquals(ChatMessage.MessageType.LEAVE, ChatMessage.MessageType.valueOf("LEAVE"));
        assertEquals(ChatMessage.MessageType.SYSTEM, ChatMessage.MessageType.valueOf("SYSTEM"));
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ChatMessage message1 = new ChatMessage();
        message1.setId(1L);
        message1.setRoomId("room123");
        message1.setSenderId("user123");
        message1.setContent("Hola");
        message1.setTimestamp(LocalDateTime.now());
        message1.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message2 = new ChatMessage();
        message2.setId(1L);
        message2.setRoomId("room123");
        message2.setSenderId("user123");
        message2.setContent("Hola");
        message2.setTimestamp(message1.getTimestamp());
        message2.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message3 = new ChatMessage();
        message3.setId(2L);
        message3.setRoomId("room456");
        message3.setSenderId("user456");
        message3.setContent("Adiós");
        message3.setTimestamp(LocalDateTime.now());
        message3.setType(ChatMessage.MessageType.LEAVE);

        // Assert
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        chatMessage.setId(1L);
        chatMessage.setRoomId("room123");
        chatMessage.setSenderId("user123");
        chatMessage.setContent("Test message");
        chatMessage.setTimestamp(LocalDateTime.of(2024, 1, 1, 12, 0));
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        // Act
        String result = chatMessage.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("roomId=room123"));
        assertTrue(result.contains("senderId=user123"));
        assertTrue(result.contains("content=Test message"));
        assertTrue(result.contains("type=CHAT"));
    }

    @Test
    void testNullValues() {
        // Act & Assert
        assertNull(chatMessage.getId());
        assertNull(chatMessage.getRoomId());
        assertNull(chatMessage.getSenderId());
        assertNull(chatMessage.getContent());
        assertNull(chatMessage.getTimestamp());
        assertNull(chatMessage.getType());
    }
} 