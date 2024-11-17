package com.zonainmueble.reports.pois;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoisResponse {
  private List<Poi> pois;
}
