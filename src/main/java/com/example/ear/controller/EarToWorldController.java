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
    public ResponseEntity<byte[]> earToWorld(@RequestPart("imageFile") MultipartFile imageFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "audio/mpeg"); // 적절한 MIME 타입 설정
        // inline 은 다운로드 하지 않고, 페이지 내에서 바로 재생 처리
        headers.add("Content-Disposition", "inline; filename=speech.mp3");
        return new ResponseEntity<>(earToWorldService.mainLogic(imageFile).toByteArray(),headers, HttpStatus.OK);
    }

    /**
     * 음성 파일을 보내는 API
     */
    @PostMapping("/audio/{receive-id}/out")
    public ResponseEntity<String> sendAudioFile(@RequestPart("file") MultipartFile file,
                                                   @PathVariable("receive-id") Long receiveMemberId) throws IOException {
        byte[] voiceRecordFile = file.getBytes();
        return ResponseEntity.ok().body(earToWorldService.sendRecordVoiceFile(voiceRecordFile,receiveMemberId));
    }

    /**
     * 음성 파일을 받는 API
     */
/*    @GetMapping("/audio/{receive-id}/in")
    public ResponseEntity<String> receiveAudioFile(@RequestPart("file") MultipartFile file,
                                                   @PathVariable("receive-id") Long receiveMemberId,
                                                   HttpServletRequest httpServletRequest) throws IOException {
        byte[] voiceRecordFile = file.getBytes();
        earToWorldService.sendRecordVoiceFile(voiceRecordFile,receiveMemberId);
    }*/


}
