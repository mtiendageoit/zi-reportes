package com.zonainmueble.reports.pois.heremaps;

import java.util.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HereMapsPoisResponse {
  private Integer limit;
  private List<Poi> items;
}
