package com.zonainmueble.reports.geometry;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Polygon {
  private String wkt;
  private List<Coordinate> coordinates;

  public Polygon() {
    this.coordinates = new ArrayList<>();
  }

  public Polygon(List<Coordinate> coordinates) {
    this.coordinates = coordinates;
  }
}
