package com.project.webProject.persistence;

import com.project.webProject.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    // 해당하는 이메일이 있는지 확인
    Boolean existsByEmail (String email);

    @Query(value = "SELECT COUNT(id) FROM member WHERE name = :name", nativeQuery = true)
    int findByName(@Param("name") String name);

    @Query(value = "SELECT * FROM member WHERE email =:email",nativeQuery = true)
    Optional<Member> findByEmail(@Param("email")String email);
}



