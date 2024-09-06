package com.example.ear.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmitterRepository {
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, SseEmitter emitter) {
        log.info("emitterMap.length = {} " , emitterMap.size());
        for (String s : emitterMap.keySet()) {
            System.out.println(emitterMap.get(s));
        }
        final String key = getKey(memberId);
        emitterMap.put(key, emitter);
        return emitter;
    }

    public void delete(Long memberId) {
        emitterMap.remove(getKey(memberId));
    }

    public Optional<SseEmitter> get(Long memberId) {
        SseEmitter result = emitterMap.get(getKey(memberId));
        return Optional.ofNullable(result);
    }

    private String getKey(Long userId) {
        return "emitter:UID:" + userId;
    }

}

