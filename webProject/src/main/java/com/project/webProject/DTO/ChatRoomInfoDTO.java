package com.project.webProject.DTO;

import com.project.webProject.model.ChatRoom;
import com.project.webProject.service.ChatService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class ChatRoomInfoDTO {
    private String roomId; // 채팅방 아이디
    private String roomName; // 채팅방 이름
    private long userCount; //채팅방 인원수
    private Set<WebSocketSession> sessions = new HashSet<>();

  /*  @Builder
    public ChatRoomInfoDTO(String roomId, String roomName){
        this.roomId = roomId;
        this.roomName = roomName;
    }


    public void handleAction(WebSocketSession session, ChatRoomDTO chatRoomDTO, ChatService chatService){
        if(chatRoomDTO.getType().equals(ChatRoomDTO.MessageType.ENTER)){
            sessions.add(session);

            // message 에는 입장하였다는 메시지를 띄운다
            chatRoomDTO.setMessage(chatRoomDTO.getSender() + " 님이 입장하셨습니다");
            sendMessage(chatRoomDTO, chatService);
        } else if (chatRoomDTO.getType().equals(ChatRoomDTO.MessageType.TALK)) {
            chatRoomDTO.setMessage(chatRoomDTO.getMessage());
            sendMessage(chatRoomDTO, chatService);
        }

    }

    public <T> void sendMessage(T message, ChatService service) {
        sessions.parallelStream().forEach(session -> service.sendMessage(session, message));
    }
*/

    public static ChatRoomInfoDTO create(String name) {
        ChatRoomInfoDTO chatRoomInfoDTO = new ChatRoomInfoDTO();
        chatRoomInfoDTO.roomId = UUID.randomUUID().toString();
        chatRoomInfoDTO.roomName = name;
        return chatRoomInfoDTO;
    }

}
