package com.example.ear.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.VoiceId;



@Service
@Slf4j
public class PollyService {

    @Value(value = "${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Autowired
    private PollyClient pollyClient;

    public ResponseInputStream<SynthesizeSpeechResponse> synthesizeSpeech(String text, String outputFileName) {
        // 음성 합성 요청 생성
        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3) // 출력 형식 설정 (MP3 또는 OGG_VORBIS 등)
                .voiceId(VoiceId.SEOYEON) // 사용할 음성 설정
                .build();

        // Polly API 호출하여 음성 파일 생성
        return pollyClient.synthesizeSpeech(request);
        }
    }

