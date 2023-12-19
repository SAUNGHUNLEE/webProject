package com.project.webProject.config;

import com.project.webProject.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;


@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/webjars/**").permitAll()
                                .requestMatchers("/ws/**").authenticated()
                                .requestMatchers("/member/**").permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // CSS, JS, 이미지 폴더에 대한 접근을 허용
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login") // 로그인 페이지 경로 설정
                        .loginProcessingUrl("/login") // 로그인 프로세스 URL, 폼 액션과 일치해야 함
                        .defaultSuccessUrl("/member/main", true) // 로그인 성공 시 리디렉션할 URL
                        .failureUrl("/member/login?error=true") // 로그인 실패 시 리디렉션할 URL
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/member/login")
                        .invalidateHttpSession(true)
                )
                // CSRF 설정 등
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 관리 설정
                .apply(new SessionManagementConfigurer<>())
                .sessionFixation(sessionFixationConfigurer ->
                        sessionFixationConfigurer.migrateSession()) // 세션 고정 보호
                .sessionConcurrency(concurrencyControlConfigurer ->
                        concurrencyControlConfigurer
                                .maximumSessions(1) // 동시 세션 제한
                                .maxSessionsPreventsLogin(true)); // 중복 로그인 방지

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HttpSessionEventPublisher 빈을 등록하여 세션 이벤트를 Spring Security가 인식
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}