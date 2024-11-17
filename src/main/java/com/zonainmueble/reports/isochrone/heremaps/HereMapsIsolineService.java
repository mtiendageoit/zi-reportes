package com.zonainmueble.reports.isochrone.heremaps;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.geometry.*;
import com.zonainmueble.reports.geometry.Isochrone.*;
import com.zonainmueble.reports.geometry.Polygon;
import com.zonainmueble.reports.isochrone.IsochroneRequest;
import com.zonainmueble.reports.isochrone.IsochroneResponse;
import com.zonainmueble.reports.isochrone.heremaps.isoline.*;
import com.zonainmueble.reports.isochrone.heremaps.isoline.PolylineEncoderDecoder.LatLngZ;
import com.zonainmueble.reports.services.IsochroneService;
import com.zonainmueble.reports.utils.DateTimeUtils;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class HereMapsIsolineService implements IsochroneService {
  private final int SECONDS_IN_MINUTE = 60;

  @Value("${apis.here.maps.key}")
  private String key;

  @Value("${apis.here.maps.isoline.url}")
  private String isolineUrl;

  private final RestTemplate restTemplate;

  @Override
  @Retry(name = "hereMapsIsolineRequest", fallbackMethod = "isolineFallback")
  public IsochroneResponse isochrone(IsochroneRequest input) {
    String url = buildIsolineUrl(input);

    log.info("url: " + url);
    try {
      ResponseEntity<IsolineResponse> response = restTemplate.getForEntity(url, IsolineResponse.class);
      return buildIsochroneResponse(response.getBody(), input);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("url: {}, input: {}", url, input);
      throw new RuntimeException("Error al obtener el isoline desde HereMaps");
    }
  }

  public IsochroneResponse isolineFallback(IsochroneRequest input, RuntimeException e) {
    log.error("isolineFallback:", e);
    return new IsochroneResponse(List.of());
  }

  private String buildIsolineUrl(IsochroneRequest input) {
    String origin = String.format("%f,%f", input.getCenter().getLatitude(), input.getCenter().getLongitude());

    List<Integer> valuesList = input.getTimeValues().stream()
        .map(time -> time.getValue() * SECONDS_IN_MINUTE)
        .toList();

    String values = StringUtils.collectionToCommaDelimitedString(valuesList);

    StringBuilder urlBuilder = new StringBuilder(isolineUrl);
    urlBuilder.append("?transportMode=").append(isochroneTypeFrom(input.getTransportMode()));
    urlBuilder.append("&origin=").append(origin);

    if (input.getDepartureTime() != null) {
      urlBuilder.append("&departureTime=")
          .append(DateTimeUtils.format(input.getDepartureTime(), "yyyy-MM-dd HH:mm:ss").replace(" ", "T"));
    }

    urlBuilder.append("&range[type]=time");
    urlBuilder.append("&range[values]=").append(values);
    urlBuilder.append("&shape[maxPoints]=350");
    urlBuilder.append("&optimizeFor=performance");
    urlBuilder.append("&apiKey=").append(key);

    return urlBuilder.toString();
  }

  private IsochroneResponse buildIsochroneResponse(IsolineResponse input, IsochroneRequest request) {
    List<Isochrone> isochrones = new ArrayList<>();

    for (Isoline isoline : input.getIsolines()) {
      isochrones.add(toIsocrhone(isoline, request));
    }
    return new IsochroneResponse(isochrones);
  }

  private Isochrone toIsocrhone(Isoline isoline, IsochroneRequest request) {
    List<Coordinate> coords = new ArrayList<>();

    List<LatLngZ> locations = PolylineEncoderDecoder.decode(isoline.getPolygons().get(0).getOuter());
    for (LatLngZ loc : locations) {
      coords.add(new Coordinate(loc.lat, loc.lng));
    }

    return Isochrone.builder()
        .transportMode(request.getTransportMode())
        .time(Time.from(isoline.getRange().getValue() / SECONDS_IN_MINUTE))
        .polygon(new Polygon(coords))
        .center(request.getCenter())
        .build();
  }

  private String isochroneTypeFrom(TransportMode type) {
    switch (type) {
      case WALKING:
        return "pedestrian";
      case DRIVING:
        return "car";
      default:
        throw new NoSuchElementException("El elemento no existe");
    }
  }
}
