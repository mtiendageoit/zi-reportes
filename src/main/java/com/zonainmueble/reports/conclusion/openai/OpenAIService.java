package com.zonainmueble.reports.conclusion.openai;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.services.ConclusionService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class OpenAIService implements ConclusionService {

  @Value("${apis.openai.key}")
  private String key;

  @Value("${apis.openai.completions.url}")
  private String url;

  @Value("${apis.openai.completions.model}")
  private String model;

  private final RestTemplate restTemplate;

  public OpenAIService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public String conclusion(String systemPromt, String message) {
    Message system = new Message(MessageRole.system.name(), systemPromt);
    Message user = new Message(MessageRole.user.name(), message);

    CompletionRequest request = new CompletionRequest(model, List.of(system, user));

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(this.key);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CompletionRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<CompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
          CompletionResponse.class);

      return response.getBody().getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("\n\nurl: {}\request: {}\n\n", url, request);
      throw new RuntimeException("Error al obtener el completion desde OpenAI");
    }
  }

}
