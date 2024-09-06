package com.example.ear.service;

import com.example.ear.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.management.RuntimeMBeanException;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    // 프론트에서 설정한 이벤트 이름
    private final static String ALARM_NAME = "alarm";

    private final EmitterRepository emitterRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter connectNotification(Long memberId) {

        log.info("connectNotification : " + memberId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberId, emitter);

        log.info("emitter.ref : " + emitter.hashCode());

        Optional<SseEmitter> sseEmitter = emitterRepository.get(memberId);
        log.info("sseEmitter : " + sseEmitter.get());

        emitter.onCompletion(() -> emitterRepository.delete(memberId));
        emitter.onTimeout(() -> emitterRepository.delete(memberId));

        try {
            log.info("send");
            // send() 로 이벤트 전송
            emitter.send(SseEmitter.event()
                    .id(ALARM_NAME)
                    // 프론트에서 설정한 이벤트와 같은 이벤트를 넣어줘야 한다.
                    .name(ALARM_NAME)
                    .data("connect completed"));
        } catch (IOException exception) {
            throw new RuntimeException("알람 연동에서 문제가 발생했습니다.");
        }
        return emitter;
    }

}
