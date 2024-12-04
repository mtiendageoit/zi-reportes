package com.zonainmueble.reports.services;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.zonainmueble.reports.dto.ReportRequest;
import com.zonainmueble.reports.exceptions.LocationDataUnavailableException;
import com.zonainmueble.reports.geometry.Isochrone;
import com.zonainmueble.reports.isochrone.IsochroneRequest;
import com.zonainmueble.reports.isochrone.IsochroneResponse;
import com.zonainmueble.reports.maps.Marker;
import com.zonainmueble.reports.maps.Marker.MarkerSize;
import com.zonainmueble.reports.models.Municipio;
import com.zonainmueble.reports.models.PoiCategory;
import com.zonainmueble.reports.pois.Poi;
import com.zonainmueble.reports.pois.PoisRequest;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.utils.DateTimeUtils;
import com.zonainmueble.reports.utils.GeometryUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractReportService implements ReportService {
  private final Integer BUFFER_METERS = 10;

  private final ReportRepository repository;

  private final PoisService poisService;
  private final JasperReportService jasper;
  private final IsochroneService isochroneService;

  protected abstract String getReportPath();

  protected abstract Map<String, Object> getReportParams(ReportRequest input, Municipio municipio);

  public AbstractReportService(
      IsochroneService isochroneService,
      PoisService poisService,
      JasperReportService jasper,
      ReportRepository repository) {

    this.jasper = jasper;
    this.repository = repository;
    this.poisService = poisService;
    this.isochroneService = isochroneService;
  }

  @Override
  public byte[] pdf(ReportRequest input) {
    var municipio = municipioFrom(input.getLatitude(), input.getLongitude());

    var reportParams = getReportParams(input, municipio);
    reportParams.putAll(buildTimeParams());

    Map<String, Object> params = new HashMap<>();
    params.put("params", reportParams);
    return jasper.generatePdf(getReportPath(), params);
  }

  protected Map<String, String> addressParam(ReportRequest input) {
    String address = input.getAddress().trim();

    if (!address.endsWith(".")) {
      address += ".";
    }

    return Map.of("address", address);
  }

  protected Map<String, String> municipioParams(Municipio municipio) {
    return Map.of("clave_edo", municipio.getClaveEdo(), "nombre_edo", municipio.getNombreEdo());
  }

  private Map<String, String> buildTimeParams() {
    LocalDateTime now = DateTimeUtils.now();
    String date = DateTimeUtils.format(now, "dd MMMM'.' yyyy");
    date = date.substring(0, 3) + date.substring(3, 4).toUpperCase() + date.substring(4);
    String time = DateTimeUtils.format(now, "HH:mm 'hrs'");

    return Map.of("report_date", date, "report_time", time);
  }

  private Municipio municipioFrom(double latitude, double longitude) {
    Optional<Municipio> mun = repository.municipio(latitude, longitude);

    if (mun.isPresent())
      return mun.get();

    log.warn(
        "No se puede generar el reporte para la ubicacion enviada, ya que no se encontro alguna manzana con la cual intersecte. latitude: {}, longitude: {}",
        latitude, longitude);

    throw new LocationDataUnavailableException("LOCATION_DATA_UNAVAILABLE",
        "No se puede generar el reporte para la ubicación enviada");
  }

  protected IsochroneResponse isochronesFrom(IsochroneRequest input) {
    IsochroneResponse response = isochroneService.isochrone(input);

    response.getIsochrones().forEach(isochrone -> {
      isochrone.setPolygon(GeometryUtils.buffer(isochrone.getPolygon(), BUFFER_METERS));
      isochrone.getPolygon().setWkt(GeometryUtils.polygonToWKT(isochrone.getPolygon()));
    });

    return response;
  }

  protected List<Poi> poisFrom(Isochrone isochrone, List<? extends PoiCategory> categories) {
    if (categories.isEmpty())
      return Collections.emptyList();

    List<String> catIds = categories.stream().map(i -> i.getKey()).collect(Collectors.toList());

    PoisRequest request = new PoisRequest();
    request.setCategories(catIds);
    request.setCenter(isochrone.getCenter());
    request.setBoundingBox(GeometryUtils.boundingBox(isochrone.getPolygon()));
    request.setLimit(100);

    List<Poi> pois = poisService.pois(request).getPois();
    List<Poi> intersectPois = GeometryUtils.within(pois, isochrone.getPolygon());
    return intersectPois;
  }

  protected List<PoiCategory> groupPoisCategoriesFrom(Isochrone isochrone, List<PoiCategory> categories) {
    List<Poi> pois = poisFrom(isochrone, categories);
    return categorizeAndSortPois(categories, pois);
  }

  protected List<PoiCategory> categorizeAndSortPois(List<PoiCategory> mainCategories, List<Poi> pois) {
    List<PoiCategory> categories = new ArrayList<PoiCategory>();

    Map<String, Long> count = pois.stream()
        .filter(poi -> poi.getPrimaryCategory().isPresent())
        .collect(Collectors.groupingBy(poi -> poi.getPrimaryCategory().get().getId(), Collectors.counting()));

    categories.addAll(categoriesCount(mainCategories, count));

    return categories;
  }

  private List<PoiCategory> categoriesCount(List<PoiCategory> categories, Map<String, Long> count) {
    return categories.stream()
        .peek(category -> category.setCount(count.getOrDefault(category.getKey(), 0L).intValue()))
        .filter(category -> category.getCount() > 0)
        .sorted(Comparator.comparingInt(PoiCategory::getCount).reversed()).collect(Collectors.toList());
  }

  protected List<Marker> markersFrom(List<Poi> pois, List<PoiCategory> categories, MarkerSize size) {
    List<Marker> markers = new ArrayList<>();

    Optional<PoiCategory> category;
    for (Poi poi : pois) {
      if (poi.getPrimaryCategory().isPresent()) {
        category = categories.stream()
            .filter(cat -> cat.getKey().equalsIgnoreCase(poi.getPrimaryCategory().get().getId())).findFirst();
        if (category.isPresent()) {
          markers.add(Marker.builder()
              .coordinate(poi.getLocation())
              .color(category.get().getColor())
              .size(size)
              .build());
        }
      }
    }

    return markers;
  }
}
