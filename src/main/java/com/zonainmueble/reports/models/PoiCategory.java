package com.zonainmueble.reports.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PoiCategory {
  public static final String DEFAULT_ICON = "icon-pois-generic.svg";
  public static final String DEFAULT_COLOR = "#000000";

  private String key;
  private String name;
  private String icon;
  private String color;
  private Integer count;

  public static PoiCategory GASOLINERA() {
    return PoiCategory.builder()
        .key("700-7600-0116")
        .color("#70AD47")
        .build();
  }

  public static PoiCategory ESTACIONAMIENTO() {
    return PoiCategory.builder()
        .key("800-8500-0178")
        .color("#008FFF")
        .build();
  }
}
