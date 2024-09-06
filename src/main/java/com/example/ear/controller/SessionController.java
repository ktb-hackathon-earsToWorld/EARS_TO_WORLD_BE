package com.example.ear.controller;

import com.example.ear.domain.Member;
import com.example.ear.global.SessionConst;
import com.example.ear.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    private final MemberRepository memberRepository;

    @GetMapping("/check-session")
    public ResponseEntity<Boolean> checkSession(HttpSession httpSession) {
        String loginId = (String) httpSession.getAttribute(SessionConst.SESSION_CONST);
        Member member = memberRepository.findByLoginId(loginId);
        boolean isLoggedIn = true;
        if (member == null) {
            isLoggedIn = false;
        }
        return ResponseEntity.ok().body(isLoggedIn);
    }
}
