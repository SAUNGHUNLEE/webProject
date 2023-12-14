package com.project.webProject.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailNumber extends JpaRepository<EmailNumber, Integer> {



}



