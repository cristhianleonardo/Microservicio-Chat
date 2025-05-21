package com.subaston.chat.repository;

import com.subaston.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
    Optional<ChatRoom> findByOwnerId(String ownerId);
} 