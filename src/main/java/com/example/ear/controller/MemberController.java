package com.example.ear.controller;

import com.example.ear.domain.Member;
import com.example.ear.dto.request.JoinRequestDto;
import com.example.ear.dto.request.LoginRequestDto;
import com.example.ear.dto.response.JoinResponseDto;
import com.example.ear.dto.response.LoginResponseDto;
import com.example.ear.global.SessionConst;
import com.example.ear.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        session.setAttribute(SessionConst.SESSION_CONST, loginRequestDto.getLoginId());
        LoginResponseDto response = LoginResponseDto.builder()
                .loginId(loginRequestDto.getLoginId())
                .memberId(loginMember.getId())
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request , HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            // 쿠키 무효화
            Cookie cookie = new Cookie("JSESSIONID", null); // 'JSESSIONID'는 세션 쿠키 이름
            cookie.setPath("/"); // 애플리케이션 루트 경로에 설정
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0); // 쿠키 만료 시간 0으로 설정하여 쿠키를 무효화
            response.addCookie(cookie);

            return ResponseEntity.ok("Logged out successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active session found.");
        }
    }
}
