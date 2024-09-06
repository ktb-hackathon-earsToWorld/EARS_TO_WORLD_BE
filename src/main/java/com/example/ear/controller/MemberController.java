package com.example.ear.controller;

import com.example.ear.domain.Member;
import com.example.ear.dto.request.JoinRequestDto;
import com.example.ear.dto.request.LoginRequestDto;
import com.example.ear.dto.response.JoinResponseDto;
import com.example.ear.dto.response.LoginResponseDto;
import com.example.ear.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;
    private static final String SESSION_CONST = "EARS_TO_EAR_SESSION";
    @PostMapping("/join")
    public ResponseEntity<JoinResponseDto> join(@RequestBody JoinRequestDto joinRequestDto) {
        return ResponseEntity.ok().body(memberService.join(joinRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto,
                                                  HttpServletRequest request) {
        Member loginMember = memberService.login(loginRequestDto);
        if (loginMember==null) {
            throw new RuntimeException("login 정보가 일치하지 않습니다.");
        }
        // 로그인 성공시
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_CONST , loginRequestDto.getLoginId());
        LoginResponseDto response = LoginResponseDto.builder()
                .loginId(loginRequestDto.getLoginId())
                .memberId(loginMember.getId())
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            return ResponseEntity.ok("Logged out successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active session found.");
        }
    }
}
