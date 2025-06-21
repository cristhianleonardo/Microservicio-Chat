package com.subaston.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomTest {

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom();
    }

    @Test
    void testChatRoomCreation() {
        // Arrange
        Long id = 1L;
        String roomId = "room123";
        String ownerId = "user123";
        boolean isActive = true;
        boolean onlyOwnerCanWrite = false;

        // Act
        chatRoom.setId(id);
        chatRoom.setRoomId(roomId);
        chatRoom.setOwnerId(ownerId);
        chatRoom.setActive(isActive);
        chatRoom.setOnlyOwnerCanWrite(onlyOwnerCanWrite);

        // Assert
        assertEquals(id, chatRoom.getId());
        assertEquals(roomId, chatRoom.getRoomId());
        assertEquals(ownerId, chatRoom.getOwnerId());
        assertEquals(isActive, chatRoom.isActive());
        assertEquals(onlyOwnerCanWrite, chatRoom.isOnlyOwnerCanWrite());
    }

    @Test
    void testChatRoomWithDefaultValues() {
        // Act & Assert
        assertNull(chatRoom.getId());
        assertNull(chatRoom.getRoomId());
        assertNull(chatRoom.getOwnerId());
        assertFalse(chatRoom.isActive());
        assertFalse(chatRoom.isOnlyOwnerCanWrite());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ChatRoom room1 = new ChatRoom();
        room1.setId(1L);
        room1.setRoomId("room123");
        room1.setOwnerId("user123");
        room1.setActive(true);
        room1.setOnlyOwnerCanWrite(false);

        ChatRoom room2 = new ChatRoom();
        room2.setId(1L);
        room2.setRoomId("room123");
        room2.setOwnerId("user123");
        room2.setActive(true);
        room2.setOnlyOwnerCanWrite(false);

        ChatRoom room3 = new ChatRoom();
        room3.setId(2L);
        room3.setRoomId("room456");
        room3.setOwnerId("user456");
        room3.setActive(false);
        room3.setOnlyOwnerCanWrite(true);

        // Assert
        assertEquals(room1, room2);
        assertNotEquals(room1, room3);
        assertEquals(room1.hashCode(), room2.hashCode());
        assertNotEquals(room1.hashCode(), room3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        chatRoom.setId(1L);
        chatRoom.setRoomId("room123");
        chatRoom.setOwnerId("user123");
        chatRoom.setActive(true);
        chatRoom.setOnlyOwnerCanWrite(false);

        // Act
        String result = chatRoom.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("roomId=room123"));
        assertTrue(result.contains("ownerId=user123"));
        assertTrue(result.contains("isActive=true"));
        assertTrue(result.contains("onlyOwnerCanWrite=false"));
    }

    @Test
    void testToggleActiveStatus() {
        // Arrange
        chatRoom.setActive(false);

        // Act
        chatRoom.setActive(true);

        // Assert
        assertTrue(chatRoom.isActive());

        // Act
        chatRoom.setActive(false);

        // Assert
        assertFalse(chatRoom.isActive());
    }

    @Test
    void testToggleWritePermission() {
        // Arrange
        chatRoom.setOnlyOwnerCanWrite(false);

        // Act
        chatRoom.setOnlyOwnerCanWrite(true);

        // Assert
        assertTrue(chatRoom.isOnlyOwnerCanWrite());

        // Act
        chatRoom.setOnlyOwnerCanWrite(false);

        // Assert
        assertFalse(chatRoom.isOnlyOwnerCanWrite());
    }

    @Test
    void testRoomOwnership() {
        // Arrange
        String ownerId = "user123";
        chatRoom.setOwnerId(ownerId);

        // Act & Assert
        assertEquals(ownerId, chatRoom.getOwnerId());
        assertTrue(chatRoom.getOwnerId().equals("user123"));
        assertFalse(chatRoom.getOwnerId().equals("user456"));
    }

    @Test
    void testRoomIdUniqueness() {
        // Arrange
        String roomId1 = "room123";
        String roomId2 = "room456";

        // Act
        chatRoom.setRoomId(roomId1);

        // Assert
        assertEquals(roomId1, chatRoom.getRoomId());

        // Act
        chatRoom.setRoomId(roomId2);

        // Assert
        assertEquals(roomId2, chatRoom.getRoomId());
        assertNotEquals(roomId1, chatRoom.getRoomId());
    }
} 