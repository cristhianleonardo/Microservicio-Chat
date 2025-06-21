package com.subaston.chat;

import com.subaston.chat.model.ChatMessage;
import com.subaston.chat.model.ChatRoom;
import com.subaston.chat.repository.ChatMessageRepository;
import com.subaston.chat.repository.ChatRoomRepository;
import com.subaston.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ChatIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/chat";
        headers = new HttpHeaders();
        headers.set("X-User-Id", "testUser123");
    }

    @Test
    void testCreateRoom() {
        // Act
        ResponseEntity<ChatRoom> response = restTemplate.exchange(
                baseUrl + "/room",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ChatRoom.class
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRoomId());
        assertEquals("testUser123", response.getBody().getOwnerId());
        assertTrue(response.getBody().isActive());
        assertFalse(response.getBody().isOnlyOwnerCanWrite());
    }

    @Test
    void testGetRoomMessages() {
        // Arrange
        ChatRoom room = createTestRoom();
        createTestMessages(room.getRoomId());

        // Act
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/room/" + room.getRoomId() + "/messages",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    void testToggleWritePermission() {
        // Arrange
        ChatRoom room = createTestRoom();

        // Act
        ResponseEntity<ChatRoom> response = restTemplate.exchange(
                baseUrl + "/room/" + room.getRoomId() + "/toggleWrite",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                ChatRoom.class
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isOnlyOwnerCanWrite());
    }

    @Test
    void testToggleWritePermission_WithNonOwner() {
        // Arrange
        ChatRoom room = createTestRoom();
        HttpHeaders nonOwnerHeaders = new HttpHeaders();
        nonOwnerHeaders.set("X-User-Id", "nonOwnerUser");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/room/" + room.getRoomId() + "/toggleWrite",
                HttpMethod.PUT,
                new HttpEntity<>(nonOwnerHeaders),
                String.class
        );

        // Assert
        assertEquals(500, response.getStatusCodeValue()); // Debería fallar
    }

    @Test
    void testChatServiceIntegration() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setRoomId("testRoom123");
        message.setSenderId("testUser123");
        message.setContent("Mensaje de prueba");
        message.setTimestamp(LocalDateTime.now());
        message.setType(ChatMessage.MessageType.CHAT);

        // Act
        ChatMessage savedMessage = chatService.saveMessage(message);
        List<ChatMessage> messages = chatService.getRoomMessages("testRoom123");

        // Assert
        assertNotNull(savedMessage.getId());
        assertEquals("testRoom123", savedMessage.getRoomId());
        assertEquals("testUser123", savedMessage.getSenderId());
        assertEquals("Mensaje de prueba", savedMessage.getContent());
        assertEquals(1, messages.size());
        assertEquals(savedMessage.getId(), messages.get(0).getId());
    }

    @Test
    void testRepositoryIntegration() {
        // Arrange
        ChatRoom room = new ChatRoom();
        room.setRoomId("integrationRoom");
        room.setOwnerId("integrationUser");
        room.setActive(true);
        room.setOnlyOwnerCanWrite(false);

        // Act
        ChatRoom savedRoom = chatRoomRepository.save(room);
        Optional<ChatRoom> foundRoom = chatRoomRepository.findByRoomId("integrationRoom");

        // Assert
        assertNotNull(savedRoom.getId());
        assertTrue(foundRoom.isPresent());
        assertEquals("integrationRoom", foundRoom.get().getRoomId());
        assertEquals("integrationUser", foundRoom.get().getOwnerId());
    }

    @Test
    void testMessageOrdering() {
        // Arrange
        String roomId = "orderingRoom";
        createTestRoomWithId(roomId);

        ChatMessage message1 = new ChatMessage();
        message1.setRoomId(roomId);
        message1.setSenderId("user1");
        message1.setContent("Primer mensaje");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(2));
        message1.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message2 = new ChatMessage();
        message2.setRoomId(roomId);
        message2.setSenderId("user2");
        message2.setContent("Segundo mensaje");
        message2.setTimestamp(LocalDateTime.now().minusMinutes(1));
        message2.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message3 = new ChatMessage();
        message3.setRoomId(roomId);
        message3.setSenderId("user3");
        message3.setContent("Tercer mensaje");
        message3.setTimestamp(LocalDateTime.now());
        message3.setType(ChatMessage.MessageType.CHAT);

        // Act
        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);
        chatMessageRepository.save(message3);

        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

        // Assert
        assertEquals(3, messages.size());
        assertEquals("Primer mensaje", messages.get(0).getContent());
        assertEquals("Segundo mensaje", messages.get(1).getContent());
        assertEquals("Tercer mensaje", messages.get(2).getContent());
    }

    // Métodos auxiliares
    private ChatRoom createTestRoom() {
        ChatRoom room = new ChatRoom();
        room.setRoomId("testRoom" + System.currentTimeMillis());
        room.setOwnerId("testUser123");
        room.setActive(true);
        room.setOnlyOwnerCanWrite(false);
        return chatRoomRepository.save(room);
    }

    private ChatRoom createTestRoomWithId(String roomId) {
        ChatRoom room = new ChatRoom();
        room.setRoomId(roomId);
        room.setOwnerId("testUser123");
        room.setActive(true);
        room.setOnlyOwnerCanWrite(false);
        return chatRoomRepository.save(room);
    }

    private void createTestMessages(String roomId) {
        ChatMessage message1 = new ChatMessage();
        message1.setRoomId(roomId);
        message1.setSenderId("user1");
        message1.setContent("Mensaje 1");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(1));
        message1.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message2 = new ChatMessage();
        message2.setRoomId(roomId);
        message2.setSenderId("user2");
        message2.setContent("Mensaje 2");
        message2.setTimestamp(LocalDateTime.now());
        message2.setType(ChatMessage.MessageType.CHAT);

        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);
    }
} 