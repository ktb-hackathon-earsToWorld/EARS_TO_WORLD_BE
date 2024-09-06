package com.example.ear.service;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.ear.domain.Member;
import com.example.ear.dto.request.ChatGptRequest;
import com.example.ear.dto.request.VoiceRecordRequestDto;
import com.example.ear.repository.EmitterRepository;
import com.example.ear.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.*;
import java.net.URL;
import java.util.UUID;

import static com.example.ear.dto.request.ChatGptRequest.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EarToWorldService {

    private static final String COMMNET = "위 내용을 장애인과 노인들이 이해할 수 있도록 어려운 말들은 " +
            "부가설명을 해주고 전체적인 내용을 쉽고 자세하게 구어체로 요약해줘";

    // TODO: 9/6/24 localhost -> 배포된 환경으로 변경해야합니다.
    private static final String SEND_ENDPOINT = "http://localhost:8080/api/audio";

    private final static String ALARM_NAME = "alarm";

    @Value("${cloud.aws.credentials.access-key}")
    private String s3AccessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String s3SecretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${naver.service.secretKey}")
    private String naverOcrSecretKey;

    private final ChatGPTService chatGPTService;
    private final NaverOrcApiService naverOrcApiService;
    private final MemberRepository memberRepository;
    private final AmazonS3Client amazonS3Client;
    private final PollyService pollyService;
    private final RestTemplate restTemplate;
    private final AlarmService alarmService;
    private final EmitterRepository emitterRepository;



    private S3Client s3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public String summaryText(String text) throws IOException {
        text = text + "\n" + COMMNET;
        String summaryResultFromChatGPT = chatGPTService.prompt(SummaryDto.of(text));
        ResponseInputStream<SynthesizeSpeechResponse> resultFromAwsPolly = pollyService
                .synthesizeSpeech(summaryResultFromChatGPT, "earToWorld.mp3");

        ByteArrayOutputStream byteArrayOutputStream = dataToByteArray(resultFromAwsPolly);
        byte[] result = byteArrayOutputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(result);

        // result 를 S3 에 저장
        // S3 에 저장
        String storeFileName = UUID.randomUUID().toString() + ".mp3";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/mpeg");
        metadata.setContentLength(result.length);
        amazonS3Client.putObject(bucket, storeFileName, inputStream, metadata);

        return storeFileName;
    }

    public String mainLogic(MultipartFile imageFile) throws IOException {
        // 1. S3 에 이미지 파일 저장 후 이미지 URL 가져오기
        String imageUrl = getImageUrlFromS3(imageFile);

        // 2. 가져온 이미지 URL 을 네이버 OCR 에 보내서 내용 추출하기
        String extractContent = naverOrcApiService.callApi("POST", imageUrl, naverOcrSecretKey, "jpg");

        // 2-1) 추출하기 위해 프롬프트에 내용을 추가
        extractContent = extractContent + "\n" + COMMNET;

        // 3. 추출한 내용을 ChatGPT 에게 보내서 요약본 요청하기
        String summaryResultFromChatGPT = chatGPTService.prompt(SummaryDto.of(extractContent));

        // 4. 받아온 요약본을 AWS Polly 에 보내서 음성 파일 가져오기
        ResponseInputStream<SynthesizeSpeechResponse> resultFromAwsPolly = pollyService
                .synthesizeSpeech(summaryResultFromChatGPT, "earToWorld.mp3");

        ByteArrayOutputStream byteArrayOutputStream = dataToByteArray(resultFromAwsPolly);
        byte[] result = byteArrayOutputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(result);

        // result 를 S3 에 저장
        // S3 에 저장
        String storeFileName = UUID.randomUUID().toString() + ".mp3";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/mpeg");
        metadata.setContentLength(imageFile.getSize());
        amazonS3Client.putObject(bucket, storeFileName, inputStream, metadata);

        return storeFileName;
    }

    /**
     * 음성 파일을 사용자에게 보내는 로직
     */

    public String sendRecordVoiceFile(String voiceRecordUrl , String receiveLoginId) throws IOException {
        Member member = memberRepository.findByLoginId(receiveLoginId);

        if (member == null) {
            throw new RuntimeException("존재하지 않는 login Id 입니다.");
        }

        // SSE 로 알람 보내기
        emitterRepository.get(member.getId()).ifPresentOrElse(it -> {
                    try {
                        log.info("send 시작");
                        it.send(SseEmitter.event()
                                .id(member.getId().toString())
                                .name(ALARM_NAME)
                                .data(VoiceRecordRequestDto.of(voiceRecordUrl,member.getId())));
                        log.info("send 끝");
                    } catch (IOException exception) {
                        emitterRepository.delete(member.getId());
                        throw new RuntimeException("알람 로직에서 오류가 발생했습니다.");
                    }
                },
                () -> log.info("No emitter founded")
        );
        return "SSE 를 통한 알람 전송 성공";
    }

    private static ByteArrayOutputStream dataToByteArray(ResponseInputStream<SynthesizeSpeechResponse> resultFromAwsPolly) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2 * 1024];
        int bytesRead;
        while ((bytesRead = resultFromAwsPolly.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream;
    }

    private String getImageUrlFromS3(MultipartFile imageFile) throws IOException {
        //파일의 원본 이름
        String originalFileName = imageFile.getOriginalFilename();

        //DB에 저장될 파일 이름
        String storeFileName = createStoreFileName(originalFileName);

        // S3 에 저장
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imageFile.getContentType());
        metadata.setContentLength(imageFile.getSize());
        amazonS3Client.putObject(bucket, storeFileName, imageFile.getInputStream(), metadata);

        return "https://like-lion-dynamo.s3.amazonaws.com/" + storeFileName;
    }

    private String getVoiceRecordUrlFromS3(MultipartFile voiceFile) throws IOException {
        //파일의 원본 이름
        String originalFileName = voiceFile.getOriginalFilename();

        //DB에 저장될 파일 이름
        String storeFileName = createStoreFileName(originalFileName);

        // S3 에 저장
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(voiceFile.getContentType());
        metadata.setContentLength(voiceFile.getSize());
        amazonS3Client.putObject(bucket, storeFileName, voiceFile.getInputStream(), metadata);

        return "https://like-lion-dynamo.s3.amazonaws.com/" + storeFileName;
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
    /**
     * 파일 확장자를 추출하기 위해 만든 메서드
     */
    private String extractExt(String originalFilename) {
        int post = originalFilename.lastIndexOf(".");
        return originalFilename.substring(post + 1);
    }



}
