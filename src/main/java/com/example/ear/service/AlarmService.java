package com.example.ear.service;

import com.example.ear.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.management.RuntimeMBeanException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    // 프론트에서 설정한 이벤트 이름
    private final static String ALARM_NAME = "alarm";

    private final EmitterRepository emitterRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

/*    public void send(AlarmType type, AlarmArgs args, Integer receiverId) {
        UserEntity userEntity = userEntityRepository
                .findById(receiverId)
                .orElseThrow(()
                        -> new SimpleSnsApplicationException(ErrorCode.USER_NOT_FOUND));
        AlarmEntity entity = AlarmEntity.of(type, args, userEntity);
        alarmEntityRepository.save(entity);
        emitterRepository.get(receiverId).ifPresentOrElse(it -> {
                    try {
                        it.send(SseEmitter.event()
                                .id(entity.getId().toString())
                                .name(ALARM_NAME)
                                .data(new AlarmNoti()));
                    } catch (IOException exception) {
                        emitterRepository.delete(receiverId);
                        throw new SimpleSnsApplicationException(ErrorCode.NOTIFICATION_CONNECT_ERROR);
                    }
                },
                // 유저가 브라우저에 접속하지 않은 상황을 수 있기 때문?
                () -> log.info("No emitter founded")
        );
    }*/


    public SseEmitter connectNotification(Long memberId) {

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(memberId));
        emitter.onTimeout(() -> emitterRepository.delete(memberId));

        try {
            log.info("send");
            // send() 로 이벤트 전송
            emitter.send(SseEmitter.event()
                    .id("id")
                    // 프론트에서 설정한 이벤트와 같은 이벤트를 넣어줘야 한다.
                    .name(ALARM_NAME)
                    .data("connect completed"));
        } catch (IOException exception) {
            throw new RuntimeException("알람 연동에서 문제가 발생했습니다.");
        }
        return emitter;
    }

}
