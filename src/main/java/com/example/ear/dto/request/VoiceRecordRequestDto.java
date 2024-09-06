package com.example.ear.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter
public class VoiceRecordRequestDto {
    private String voiceRecordUrl;
    private Long memberId;

    public static VoiceRecordRequestDto of(String voiceRecordUrl , Long memberId) {
        return VoiceRecordRequestDto.builder()
                .voiceRecordUrl(voiceRecordUrl)
                .memberId(memberId)
                .build();
    }
}
