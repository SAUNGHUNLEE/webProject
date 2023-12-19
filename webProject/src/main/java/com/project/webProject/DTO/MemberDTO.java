package com.project.webProject.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private int id;
    private String password;
    private String email;
    private String name;
    private LocalDateTime joinDay;
    private MultipartFile profile;
    private String profilePath; // 프로필 사진 저장 경로
    private int role;  //0:학생 1:관리자
    private int state; //0:미인증 1:인증
    private LocalDate birthDay;
    private String status;
}
