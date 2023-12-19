package com.project.webProject.service;

import com.project.webProject.DTO.MemberDTO;
import com.project.webProject.model.Member;
import com.project.webProject.persistence.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailTokenService emailTokenService;

    @Autowired
    private EmailService emailService;


    @Value("${file.path}")
    private String filePathRoot;

    @Value("${encryption.secret-key}")
    private String secretKey;

    //파일 암호화
    private void encryptFile(Path inputFile, Path outputFile) throws Exception{
        Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,key);

        try(InputStream inputStream = Files.newInputStream(inputFile);
            OutputStream outputStream = Files.newOutputStream(outputFile)){

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1){
                byte[] encrypted = cipher.update(buffer, 0,length);
                if(encrypted != null){
                    outputStream.write(encrypted);
                }
            }
            byte[] encrypted = cipher.doFinal();
            if(encrypted != null){
                outputStream.write(encrypted);
            }
        }

    }

    //파일 복호화
    private void decryptFile(Path inputFile, Path outputFile) throws Exception {
        Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (InputStream inputStream = Files.newInputStream(inputFile);
             OutputStream outputStream = Files.newOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byte[] decrypted = cipher.update(buffer, 0, length);
                if (decrypted != null) {
                    outputStream.write(decrypted);
                }
            }
            byte[] decrypted = cipher.doFinal();
            if (decrypted != null) {
                outputStream.write(decrypted);
            }
        }
    }

    public byte[] loadProfileImage(int userId) throws Exception {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 없습니다."));

        Path encryptedFilePath = Paths.get(member.getProfile());
        Path decryptedFilePath = encryptedFilePath.getParent().resolve("dec_" + encryptedFilePath.getFileName());

        decryptFile(encryptedFilePath, decryptedFilePath); // 복호화

        return Files.readAllBytes(decryptedFilePath); // 파일을 byte 배열로 로드하여 반환
    }

    // 회원가입 시 이메일 인증 토큰 생성 및 발송
    public String sendEmailVerificationToken(int userId,String email) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다. " + email));;
        try {
            return emailTokenService.createEmailToken(member.getId(),email);
        } catch (Exception e) {
            throw new RuntimeException("이메일 인증 토큰 발송 실패", e);
        }
    }


    //회원가입
    public MemberDTO registerMember(MemberDTO memberDTO) {

        // 회원가입 로직
        if (memberDTO == null || memberDTO.getEmail() == null || memberDTO.getProfile() == null) {
            throw new IllegalArgumentException("필수 정보가 누락되었습니다.");
        }

        String email = memberDTO.getEmail();
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }

        String fileName = StringUtils.cleanPath(memberDTO.getProfile().getOriginalFilename());
        String uploadDir = filePathRoot + "/profile/" + email;

        // 파일 저장
        try {

            if (!fileName.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                // 파일이 이미 존재하면 기존 파일을 덮어쓰거나, 다른 이름으로 저장
                if (Files.exists(filePath)) {
                    // 현재 시간을 파일명에 추가하여 덮어쓰기를 방지
                    String newFileName = StringUtils.cleanPath(System.currentTimeMillis() + "_" + memberDTO.getProfile().getOriginalFilename());
                    filePath = uploadPath.resolve(newFileName);
                }
                try (InputStream inputStream = memberDTO.getProfile().getInputStream()) {
                    Path tempFile = Files.createTempFile(uploadPath, null, null);
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    encryptFile(tempFile, filePath); // 파일 암호화
                    Files.deleteIfExists(tempFile); // 임시 파일 삭제
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("파일을 저장하는데 실패했습니다.", e);
        }

        // 회원 정보 저장
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(memberDTO.getPassword()))
                .name(memberDTO.getName())
                .joinDay(LocalDateTime.now())
                .profile(uploadDir + "/" + fileName)
                .birthDay(memberDTO.getBirthDay())
                .role(0) // 기본적으로 '0'을 학생으로 설정
                .state(0) // 기본적으로 '0'을 미인증으로 설정
                .build();

        memberRepository.save(member);
        // 이메일 인증 토큰 생성 및 전송
        sendEmailVerificationToken(member.getId(),email);
        // DTO에 파일 경로 설정
        memberDTO.setId(member.getId());
        memberDTO.setProfile(null); // MultipartFile은 DTO에 포함되지 않도록 null 처리
        memberDTO.setProfilePath(uploadDir + "/" + fileName);

        return memberDTO;
    }


    //닉네임 중복체크
    private boolean checkName(String name){
        if(name == null || name.equals("")){
            throw new RuntimeException("MemberService.checkName() : name 값이 이상해요");
        }

        int count = memberRepository.findByName(name);
        if(count > 0){
            return false;
        }
        return true;
    }


    public MemberDTO viewProfile(String email){
        try{
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

            String status = "";
            if(member.getRole() == 0){
                status = "일반회원";
            }else if(member.getRole() == 1){
                status = "관리자";
            }


            byte[] profileImage = loadProfileImage(member.getId());
            String imageType = determineImageType(member.getProfile());
            String base64Image = Base64.getEncoder().encodeToString(profileImage);
            String profileImagePath = "data:" + imageType + ";base64," + base64Image;


            MemberDTO memDTO = MemberDTO.builder()
                    .email(member.getEmail())
                    .name(member.getName())
                    .joinDay(member.getJoinDay())
                    .birthDay(member.getBirthDay())
                    .profilePath(profileImagePath)
                    .status(status)
                    .build();

            return memDTO;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("MemberService.viewProfile() : 에러 발생.");
        }

    }

    public MemberDTO findByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // 여기서 member 엔티티를 MemberDTO 객체로 변환합니다.
        // 필요한 모든 정보를 MemberDTO 객체에 설정합니다.
        MemberDTO memberDTO = MemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .joinDay(member.getJoinDay())
                .profilePath(member.getProfile()) // 프로필 경로 설정
                .birthDay(member.getBirthDay())
                .role(member.getRole())
                .state(member.getState())
                .build();

        return memberDTO;
    }



    private String determineImageType(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.probeContentType(path);
    }

}
