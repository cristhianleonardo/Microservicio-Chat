package com.subaston.chat.repository;

import com.subaston.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private ChatMessage testMessage1;
    private ChatMessage testMessage2;
    private ChatMessage testMessage3;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        entityManager.clear();

        // Crear mensajes de prueba
        testMessage1 = new ChatMessage();
        testMessage1.setRoomId("room123");
        testMessage1.setSenderId("user123");
        testMessage1.setContent("Hola, ¿cómo estás?");
        testMessage1.setTimestamp(LocalDateTime.now().minusMinutes(2));
        testMessage1.setType(ChatMessage.MessageType.CHAT);

        testMessage2 = new ChatMessage();
        testMessage2.setRoomId("room123");
        testMessage2.setSenderId("user456");
        testMessage2.setContent("¡Hola! Todo bien");
        testMessage2.setTimestamp(LocalDateTime.now().minusMinutes(1));
        testMessage2.setType(ChatMessage.MessageType.CHAT);

        testMessage3 = new ChatMessage();
        testMessage3.setRoomId("room456");
        testMessage3.setSenderId("user789");
        testMessage3.setContent("Mensaje de otra sala");
        testMessage3.setTimestamp(LocalDateTime.now());
        testMessage3.setType(ChatMessage.MessageType.CHAT);
    }

    @Test
    void testSaveMessage() {
        // Act
        ChatMessage savedMessage = chatMessageRepository.save(testMessage1);

        // Assert
        assertNotNull(savedMessage.getId());
        assertEquals(testMessage1.getRoomId(), savedMessage.getRoomId());
        assertEquals(testMessage1.getSenderId(), savedMessage.getSenderId());
        assertEquals(testMessage1.getContent(), savedMessage.getContent());
        assertEquals(testMessage1.getType(), savedMessage.getType());
    }

    @Test
    void testFindByRoomIdOrderByTimestampAsc() {
        // Arrange
        entityManager.persistAndFlush(testMessage1);
        entityManager.persistAndFlush(testMessage2);
        entityManager.persistAndFlush(testMessage3);

        // Act
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc("room123");

        // Assert
        assertEquals(2, messages.size());
        assertEquals(testMessage1.getContent(), messages.get(0).getContent());
        assertEquals(testMessage2.getContent(), messages.get(1).getContent());
        assertTrue(messages.get(0).getTimestamp().isBefore(messages.get(1).getTimestamp()));
    }

    @Test
    void testFindByRoomIdOrderByTimestampAsc_WithEmptyRoom() {
        // Act
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc("emptyRoom");

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    void testFindByRoomIdOrderByTimestampAsc_WithMultipleRooms() {
        // Arrange
        entityManager.persistAndFlush(testMessage1);
        entityManager.persistAndFlush(testMessage2);
        entityManager.persistAndFlush(testMessage3);

        // Act
        List<ChatMessage> room123Messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc("room123");
        List<ChatMessage> room456Messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc("room456");

        // Assert
        assertEquals(2, room123Messages.size());
        assertEquals(1, room456Messages.size());
        assertEquals("room123", room123Messages.get(0).getRoomId());
        assertEquals("room456", room456Messages.get(0).getRoomId());
    }

    @Test
    void testSaveMessageWithDifferentTypes() {
        // Arrange
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setRoomId("room123");
        joinMessage.setSenderId("user123");
        joinMessage.setContent("user123 se ha unido al chat!");
        joinMessage.setTimestamp(LocalDateTime.now());
        joinMessage.setType(ChatMessage.MessageType.JOIN);

        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setRoomId("room123");
        leaveMessage.setSenderId("user456");
        leaveMessage.setContent("user456 ha abandonado el chat");
        leaveMessage.setTimestamp(LocalDateTime.now());
        leaveMessage.setType(ChatMessage.MessageType.LEAVE);

        // Act
        ChatMessage savedJoinMessage = chatMessageRepository.save(joinMessage);
        ChatMessage savedLeaveMessage = chatMessageRepository.save(leaveMessage);

        // Assert
        assertEquals(ChatMessage.MessageType.JOIN, savedJoinMessage.getType());
        assertEquals(ChatMessage.MessageType.LEAVE, savedLeaveMessage.getType());
    }

    @Test
    void testMessageOrdering() {
        // Arrange
        ChatMessage message1 = new ChatMessage();
        message1.setRoomId("room123");
        message1.setSenderId("user1");
        message1.setContent("Primer mensaje");
        message1.setTimestamp(LocalDateTime.now().minusHours(1));
        message1.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message2 = new ChatMessage();
        message2.setRoomId("room123");
        message2.setSenderId("user2");
        message2.setContent("Segundo mensaje");
        message2.setTimestamp(LocalDateTime.now());
        message2.setType(ChatMessage.MessageType.CHAT);

        ChatMessage message3 = new ChatMessage();
        message3.setRoomId("room123");
        message3.setSenderId("user3");
        message3.setContent("Tercer mensaje");
        message3.setTimestamp(LocalDateTime.now().minusMinutes(30));
        message3.setType(ChatMessage.MessageType.CHAT);

        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);
        entityManager.persistAndFlush(message3);

        // Act
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc("room123");

        // Assert
        assertEquals(3, messages.size());
        assertEquals("Primer mensaje", messages.get(0).getContent());
        assertEquals("Tercer mensaje", messages.get(1).getContent());
        assertEquals("Segundo mensaje", messages.get(2).getContent());
    }

    @Test
    void testUpdateMessage() {
        // Arrange
        ChatMessage savedMessage = entityManager.persistAndFlush(testMessage1);
        String newContent = "Mensaje actualizado";

        // Act
        savedMessage.setContent(newContent);
        ChatMessage updatedMessage = chatMessageRepository.save(savedMessage);

        // Assert
        assertEquals(newContent, updatedMessage.getContent());
        assertEquals(savedMessage.getId(), updatedMessage.getId());
    }

    @Test
    void testDeleteMessage() {
        // Arrange
        ChatMessage savedMessage = entityManager.persistAndFlush(testMessage1);
        Long messageId = savedMessage.getId();

        // Act
        chatMessageRepository.delete(savedMessage);

        // Assert
        assertFalse(chatMessageRepository.findById(messageId).isPresent());
    }
} 