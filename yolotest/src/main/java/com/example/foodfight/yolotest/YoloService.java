package com.example.foodfight.yolotest;


import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;



@Service
public class YoloService {
    private final String yoloApiUrl = "http://localhost:8001/predict";

    public YoloResult sendToYoloApi(MultipartFile file) throws IOException {
        String uploadDirPath = System.getProperty("user.dir") + "/uploads"; // 실행 위치 기준 절대경로
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File convFile = new File(uploadDirPath + "/" + file.getOriginalFilename());
        file.transferTo(convFile);

        // API 요청 준비
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(convFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<YoloResult> response = restTemplate.postForEntity(
                yoloApiUrl, requestEntity, YoloResult.class);

        return response.getBody();
    }
}
