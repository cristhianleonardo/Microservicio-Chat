package com.subaston.chat.controller;

import com.subaston.chat.model.ChatMessage;
import com.subaston.chat.model.ChatRoom;
import com.subaston.chat.repository.ChatMessageRepository;
import com.subaston.chat.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestHeader("X-User-Id") String userId) {
        ChatRoom room = new ChatRoom();
        room.setRoomId(UUID.randomUUID().toString());
        room.setOwnerId(userId);
        room.setActive(true);
        room.setOnlyOwnerCanWrite(false);
        return chatRoomRepository.save(room);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
        
        ChatRoom room = chatRoomRepository.findByRoomId(chatMessage.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.isOnlyOwnerCanWrite() && !userId.equals(room.getOwnerId())) {
            return;
        }

        chatMessage.setSenderId(userId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
        
        headerAccessor.getSessionAttributes().put("username", userId);
        headerAccessor.getSessionAttributes().put("roomId", chatMessage.getRoomId());

        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setType(ChatMessage.MessageType.JOIN);
        joinMessage.setSenderId(userId);
        joinMessage.setRoomId(chatMessage.getRoomId());
        joinMessage.setTimestamp(LocalDateTime.now());
        joinMessage.setContent(userId + " se ha unido al chat!");

        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), joinMessage);
    }

    @PutMapping("/room/{roomId}/toggleWrite")
    @ResponseBody
    public ChatRoom toggleWritePermission(@PathVariable String roomId, @RequestHeader("X-User-Id") String userId) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerId().equals(userId)) {
            throw new RuntimeException("Only room owner can toggle write permission");
        }

        room.setOnlyOwnerCanWrite(!room.isOnlyOwnerCanWrite());
        return chatRoomRepository.save(room);
    }

    @GetMapping("/room/{roomId}/messages")
    @ResponseBody
    public List<ChatMessage> getRoomMessages(@PathVariable String roomId, @RequestHeader("X-User-Id") String userId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
} 