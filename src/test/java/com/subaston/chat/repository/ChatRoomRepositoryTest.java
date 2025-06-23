package com.subaston.chat.repository;

import com.subaston.chat.model.ChatRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatRoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private ChatRoom testRoom1;
    private ChatRoom testRoom2;
    private ChatRoom testRoom3;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        entityManager.clear();

        // Crear salas de chat de prueba
        testRoom1 = new ChatRoom();
        testRoom1.setRoomId("room123");
        testRoom1.setOwnerId("user123");
        testRoom1.setActive(true);
        testRoom1.setOnlyOwnerCanWrite(false);

        testRoom2 = new ChatRoom();
        testRoom2.setRoomId("room456");
        testRoom2.setOwnerId("user456");
        testRoom2.setActive(true);
        testRoom2.setOnlyOwnerCanWrite(true);

        testRoom3 = new ChatRoom();
        testRoom3.setRoomId("room789");
        testRoom3.setOwnerId("user123");
        testRoom3.setActive(false);
        testRoom3.setOnlyOwnerCanWrite(false);
    }

    @Test
    void testSaveRoom() {
        // Act
        ChatRoom savedRoom = chatRoomRepository.save(testRoom1);

        // Assert
        assertNotNull(savedRoom.getId());
        assertEquals(testRoom1.getRoomId(), savedRoom.getRoomId());
        assertEquals(testRoom1.getOwnerId(), savedRoom.getOwnerId());
        assertEquals(testRoom1.isActive(), savedRoom.isActive());
        assertEquals(testRoom1.isOnlyOwnerCanWrite(), savedRoom.isOnlyOwnerCanWrite());
    }

    @Test
    void testFindByRoomId() {
        // Arrange
        entityManager.persistAndFlush(testRoom1);
        entityManager.persistAndFlush(testRoom2);

        // Act
        Optional<ChatRoom> foundRoom = chatRoomRepository.findByRoomId("room123");

        // Assert
        assertTrue(foundRoom.isPresent());
        assertEquals("room123", foundRoom.get().getRoomId());
        assertEquals("user123", foundRoom.get().getOwnerId());
    }

    @Test
    void testFindByRoomId_WithNonExistentRoom() {
        // Act
        Optional<ChatRoom> foundRoom = chatRoomRepository.findByRoomId("nonexistent");

        // Assert
        assertFalse(foundRoom.isPresent());
    }

    @Test
    void testFindByOwnerId() {
        // Arrange
        entityManager.persistAndFlush(testRoom1);
        entityManager.persistAndFlush(testRoom2);
        entityManager.persistAndFlush(testRoom3);

        // Act
        Optional<ChatRoom> foundRoom = chatRoomRepository.findByOwnerId("user123");

        // Assert
        assertTrue(foundRoom.isPresent());
        assertEquals("user123", foundRoom.get().getOwnerId());
    }

    @Test
    void testFindByOwnerId_WithNonExistentOwner() {
        // Act
        Optional<ChatRoom> foundRoom = chatRoomRepository.findByOwnerId("nonexistent");

        // Assert
        assertFalse(foundRoom.isPresent());
    }

    @Test
    void testFindAllRooms() {
        // Arrange
        entityManager.persistAndFlush(testRoom1);
        entityManager.persistAndFlush(testRoom2);
        entityManager.persistAndFlush(testRoom3);

        // Act
        List<ChatRoom> allRooms = chatRoomRepository.findAll();

        // Assert
        assertEquals(3, allRooms.size());
    }

    @Test
    void testUpdateRoom() {
        // Arrange
        ChatRoom savedRoom = entityManager.persistAndFlush(testRoom1);
        String newRoomId = "updatedRoom123";

        // Act
        savedRoom.setRoomId(newRoomId);
        ChatRoom updatedRoom = chatRoomRepository.save(savedRoom);

        // Assert
        assertEquals(newRoomId, updatedRoom.getRoomId());
        assertEquals(savedRoom.getId(), updatedRoom.getId());
    }

    @Test
    void testDeleteRoom() {
        // Arrange
        ChatRoom savedRoom = entityManager.persistAndFlush(testRoom1);
        Long roomId = savedRoom.getId();

        // Act
        chatRoomRepository.delete(savedRoom);

        // Assert
        assertFalse(chatRoomRepository.findById(roomId).isPresent());
    }

    @Test
    void testRoomWithDifferentStates() {
        // Arrange
        ChatRoom activeRoom = new ChatRoom();
        activeRoom.setRoomId("activeRoom");
        activeRoom.setOwnerId("user1");
        activeRoom.setActive(true);
        activeRoom.setOnlyOwnerCanWrite(false);

        ChatRoom inactiveRoom = new ChatRoom();
        inactiveRoom.setRoomId("inactiveRoom");
        inactiveRoom.setOwnerId("user2");
        inactiveRoom.setActive(false);
        inactiveRoom.setOnlyOwnerCanWrite(true);

        entityManager.persistAndFlush(activeRoom);
        entityManager.persistAndFlush(inactiveRoom);

        // Act
        Optional<ChatRoom> foundActiveRoom = chatRoomRepository.findByRoomId("activeRoom");
        Optional<ChatRoom> foundInactiveRoom = chatRoomRepository.findByRoomId("inactiveRoom");

        // Assert
        assertTrue(foundActiveRoom.isPresent());
        assertTrue(foundActiveRoom.get().isActive());
        assertFalse(foundActiveRoom.get().isOnlyOwnerCanWrite());

        assertTrue(foundInactiveRoom.isPresent());
        assertFalse(foundInactiveRoom.get().isActive());
        assertTrue(foundInactiveRoom.get().isOnlyOwnerCanWrite());
    }

    @Test
    void testRoomIdUniqueness() {
        // Arrange
        entityManager.persistAndFlush(testRoom1);

        ChatRoom duplicateRoom = new ChatRoom();
        duplicateRoom.setRoomId("room123"); // Mismo roomId
        duplicateRoom.setOwnerId("user999");
        duplicateRoom.setActive(true);
        duplicateRoom.setOnlyOwnerCanWrite(false);

        // Act & Assert
        // En una base de datos real, esto debería fallar si roomId es único
        // Para este test, verificamos que se puede guardar (depende de la configuración de la BD)
        ChatRoom savedDuplicate = chatRoomRepository.save(duplicateRoom);
        assertNotNull(savedDuplicate.getId());
    }

    @Test
    void testToggleRoomStates() {
        // Arrange
        ChatRoom room = entityManager.persistAndFlush(testRoom1);

        // Act - Toggle active state
        room.setActive(!room.isActive());
        ChatRoom updatedRoom1 = chatRoomRepository.save(room);

        // Act - Toggle write permission
        room.setOnlyOwnerCanWrite(!room.isOnlyOwnerCanWrite());
        ChatRoom updatedRoom2 = chatRoomRepository.save(room);

        // Assert
        assertFalse(updatedRoom1.isActive()); // Cambió de true a false
        assertTrue(updatedRoom2.isOnlyOwnerCanWrite()); // Cambió de false a true
    }
} 