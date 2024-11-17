package com.zonainmueble.reports.geometry;

import lombok.*;

@Data
@AllArgsConstructor
public class Extent {
  private Coordinate southWest; // south-west
  private Coordinate northEast; // north-east

  public Extent(double latitudeSouth, double longitudeWest, double latitudeNorth, double longitudeEast) {
    this.southWest = new Coordinate(latitudeSouth, longitudeWest);
    this.northEast = new Coordinate(latitudeNorth, longitudeEast);
  }
}
