package com.zonainmueble.reports.maps;

import java.util.ArrayList;
import java.util.List;

import com.zonainmueble.reports.geometry.Coordinate;
import com.zonainmueble.reports.geometry.Polygon;

import lombok.Data;

@Data
public class StyledPolygon {
  private String color;
  private Integer weight;
  private String fillColor;

  private List<Coordinate> coordinates;

  public StyledPolygon() {
    this.weight = 4;
    this.color = "#0000FF77";
    this.coordinates = new ArrayList<>();
  }

  public StyledPolygon(List<Coordinate> coordinates) {
    this();
    this.coordinates = coordinates;
  }

  public StyledPolygon(Polygon polygon) {
    this();
    this.coordinates = polygon.getCoordinates();
  }

  public StyledPolygon(Polygon polygon, String color) {
    this();
    this.coordinates = polygon.getCoordinates();
    this.color = color;
  }
}
