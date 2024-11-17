package com.zonainmueble.reports.conclusion.openai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompletionRequest {
  private String model;
  private List<Message> messages;
}
