package com.zonainmueble.reports.pois.heremaps;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.geometry.Coordinate;
import com.zonainmueble.reports.geometry.Extent;
import com.zonainmueble.reports.pois.PoisRequest;
import com.zonainmueble.reports.pois.PoisResponse;
import com.zonainmueble.reports.services.PoisService;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HereMapsPoisService implements PoisService {
  @Value("${apis.here.maps.key}")
  private String key;

  @Value("${apis.here.maps.browse.url}")
  private String browseUrl;

  private final RestTemplate restTemplate;

  @Override
  @Retry(name = "hereMapsPoisRequest", fallbackMethod = "poisFallback")
  public PoisResponse pois(PoisRequest input) {
    String url = buildPoisUrl(input);

    try {
      ResponseEntity<HereMapsPoisResponse> response = restTemplate.getForEntity(url, HereMapsPoisResponse.class);

      List<Poi> pois = response.getBody().getItems().stream()
          .filter(item -> item.getPrimaryCategory().isPresent() && item.getResultType().equalsIgnoreCase("place"))
          .collect(Collectors.toList());
      response.getBody().setItems(pois);

      return responseFrom(response.getBody());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("url: {}, input: {}", url, input);
      throw new RuntimeException("Error al obtener los pois desde HereMaps");
    }
  }

  private PoisResponse responseFrom(HereMapsPoisResponse input) {
    List<com.zonainmueble.reports.pois.Poi> pois = input.getItems().stream()
        .map(i -> {
          com.zonainmueble.reports.pois.Poi poi = new com.zonainmueble.reports.pois.Poi();
          BeanUtils.copyProperties(i, poi);
          poi.setLocation(new Coordinate(i.getPosition().getLat(), i.getPosition().getLng()));
          return poi;
        }).collect(Collectors.toList());

    return PoisResponse.builder()
        .pois(pois)
        .build();
  }

  public PoisResponse poisFallback(PoisRequest input, RuntimeException e) {
    log.error("poisFallback:", e);
    return PoisResponse.builder().pois(List.of()).build();
  }

  private String buildPoisUrl(PoisRequest input) {
    StringBuilder urlBuilder = new StringBuilder(browseUrl).append("?");
    urlBuilder.append("at=").append(input.getCenter().getLatitude()).append(",")
        .append(input.getCenter().getLongitude());

    if (!CollectionUtils.isEmpty(input.getCategories())) {
      String categories = String.join(",", input.getCategories());
      urlBuilder.append("&categories=").append(categories);
    }

    if (input.getBoundingBox() != null) {
      Extent bb = input.getBoundingBox();

      StringJoiner join = new StringJoiner(",");
      join.add(String.valueOf(bb.getSouthWest().getLongitude()));
      join.add(String.valueOf(bb.getSouthWest().getLatitude()));
      join.add(String.valueOf(bb.getNorthEast().getLongitude()));
      join.add(String.valueOf(bb.getNorthEast().getLatitude()));

      urlBuilder.append("&in=bbox:").append(join.toString());
    }

    if (input.getLimit() != null) {
      urlBuilder.append("&limit=").append(input.getLimit());
    }

    urlBuilder.append("&apiKey=").append(key);

    return urlBuilder.toString();
  }

}
