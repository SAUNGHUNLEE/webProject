package com.project.webProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "join_day")
    private LocalDateTime joinDay;

    @Column(name = "profile")
    private String profile;

    @Column(name = "role")
    private int role;  //0:학생 1:관리자

    @Column(name = "state")
    private int state; //0:미인증 1:인증

    @Column(name = "birthday")
    private LocalDate birthDay; //0:미인증 1:인증


}
