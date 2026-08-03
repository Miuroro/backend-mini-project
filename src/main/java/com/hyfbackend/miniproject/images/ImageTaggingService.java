package com.hyfbackend.miniproject.images;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ImageTaggingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value ("${gemini.api-key}")
    private String geminiApiKey;

    public ImageTaggingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent")
                .build();
    }

    public Map<String, Object> generateTags(MultipartFile imageFile) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            String mimeType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";
            String dataUri = "data:" + mimeType + ";base64," + base64Image;

            String promptText = """
                Analyze this image and return a JSON object with exactly three keys:
                1. "objects": An array of strings representing things in the picture.
                2. "tags": An array of strings for setting, weather, or mood.
                3. "colors": An array of up to 3 prominent colors chosen strictly from: red, orange, yellow, green, blue, purple, pink, brown, black, grey, white.
                
                Return ONLY valid raw JSON matching this format without markdown code blocks:
                {
                  "objects": ["tree", "sun"],
                  "tags": ["sunny"],
                  "colors": ["green", "blue"]
                }
                """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", promptText),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType,
                                            "data", base64Image
                                    ))
                            ))
                    )
            );

            Map apiResponse = restClient.post()
                    .uri("?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (apiResponse == null || !apiResponse.containsKey("candidates")) { // to check console log
                return fallbackTags();
            }

            List<Map> candidates = (List<Map>) apiResponse.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String rawJsonContent = ((String) parts.get(0).get("text")).trim();

            if (rawJsonContent.startsWith("```json")) {
                rawJsonContent = rawJsonContent.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");
            } else if (rawJsonContent.startsWith("```")) {
                rawJsonContent = rawJsonContent.replaceAll("^```\\s*", "").replaceAll("\\s*```$", "");
            }

            return objectMapper.readValue(rawJsonContent, Map.class);

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTags();
        }
    }

    private Map<String, Object> fallbackTags() {
        return Map.of(
                "objects", List.of(),
                "tags", List.of("tagging_failed"),
                "colors", List.of()
        );
    }
}