package com.zonainmueble.reports.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryData {
  private String category;
  private Double value;
  private String color;
}
