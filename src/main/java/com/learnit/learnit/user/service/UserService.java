package com.learnit.learnit.user.service;

import com.learnit.learnit.user.dto.LoginRequestDTO;
import com.learnit.learnit.user.dto.SignupRequestDTO;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public Long signup(SignupRequestDTO signupRequest) {
        // 비밀번호 확인
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 사용자명 중복 확인
        if (userMapper.existsByUsername(signupRequest.getName())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }

        // 이메일 중복 확인
        if (userMapper.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // UserDTO 생성
        UserDTO userDTO = new UserDTO();
        userDTO.setName(signupRequest.getName());
        userDTO.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userDTO.setEmail(signupRequest.getEmail());
        userDTO.setNickname(signupRequest.getNickname());
        userDTO.setPhone(signupRequest.getPhone());
        userDTO.setRegion(signupRequest.getRegion());
        userDTO.setStatus("SIGNUP_PENDING"); // 회원가입 시 가입 미완료 상태로 설정
        userDTO.setProvider("LOCAL");

        // 사용자 저장
        userMapper.insertUser(userDTO);
        return userDTO.getUserId();
    }

    /**
     * 로그인
     */
    public Optional<UserDTO> login(LoginRequestDTO loginRequest) {
        UserDTO user = userMapper.selectUserByEmail(loginRequest.getEmail());
        
        if (user == null) {
            return Optional.empty();
        }
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return Optional.empty();
        }

        // BANNED 또는 DELETE 상태만 로그인 불가
        String status = user.getStatus();
        if ("BANNED".equals(status) || "DELETE".equals(status)) {
            throw new IllegalStateException("접근이 제한된 계정입니다.");
        }

        // SIGNUP_PENDING, PROFILE_REQUIRED, ACTIVE 상태는 로그인 허용
        // 접근 제어는 Interceptor에서 처리
        return Optional.of(user);
    }

    /**
     * 이메일 인증 번호 생성 및 저장
     */
    @Transactional
    public String generateEmailVerificationCode(String email) {
        // 6자리 랜덤 코드 생성
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        
        // DB에 저장
        userMapper.saveEmailVerificationCode(email, code);
        
        return code;
    }

    /**
     * 이메일 인증 번호 검증
     */
    public boolean verifyEmailCode(String email, String code) {
        return userMapper.verifyEmailCode(email, code);
    }

    /**
     * 이메일로 사용자 조회
     */
    public Optional<UserDTO> findByEmail(String email) {
        UserDTO user = userMapper.selectUserByEmail(email);
        return Optional.ofNullable(user);
    }

    /**
     * 사용자 ID로 조회
     */
    public Optional<UserDTO> findById(Long userId) {
        UserDTO user = userMapper.selectUserById(userId);
        return Optional.ofNullable(user);
    }

    /**
     * 사용자명 중복 확인
     */
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
}

