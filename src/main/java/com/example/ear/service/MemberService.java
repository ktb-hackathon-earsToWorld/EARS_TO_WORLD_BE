package com.example.ear.service;

import com.example.ear.domain.Member;
import com.example.ear.dto.request.JoinRequestDto;
import com.example.ear.dto.request.LoginRequestDto;
import com.example.ear.dto.response.JoinResponseDto;
import com.example.ear.dto.response.LoginResponseDto;
import com.example.ear.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public JoinResponseDto join(JoinRequestDto joinRequestDto) {
        Member member = Member.builder()
                .loginId(joinRequestDto.getLoginId())
                .password(joinRequestDto.getPassword())
                .build();
        Member joinMember = memberRepository.save(member);
        return JoinResponseDto.builder()
                .loginId(joinMember.getLoginId())
                .memberId(joinMember.getId())
                .build();
    }

    public Member login(LoginRequestDto loginRequestDto) {
        Member findMember = memberRepository.findByLoginId(loginRequestDto.getLoginId());
        if (findMember.getPassword().equals(loginRequestDto.getPassword())) {
            return findMember;
        }
        return null;
    }
}
