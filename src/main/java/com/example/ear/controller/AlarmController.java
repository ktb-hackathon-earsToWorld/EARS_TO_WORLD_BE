package com.example.ear.controller;


import com.example.ear.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlarmController {
    private final AlarmService alarmService;
    /**
     * 메인 화면에 오면 바로 구독하도록 해당 API 호출 해야합니다.
     */
    @GetMapping(value = "/subscribe/{member-id}")
    public SseEmitter subscribe(@PathVariable("member-id") Long memberId) {
        return alarmService.connectNotification(memberId);
    }
}
