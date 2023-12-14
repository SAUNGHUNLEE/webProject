package com.project.webProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_number")
public class EmailNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "email_number_id")
    private int emailNumberId;

    @Column(name = "expiration_date")
    private Timestamp expirationDate;

    @Column(name = "expired")
    private int expired;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;


}
