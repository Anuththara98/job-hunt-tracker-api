package com.anuththara.jobhunttracker.coverletter.anthropic;

import com.anuththara.jobhunttracker.coverletter.CoverLetterGenerationException;
import com.anuththara.jobhunttracker.coverletter.anthropic.dto.AnthropicRequest;
import com.anuththara.jobhunttracker.coverletter.anthropic.dto.AnthropicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
public class AnthropicClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicClient.class);
    private static final String BASE_URL = "https://api.anthropic.com";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient webClient;
    private final String model;
    private final int maxTokens;

    public AnthropicClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.anthropic.api-key}") String apiKey,
            @Value("${app.anthropic.model}") String model,
            @Value("${app.anthropic.max-tokens}") int maxTokens) {
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public String generateCoverLetter(String prompt) {
        AnthropicRequest request = new AnthropicRequest(
                model,
                maxTokens,
                List.of(new AnthropicRequest.Message("user", prompt))
        );

        try {
            AnthropicResponse response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AnthropicResponse.class)
                    .block();

            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new CoverLetterGenerationException("Empty response received from Anthropic API");
            }

            return response.content().stream()
                    .filter(block -> "text".equals(block.type()))
                    .findFirst()
                    .map(AnthropicResponse.ContentBlock::text)
                    .orElseThrow(() -> new CoverLetterGenerationException("No text content in Anthropic response"));

        } catch (WebClientResponseException e) {
            log.error("Anthropic API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CoverLetterGenerationException("Anthropic API returned " + e.getStatusCode());
        }
    }
}