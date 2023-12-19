package com.project.webProject.persistence;


import com.project.webProject.model.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Integer> {
    // 만료되지 않았으며 현재보다 이후에 만료되는 토큰정보를 가져옴
    Optional<EmailToken> findByEmailTokenIdAndExpirationDateAfterAndExpired(String emailTokenId, LocalDateTime now, boolean expired);


}



