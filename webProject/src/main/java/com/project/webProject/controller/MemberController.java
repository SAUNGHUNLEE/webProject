package com.project.webProject.controller;

import com.project.webProject.DTO.MemberDTO;
import com.project.webProject.model.EmailToken;
import com.project.webProject.model.Member;
import com.project.webProject.persistence.MemberRepository;
import com.project.webProject.service.EmailService;
import com.project.webProject.service.EmailTokenService;
import com.project.webProject.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/verify_email")
    public String showVerificationPage() {
        return "verifyEmail"; // 이메일 인증 페이지의 뷰 이름
    }

    @PostMapping("/verify_email")
    public String verifyEmail(@RequestParam("verificationCode") String verificationCode,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean isVerified = emailService.verifyEmail(verificationCode);
            if (isVerified) {
                redirectAttributes.addFlashAttribute("successMessage", "이메일 인증이 완료되었습니다.");
                return "redirect:/member/login";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "이메일 인증에 실패했습니다.");
                return "redirect:/member/verify_email";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이메일 인증 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/member/verify_email";
        }
    }



    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("memberDTO", new MemberDTO());
        return "registerMember";
    }


    @PostMapping("/join")
    public String registerMember(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        // 이메일 인증 토큰을 보내고 성공한 경우에만 인증 페이지로 리디렉션
        try {
            // 회원가입 처리
            MemberDTO registeredMember = memberService.registerMember(memberDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입에 성공했습니다. 이메일을 확인하여 인증해주세요.");
            return "redirect:/member/verify_email";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입은 완료되었습니다. 이메일 인증 이메일 전송에 실패했습니다.");
            return "redirect:/member/join";
        }
    }



    @ResponseBody
    @PostMapping("/send_email_token")
    public ResponseEntity<?> sendEmailToken(@RequestParam int userId, @RequestParam String email) {
        try {
            // 인증 이메일을 보내고 성공 시에만 리디렉션
            String token = memberService.sendEmailVerificationToken(userId, email);
            // 인증 이메일을 성공적으로 보낸 경우, 인증 번호 입력 페이지로 리디렉션
            return ResponseEntity.status(HttpStatus.FOUND) // 302 리디렉션 상태 코드
                    .header("Location", "/member/verify_email") // 인증 번호 입력 페이지 경로
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("인증번호 전송에 실패했습니다.");
        }
    }


    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("member") MemberDTO member, RedirectAttributes redirectAttributes) {
        try {

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
        return "redirect:/main";
    }





    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
       //세션 무효화
        request.getSession().invalidate();
        return "redirect:/login?logout";
    }


    @GetMapping("/mypage")
    public String myPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // 인증되지 않은 경우 로그인 페이지로 리디렉트
        }

        // UserDetails 객체에서 사용자의 이메일 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // 여기서 getUsername()은 사용자의 이메일을 반환

        // 이메일을 사용하여 사용자 프로필 가져오기
        MemberDTO profile = memberService.viewProfile(email); // 프로필 정보를 가져옴
        model.addAttribute("memberDTO", profile); // 모델에 회원 DTO를 추가

        return "mypage"; // mypage.html 뷰 이름을 반환
    }


    @GetMapping("/main")
    public String main(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // 인증되지 않은 경우 로그인 페이지로 리디렉트
        }

        // UserDetails 객체에서 사용자의 이메일 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // 여기서 getUsername()은 사용자의 이메일을 반환

        // 이메일을 사용하여 사용자 프로필 가져오기
        MemberDTO profile = memberService.viewProfile(email); // 프로필 정보를 가져옴
        model.addAttribute("memberDTO", profile); // 모델에 회원 DTO를 추가

        return "main"; // mypage.html 뷰 이름을 반환
    }



    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<MemberDTO> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        MemberDTO profile = memberService.viewProfile(email);
        return ResponseEntity.ok(profile);
    }


}
