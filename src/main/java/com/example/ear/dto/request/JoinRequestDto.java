package com.example.ear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor @NoArgsConstructor
public class JoinRequestDto {
    private String loginId;
    private String password;
}
