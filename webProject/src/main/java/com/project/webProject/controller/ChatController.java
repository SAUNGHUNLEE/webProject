package com.project.webProject.controller;

import com.project.webProject.DTO.ChatRoomDTO;
import com.project.webProject.DTO.ChatRoomInfoDTO;
import com.project.webProject.DTO.MemberDTO;
import com.project.webProject.service.ChatService;
import com.project.webProject.service.EmailService;
import com.project.webProject.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 채팅 리스트 화면
    @GetMapping("/room")
    public String rooms(Model model) {
        return "room";
    }
    // 모든 채팅방 목록 반환
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoomInfoDTO> room() {
        return chatService.findAllRoom();
    }
    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoomInfoDTO createRoom(@RequestParam String name) {
        return chatService.createRoom(name);
    }
    // 채팅방 입장 화면
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "roomdetail";
    }
    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoomInfoDTO roomInfo(@PathVariable String roomId) {
        return chatService.findById(roomId);
    }

    @PostMapping("/send-message")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody ChatRoomDTO messageDTO) {
        try {
            chatService.saveMessage(messageDTO.getType(), messageDTO.getRoomId(), messageDTO.getRoomName(),
                    messageDTO.getSender(), messageDTO.getMessage());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing message");
        }
    }
}
