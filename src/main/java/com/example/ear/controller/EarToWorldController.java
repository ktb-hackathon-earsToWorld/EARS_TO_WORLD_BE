package com.example.ear.controller;

import com.example.ear.service.EarToWorldService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j @RequestMapping("/api")
public class EarToWorldController {
    private final EarToWorldService earToWorldService;

    /**
     * 이미지 파일 -> 음성 파일 (비즈니스 로직)
     */
    @PostMapping("/ear-to-world")
    public ResponseEntity<String> earToWorld(@RequestPart("imageFile") MultipartFile imageFile) throws IOException {
        return ResponseEntity.ok().body(earToWorldService.mainLogic(imageFile));
    }

    /**
     * 텍스트 -> 요약 -> 음성파일
     */
    @PostMapping("/ear-to-world/text")
    public ResponseEntity<String> earToWorldText(@RequestBody String text) throws IOException {
        return ResponseEntity.ok().body(earToWorldService.summaryText(text));
    }

    /**
     * 음성 파일을 보내는 API
     */
    @PostMapping("/audio")
    public ResponseEntity<String> sendAudioFile(@RequestParam("receiveLoginId") String receiveLoginId,
                                                @RequestBody String voiceRecordUrl) throws IOException {
        return ResponseEntity.ok().body(earToWorldService.sendRecordVoiceFile(voiceRecordUrl,receiveLoginId));
    }

}
