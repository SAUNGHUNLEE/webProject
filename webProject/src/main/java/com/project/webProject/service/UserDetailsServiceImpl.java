package com.project.webProject.service;

import com.project.webProject.model.Member;
import com.project.webProject.persistence.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다. " + email));

        if(member.getState() != 1){
            throw new IllegalArgumentException("이메일 인증이 안된 계정입니다.");
        }
        return User.builder()
                .username(member.getEmail()) //
                .password(member.getPassword())
                // 관리자인 경우: .authorities("ROLE_ADMIN") 추가
                .roles(member.getRole() == 1 ? "ADMIN" : "USER")
                .build();
    }
}
