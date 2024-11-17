package com.zonainmueble.reports.maps;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapImageRequest {
  private int width;
  private int height;

  private String mapId;
  private MapType mapType;

  private List<Marker> markers;
  private List<StyledPolygon> polygons;

  public MapImageRequest(int width, int height, MapType mapType, Marker marker) {
    this.width = width;
    this.height = height;
    this.mapType = mapType;
    this.markers = List.of(marker);
  }

  public MapImageRequest(int width, int height, MapType mapType, List<Marker> markers) {
    this.width = width;
    this.height = height;
    this.mapType = mapType;
    this.markers = markers;
  }

  public MapImageRequest(int width, int height, MapType mapType, Marker marker, StyledPolygon polygon) {
    this.width = width;
    this.height = height;
    this.mapType = mapType;
    this.markers = List.of(marker);
    this.polygons = List.of(polygon);
  }

  public MapImageRequest(int width, int height, MapType mapType, Marker marker, List<StyledPolygon> polygons) {
    this.width = width;
    this.height = height;
    this.mapType = mapType;
    this.markers = List.of(marker);
    this.polygons = polygons;
  }

  public MapImageRequest(int width, int height, MapType mapType, Marker marker, List<StyledPolygon> polygons,
      String mapId) {
    this.width = width;
    this.height = height;
    this.mapType = mapType;
    this.markers = List.of(marker);
    this.polygons = polygons;
    this.mapId = mapId;
  }
}
