package com.project.webProject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.webProject.DTO.ChatRoomDTO;
import com.project.webProject.DTO.ChatRoomInfoDTO;
import com.project.webProject.DTO.MemberDTO;
import com.project.webProject.config.CryptoUtil;
import com.project.webProject.model.ChatRoom;
import com.project.webProject.model.Member;
import com.project.webProject.persistence.ChatRoomRepository;
import com.project.webProject.persistence.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Slf4j
@Service
public class ChatService {
    @Autowired
    private ObjectMapper mapper;
    private Map<String, ChatRoomInfoDTO> chatRooms;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @PostConstruct
    private void init(){
        chatRooms = new LinkedHashMap<>();
    }

    @Autowired
    private CryptoUtil cryptoUtil;

    public List<ChatRoomInfoDTO> findAllRoom() {
     List<ChatRoomInfoDTO> result = new ArrayList<>(chatRooms.values());
     Collections.reverse(result);
     return result;
    }

    public ChatRoomInfoDTO findById(String roomId){
        return chatRooms.get(roomId);
    }

    public ChatRoomInfoDTO createRoom(String name){
        ChatRoomInfoDTO chatRoomInfoDTO = ChatRoomInfoDTO.create(name);
        chatRooms.put(chatRoomInfoDTO.getRoomId(),chatRoomInfoDTO);
        return chatRoomInfoDTO;
    }


    public ChatRoom saveMessage(String type, String roomId,String roomName, String sender, String message) {
        try {
            String encryptedMessage = cryptoUtil.encrypt(message);
            ChatRoom chatRoom = ChatRoom.builder()
                    .type(ChatRoom.MessageType.valueOf(type)) // 문자열을 열거형 값으로 변환
                    .roomId(roomId)
                    .roomName(roomName)
                    .sender(sender)
                    .message(encryptedMessage)
                    .time(LocalDateTime.now())
                    .build();
            return chatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            throw new RuntimeException("에러발생", e);
        }
    }




}
