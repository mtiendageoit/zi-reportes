package com.zonainmueble.reports.conclusion.openai;

import lombok.*;

@Data
@AllArgsConstructor
public class Message {
  private String role;
  private String content;
}
