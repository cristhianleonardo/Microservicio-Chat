package com.subaston.chat.service;

import com.subaston.chat.model.ChatMessage;
import com.subaston.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    private ChatMessage testMessage;
    private List<ChatMessage> testMessages;

    @BeforeEach
    void setUp() {
        testMessage = new ChatMessage();
        testMessage.setId(1L);
        testMessage.setRoomId("room123");
        testMessage.setSenderId("user123");
        testMessage.setContent("Hola, ¿cómo estás?");
        testMessage.setTimestamp(LocalDateTime.now());
        testMessage.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message2 = new ChatMessage();
        message2.setId(2L);
        message2.setRoomId("room123");
        message2.setSenderId("user456");
        message2.setContent("¡Hola! Todo bien");
        message2.setTimestamp(LocalDateTime.now().plusMinutes(1));
        message2.setType(ChatMessage.MessageType.CHAT);

        testMessages = Arrays.asList(testMessage, message2);
    }

    @Test
    void saveMessage_ShouldSaveAndReturnMessage() {
        // Arrange
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        ChatMessage result = chatService.saveMessage(testMessage);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        assertEquals(testMessage.getRoomId(), result.getRoomId());
        assertEquals(testMessage.getSenderId(), result.getSenderId());
        assertEquals(testMessage.getContent(), result.getContent());
        verify(chatMessageRepository, times(1)).save(testMessage);
    }

    @Test
    void saveMessage_WithNullMessage_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.saveMessage(null);
        });
    }

    @Test
    void getRoomMessages_ShouldReturnMessagesForRoom() {
        // Arrange
        String roomId = "room123";
        when(chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)).thenReturn(testMessages);

        // Act
        List<ChatMessage> result = chatService.getRoomMessages(roomId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
        assertEquals("user456", result.get(1).getSenderId());
        verify(chatMessageRepository, times(1)).findByRoomIdOrderByTimestampAsc(roomId);
    }

    @Test
    void getRoomMessages_WithEmptyRoom_ShouldReturnEmptyList() {
        // Arrange
        String roomId = "emptyRoom";
        when(chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)).thenReturn(Arrays.asList());

        // Act
        List<ChatMessage> result = chatService.getRoomMessages(roomId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatMessageRepository, times(1)).findByRoomIdOrderByTimestampAsc(roomId);
    }

    @Test
    void getRoomMessages_WithNullRoomId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getRoomMessages(null);
        });
    }

    @Test
    void getRoomMessages_WithEmptyRoomId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getRoomMessages("");
        });
    }
} 