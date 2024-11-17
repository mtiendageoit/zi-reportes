package com.zonainmueble.reports.conclusion.openai;

import java.util.List;
import lombok.Data;

@Data
public class CompletionResponse {
  private List<Choice> choices;
}
