package com.zonainmueble.reports.maps;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.geometry.Coordinate;
import com.zonainmueble.reports.services.MapImagesService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class GoogleMapsService implements MapImagesService {

  @Value("${apis.google.maps.key}")
  private String key;

  @Value("${apis.google.maps.static.url}")
  private String staticUrl;

  private final RestTemplate restTemplate;

  public GoogleMapsService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public byte[] image(MapImageRequest input) {
    String url = buildUrl(input);

    try {
      ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
      return response.getBody();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("input: {}, url: {}", input, url);
      throw new RuntimeException("Error al obtener la imagen desde google maps static");
    }
  }

  private String buildUrl(MapImageRequest input) {
    StringBuilder urlBuilder = new StringBuilder(staticUrl).append("?");
    urlBuilder.append("format=png");
    urlBuilder.append("&maptype=").append(input.getMapType().name());
    urlBuilder.append("&size=").append(input.getWidth()).append("x").append(input.getHeight());

    if (input.getMarkers() != null && !input.getMarkers().isEmpty()) {
      urlBuilder.append("&").append(markersToStr(input.getMarkers()));
    }
    if (input.getPolygons() != null && !input.getPolygons().isEmpty()) {
      urlBuilder.append("&").append(pathsToStr(input.getPolygons()));
    }

    urlBuilder.append("&key=").append(key);

    if (input.getMapId() != null) {
      urlBuilder.append("&map_id=").append(input.getMapId());
    }

    return urlBuilder.toString();
  }

  private String pathsToStr(List<StyledPolygon> polygons) {
    return polygons.stream().map(poly -> pathToStr(poly)).collect(Collectors.joining("&"));
  }

  private String pathToStr(StyledPolygon polygon) {

    String coords = polygon.getCoordinates().stream()
        .map(coord -> String.format("%f,%f", coord.getLatitude(), coord.getLongitude()))
        .collect(Collectors.joining("|"));

    Integer weight = polygon.getWeight();
    String color = polygon.getColor().replace("#", "0x");

    StringBuilder builder = new StringBuilder("path=")
        .append("color:").append(color).append("|")
        .append("weight:").append(weight).append("|");

    if (polygon.getFillColor() != null) {
      String fillColor = polygon.getFillColor().replace("#", "0x");
      builder.append("fillcolor:").append(fillColor).append("|");
    }

    return builder.append(coords).toString();
  }

  private String markersToStr(List<Marker> markers) {
    return markers.stream().map(marker -> markerToStr(marker)).collect(Collectors.joining("&"));
  }

  private String markerToStr(Marker marker) {
    Coordinate coord = marker.getCoordinate();
    StringBuilder builder = new StringBuilder("markers=");

    if (marker.getColor() != null) {
      builder.append("color:").append(marker.getColor().replace("#", "0x")).append("|");
    }

    if (marker.getSize() != null) {
      builder.append("size:").append(marker.getSize().name()).append("|");
    }

    builder.append(coord.getLatitude()).append(",").append(coord.getLongitude());

    return builder.toString();
  }

}
