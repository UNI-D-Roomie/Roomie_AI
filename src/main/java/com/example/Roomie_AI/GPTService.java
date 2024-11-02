package com.example.Roomie_AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

@Service
public class GPTService {

    @Value("${openai.api_key}")
    private String apiKey;

    @Value("${openai.api_url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public CompareResponseDto compareImages(String compareType, String beforeUrl, String afterUrl) throws IOException {
        // Download and encode both images to Base64
        String beforeImage = encodeImageFromUrl(beforeUrl);
        String afterImage = encodeImageFromUrl(afterUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");

        // Set the prompt based on compareType
        String prompt;
        if ("ROOM".equalsIgnoreCase(compareType)) {
            prompt = "두 이미지를 청결함과 정돈됨의 관점에서 비교해주세요. " +
                    "'청소 전' 이미지가 '청소 후' 이미지와 거의 비슷하게 깨끗하면 100점을, 매우 더럽다면 5점으로, 100점부터 5점 사이의 점수로 점수를 부여해주세요" +
                    "첫 줄에는 숫자만 적고, 둘째 줄에는 한국어로 '청소 전 이미지'라는 말 없이 청소 전 상태에 대한 평가만 간략히 적어주세요.";
        } else if ("WASH".equalsIgnoreCase(compareType)) {
            prompt = "두 이미지를 설거지가 잘된 정도의 관점에서 비교해주세요. " +
                    "'설거지 전' 이미지가 '설거지 후' 이미지와 거의 비슷하게 설거지가 매우 잘되었으면 100점을, 설거지 상태가 매우 불량하면 5점으로, 100점부터 5점 사이의 점수로 점수를 부여해주세요" +
                    "첫 줄에는 숫자만 적고, 둘째 줄에는 한국어로 '설거지 전 이미지'라는 말 없이 설거지 전 상태에 대한 평가만 간략히 적어주세요.";
        } else {
            return new CompareResponseDto(0.0, "Invalid comparison type. Please use 'ROOM' or 'WASH'.");
        }

        JSONObject messageContent = new JSONObject().put("type", "text").put("text", prompt);

        JSONObject imageUrlContent1 = new JSONObject()
                .put("type", "image_url")
                .put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + beforeImage));

        JSONObject imageUrlContent2 = new JSONObject()
                .put("type", "image_url")
                .put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + afterImage));

        JSONObject messages = new JSONObject()
                .put("role", "user")
                .put("content", new JSONObject[] { messageContent, imageUrlContent1, imageUrlContent2 });

        JSONObject requestBody = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONObject[] { messages })
                .put("max_tokens", 300);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);


        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
        System.out.println("Response: " + response.getBody());

        JSONObject jsonResponse = new JSONObject(response.getBody());
        String gptResponse = jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");


        return parseGPTResponse(gptResponse);
    }

    private String encodeImageFromUrl(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            byte[] imageBytes = in.readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }


    private CompareResponseDto parseGPTResponse(String gptResponse) {
        Double score = 0.0;
        String comment = "";

        try {

            String[] lines = gptResponse.split("\n");

            // 첫번째줄
            if (lines.length > 0) {
                score = Double.parseDouble(lines[0].trim());
            }

            // 두번째줄
            if (lines.length > 1) {
                comment = lines[1].trim();
            } else {
                comment = "No comment provided in the response.";
            }
        } catch (Exception e) {
            comment = "Error parsing GPT response: " + e.getMessage();
        }

        return new CompareResponseDto(score, comment);
    }

}
