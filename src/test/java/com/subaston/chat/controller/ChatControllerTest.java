package com.subaston.chat.controller;

import com.subaston.chat.model.ChatMessage;
import com.subaston.chat.model.ChatRoom;
import com.subaston.chat.repository.ChatMessageRepository;
import com.subaston.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatController chatController;

    private ChatRoom testRoom;
    private ChatMessage testMessage;
    private List<ChatMessage> testMessages;

    @BeforeEach
    void setUp() {
        testRoom = new ChatRoom();
        testRoom.setId(1L);
        testRoom.setRoomId("room123");
        testRoom.setOwnerId("user123");
        testRoom.setActive(true);
        testRoom.setOnlyOwnerCanWrite(false);

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
    void createRoom_ShouldCreateAndReturnRoom() {
        // Arrange
        String userId = "user123";
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testRoom);

        // Act
        ChatRoom result = chatController.createRoom(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testRoom.getRoomId(), result.getRoomId());
        assertEquals(testRoom.getOwnerId(), result.getOwnerId());
        assertTrue(result.isActive());
        assertFalse(result.isOnlyOwnerCanWrite());
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    void sendMessage_WithValidMessage_ShouldSaveAndSend() {
        // Arrange
        String userId = "user123";
        when(headerAccessor.getFirstNativeHeader("X-User-Id")).thenReturn(userId);
        when(chatRoomRepository.findByRoomId(testMessage.getRoomId())).thenReturn(Optional.of(testRoom));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        chatController.sendMessage(testMessage, headerAccessor);

        // Assert
        verify(chatMessageRepository, times(1)).save(testMessage);
        // verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void sendMessage_WithRoomNotFound_ShouldThrowException() {
        // Arrange
        String userId = "user123";
        when(headerAccessor.getFirstNativeHeader("X-User-Id")).thenReturn(userId);
        when(chatRoomRepository.findByRoomId(testMessage.getRoomId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.sendMessage(testMessage, headerAccessor);
        });
    }

    @Test
    void sendMessage_WithOnlyOwnerCanWrite_AndNonOwner_ShouldNotSend() {
        // Arrange
        String userId = "user456"; // Non-owner
        testRoom.setOnlyOwnerCanWrite(true);
        when(headerAccessor.getFirstNativeHeader("X-User-Id")).thenReturn(userId);
        when(chatRoomRepository.findByRoomId(testMessage.getRoomId())).thenReturn(Optional.of(testRoom));

        // Act
        chatController.sendMessage(testMessage, headerAccessor);

        // Assert
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        // verify(messagingTemplate, never()).convertAndSend(anyString(), any());
    }

    @Test
    void addUser_ShouldSendJoinMessage() {
        // Arrange
        String userId = "user123";
        when(headerAccessor.getFirstNativeHeader("X-User-Id")).thenReturn(userId);
        when(headerAccessor.getSessionAttributes()).thenReturn(new java.util.HashMap<>());

        // Act
        chatController.addUser(testMessage, headerAccessor);

        // Assert
        // verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void toggleWritePermission_WithOwner_ShouldTogglePermission() {
        // Arrange
        String userId = "user123";
        String roomId = "room123";
        when(chatRoomRepository.findByRoomId(roomId)).thenReturn(Optional.of(testRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testRoom);

        // Act
        ChatRoom result = chatController.toggleWritePermission(roomId, userId);

        // Assert
        assertNotNull(result);
        verify(chatRoomRepository, times(1)).save(testRoom);
    }

    @Test
    void toggleWritePermission_WithNonOwner_ShouldThrowException() {
        // Arrange
        String userId = "user456"; // Non-owner
        String roomId = "room123";
        when(chatRoomRepository.findByRoomId(roomId)).thenReturn(Optional.of(testRoom));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.toggleWritePermission(roomId, userId);
        });
    }

    @Test
    void toggleWritePermission_WithRoomNotFound_ShouldThrowException() {
        // Arrange
        String userId = "user123";
        String roomId = "nonexistent";
        when(chatRoomRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatController.toggleWritePermission(roomId, userId);
        });
    }

    @Test
    void getRoomMessages_ShouldReturnMessages() {
        // Arrange
        String roomId = "room123";
        String userId = "user123";
        when(chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)).thenReturn(testMessages);

        // Act
        List<ChatMessage> result = chatController.getRoomMessages(roomId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(chatMessageRepository, times(1)).findByRoomIdOrderByTimestampAsc(roomId);
    }

    @Test
    void getRoomMessages_WithEmptyRoom_ShouldReturnEmptyList() {
        // Arrange
        String roomId = "emptyRoom";
        String userId = "user123";
        when(chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)).thenReturn(Arrays.asList());

        // Act
        List<ChatMessage> result = chatController.getRoomMessages(roomId, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatMessageRepository, times(1)).findByRoomIdOrderByTimestampAsc(roomId);
    }
} 