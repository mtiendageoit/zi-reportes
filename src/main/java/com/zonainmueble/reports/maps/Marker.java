package com.zonainmueble.reports.maps;

import com.zonainmueble.reports.geometry.Coordinate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marker {
  private String color;
  private MarkerSize size;
  private Coordinate coordinate;

  public Marker(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public Marker(Coordinate coordinate, MarkerSize size) {
    this.coordinate = coordinate;
    this.size = size;
  }

  public enum MarkerSize {
    tiny, mid, small, normal
  }

}
