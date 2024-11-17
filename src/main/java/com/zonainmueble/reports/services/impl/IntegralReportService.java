package com.zonainmueble.reports.services.impl;

import static com.zonainmueble.reports.maps.MapType.*;
import static com.zonainmueble.reports.geometry.Isochrone.Time.*;
import static com.zonainmueble.reports.geometry.Isochrone.TransportMode.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zonainmueble.reports.conclusion.ConclusionMessages;
import com.zonainmueble.reports.dto.ReportRequest;
import com.zonainmueble.reports.enums.NivelMaslow;
import com.zonainmueble.reports.geometry.Coordinate;
import com.zonainmueble.reports.geometry.Isochrone;
import com.zonainmueble.reports.geometry.Isochrone.Time;
import com.zonainmueble.reports.isochrone.IsochroneRequest;
import com.zonainmueble.reports.jasper.MaslowCategory;
import com.zonainmueble.reports.maps.MapImageRequest;
import com.zonainmueble.reports.maps.Marker;
import com.zonainmueble.reports.maps.StyledPolygon;
import com.zonainmueble.reports.maps.Marker.MarkerSize;
import com.zonainmueble.reports.models.CategoriaMaslow;
import com.zonainmueble.reports.models.Municipio;
import com.zonainmueble.reports.models.NsePoblacion;
import com.zonainmueble.reports.models.Poblacion;
import com.zonainmueble.reports.models.PoiCategory;
import com.zonainmueble.reports.models.PoiMaslowCategory;
import com.zonainmueble.reports.models.PorcentajeEstudios;
import com.zonainmueble.reports.models.PrecioMetroCuadrado;
import com.zonainmueble.reports.pois.Poi;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.services.ConclusionService;
import com.zonainmueble.reports.services.IsochroneService;
import com.zonainmueble.reports.services.JasperReportService;
import com.zonainmueble.reports.services.MapImagesService;
import com.zonainmueble.reports.services.PoisService;
import com.zonainmueble.reports.utils.DateTimeUtils;
import com.zonainmueble.reports.utils.GeometryUtils;
import com.zonainmueble.reports.utils.NumberUtils;
import com.zonainmueble.reports.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Slf4j
@Service("integralReportService")
public class IntegralReportService extends BasicReportService {
  private final String JASPER_REPORT_PATH = "/static/reportes/integral/integral.jasper";

  @Value("${apis.google.maps.custom.map-id}")
  private String googleCustomMapId;

  private final ReportRepository repository;
  private final MapImagesService imageService;
  private final ConclusionService conclusionService;

  public IntegralReportService(
      final IsochroneService isochroneService,
      final PoisService poisService,
      final MapImagesService imageService,
      final ConclusionService conclusionService,
      final JasperReportService jasperService,
      final ReportRepository repository) {
    super(isochroneService, poisService, imageService, conclusionService, jasperService, repository);

    this.repository = repository;
    this.imageService = imageService;
    this.conclusionService = conclusionService;
  }

  @Override
  protected String getReportPath() {
    return JASPER_REPORT_PATH;
  }

  @Override
  protected Map<String, Object> getReportParams(ReportRequest input, Municipio mun) {
    List<Isochrone> walkIsos = walkingIsochronesFrom(input);

    Optional<PrecioMetroCuadrado> preciom2 = repository
        .precioMetroCuadrado(mun.getClaveEdo(), mun.getClaveMun());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportParams(input, mun, walkIsos, preciom2));
    params.putAll(bienestarIngresosParams(input, mun, walkIsos, preciom2.isPresent()));
    params.putAll(movilidadParams(input, walkIsos));
    params.putAll(piramideMaslowParams(walkIsos));
    params.putAll(conclusionParams(params));
    return params;
  }

  private Map<String, Object> conclusionParams(Map<String, Object> params) {
    String systemPromt = ConclusionMessages.systemMessage();
    String message = ConclusionMessages.integralConclusionMessage(params);

    String conclusion = conclusionService.conclusion(systemPromt, message);
    return Map.of("conclusion", conclusion);
  }

  private Map<String, Object> piramideMaslowParams(List<Isochrone> walkIsos) {
    Map<String, Object> params = new HashMap<String, Object>();
    Isochrone isochrone = isochroneFrom(FIFTEEN_MINUTES, walkIsos);
    List<PoiMaslowCategory> poisCategories = repository.poisMaslow();
    List<CategoriaMaslow> maslowCategories = repository.categoriasMaslow();

    Map<NivelMaslow, List<Poi>> poisNivelMaslow = new HashMap<NivelMaslow, List<Poi>>();
    for (NivelMaslow nivel : NivelMaslow.values()) {
      List<PoiMaslowCategory> cats = poisCategories.stream().filter(i -> i.getNivelMaslow() == nivel.getValue())
          .collect(Collectors.toList());
      poisNivelMaslow.put(nivel, poisFrom(isochrone, cats));
    }

    params.putAll(piramideMaslowGeneralYDetalleParams(poisNivelMaslow, poisCategories, maslowCategories));
    params.putAll(piramideMaslowDistanciaParams(poisNivelMaslow));

    return params;
  }

  private Map<String, Object> piramideMaslowDistanciaParams(Map<NivelMaslow, List<Poi>> poisNivelMaslow) {
    Map<String, Object> params = new HashMap<String, Object>();

    double velocidadCaminandoKMH = 3;

    for (NivelMaslow nivel : NivelMaslow.values()) {
      List<Poi> pois = poisNivelMaslow.get(nivel);
      pois.sort(Comparator.comparing(Poi::getDistance));
      String prop = "POIS_MASLOW_NIVEL_" + nivel.getValue();

      for (int i = 0; i < pois.size(); i++) {
        params.put(prop + "_NOMBRE_" + i, StringUtils.alfanumeric(pois.get(i).getTitle()));
        double minutos = ((pois.get(i).getDistance() * 60) / (velocidadCaminandoKMH * 1000));
        if (minutos > 15) {
          minutos = 15;
        } else if (minutos < 1) {
          minutos = 1;
        }
        params.put(prop + "_MINUTOS_" + i, NumberUtils.formatToInt(minutos));
        params.put(prop + "_METROS_" + i, NumberUtils.formatToInt(pois.get(i).getDistance()));
      }
    }

    return params;
  }

  private Map<String, Object> piramideMaslowGeneralYDetalleParams(Map<NivelMaslow, List<Poi>> poisNivelMaslow,
      List<PoiMaslowCategory> poisCategories, List<CategoriaMaslow> maslowCategories) {
    Map<String, Object> params = new HashMap<String, Object>();

    Map<NivelMaslow, List<MaslowCategory>> poisMaslowCategories = new HashMap<>();

    List<MaslowCategory> maslowPois;
    for (NivelMaslow nivel : NivelMaslow.values()) {
      List<Poi> pois = poisNivelMaslow.get(nivel);
      if (pois != null) {
        List<CategoriaMaslow> cats = maslowCategories.stream().filter(i -> i.getNivelMaslow() == nivel.getValue())
            .collect(Collectors.toList());

        maslowPois = new ArrayList<>();
        for (CategoriaMaslow cat : cats) {
          List<String> keys = poisCategories.stream()
              .filter(pc -> pc.getIdCategoriaMaslow() == cat.getId())
              .map(g -> g.getKey()).collect(Collectors.toList());

          List<Poi> mp = pois.stream().filter(poi -> keys.contains(poi.getPrimaryCategory().get().getId()))
              .collect(Collectors.toList());

          maslowPois.add(new MaslowCategory(cat.getNombre(), mp));
        }
        poisMaslowCategories.put(nivel, maslowPois);
        params.put("MaslowPoisCollectionBeanNivel_" + nivel.getValue(), new JRBeanCollectionDataSource(maslowPois));
      }
    }

    params.putAll(piramideMaslowGeneralParams(poisMaslowCategories));

    return params;
  }

  private Map<String, Object> piramideMaslowGeneralParams(Map<NivelMaslow, List<MaslowCategory>> maslowCategories) {
    Map<String, Object> params = new HashMap<String, Object>();

    double porcentajeGeneral = 0;
    double porcentajeNivel = 0;
    for (NivelMaslow nivel : NivelMaslow.values()) {
      List<MaslowCategory> cats = maslowCategories.get(nivel);

      porcentajeNivel = 0;
      for (MaslowCategory cat : cats) {
        long count = cat.getCount();
        long numeroPeso = count >= 3 ? 3 : count;
        double porcentaje = ((100.0 / cats.size()) / 3) * numeroPeso;
        porcentajeNivel += porcentaje;
      }
      porcentajeGeneral += porcentajeNivel;
      params.put("piramide_maslow_porcentaje_nivel_" + nivel.getValue(),
          NumberUtils.formatToInt(porcentajeNivel));
    }

    params.put("piramide_maslow_porcentaje_global",
        NumberUtils.formatToInt(porcentajeGeneral / 5));
    return params;
  }

  private Map<String, Object> movilidadParams(ReportRequest input, List<Isochrone> walkIsos) {
    LocalDateTime diaAsueto = DateTimeUtils.previousFromNow(DayOfWeek.SUNDAY, 8);
    List<Isochrone> diaAsuetoIsos = drivingIsochronesFrom(input, diaAsueto);

    LocalDateTime diaLaboral = DateTimeUtils.previousFromNow(DayOfWeek.MONDAY, 8);
    List<Isochrone> diaLaboralIsos = drivingIsochronesFrom(input, diaLaboral);

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(movilidadEstacionesParams(walkIsos));
    params.putAll(movilidadGasolinerasParams(diaAsuetoIsos));
    params.putAll(movilidadAnalisisParams(input, diaAsuetoIsos, diaLaboralIsos));

    return params;
  }

  private Map<String, Object> movilidadAnalisisParams(ReportRequest input, List<Isochrone> diaAsuetoIsos,
      List<Isochrone> diaLaboralIsos) {
    Map<String, Object> params = new HashMap<String, Object>();

    Marker marker = new Marker(new Coordinate(input.getLatitude(), input.getLongitude()), MarkerSize.small);
    List<StyledPolygon> asuetoPolys = List.of(
        new StyledPolygon(isochroneFrom(TEN_MINUTES, diaAsuetoIsos).getPolygon(), "#A3DA91FF"),
        new StyledPolygon(isochroneFrom(THIRTY_MINUTES, diaAsuetoIsos).getPolygon(), "#E3E367FF"),
        new StyledPolygon(isochroneFrom(SIXTY_MINUTES, diaAsuetoIsos).getPolygon(), "#9BC1DBFF"));
    List<StyledPolygon> laboralPolys = List.of(
        new StyledPolygon(isochroneFrom(TEN_MINUTES, diaLaboralIsos).getPolygon(), "#A3DA91FF"),
        new StyledPolygon(isochroneFrom(THIRTY_MINUTES, diaLaboralIsos).getPolygon(), "#E3E367FF"),
        new StyledPolygon(isochroneFrom(SIXTY_MINUTES, diaLaboralIsos).getPolygon(), "#9BC1DBFF"));

    byte[] diaAsuetoImg = imageService
        .image(new MapImageRequest(326, 350, roadmap, marker, asuetoPolys, googleCustomMapId));
    params.put("mapImageMovilidadAnalisisDiaAsueto", diaAsuetoImg);

    byte[] diaLaboralImg = imageService
        .image(new MapImageRequest(326, 350, roadmap, marker, laboralPolys, googleCustomMapId));
    params.put("mapImageMovilidadAnalisisDiaLaboral", diaLaboralImg);

    params.putAll(addKmDistanceParams(diaAsuetoIsos, diaLaboralIsos));

    return params;
  }

  private Map<String, Object> addKmDistanceParams(List<Isochrone> diaAsuetoIsos, List<Isochrone> diaLaboralIsos) {
    Map<String, Object> params = new HashMap<String, Object>();
    Isochrone iso = isochroneFrom(TEN_MINUTES, diaAsuetoIsos);
    double distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilAsueto10", NumberUtils.formatToOneDecimal(distance));

    iso = isochroneFrom(THIRTY_MINUTES, diaAsuetoIsos);
    distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilAsueto30", NumberUtils.formatToOneDecimal(distance));

    iso = isochroneFrom(SIXTY_MINUTES, diaAsuetoIsos);
    distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilAsueto60", NumberUtils.formatToOneDecimal(distance));

    iso = isochroneFrom(TEN_MINUTES, diaLaboralIsos);
    distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilLaboral10", NumberUtils.formatToOneDecimal(distance));

    iso = isochroneFrom(THIRTY_MINUTES, diaLaboralIsos);
    distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilLaboral30", NumberUtils.formatToOneDecimal(distance));

    iso = isochroneFrom(SIXTY_MINUTES, diaLaboralIsos);
    distance = GeometryUtils.farthestPointDistanceKM(iso.getPolygon(), iso.getCenter());
    params.put("isoAutomovilLaboral60", NumberUtils.formatToOneDecimal(distance));

    return params;
  }

  private Map<String, Object> movilidadGasolinerasParams(List<Isochrone> carIsos) {
    Map<String, Object> params = new HashMap<String, Object>();

    Isochrone isochrone = isochroneFrom(TEN_MINUTES, carIsos);
    List<PoiCategory> categories = repository.poisMovilidadAutomovil();
    List<Poi> pois = poisFrom(isochrone, categories);
    List<Marker> markers = markersFrom(pois, categories, MarkerSize.small);
    markers.add(new Marker(isochrone.getCenter()));

    byte[] img = imageService.image(new MapImageRequest(448, 448, roadmap, markers));
    params.put("mapImageMovilidadGasolineras", img);

    params.put("poisMapPinColorGasolineras", PoiCategory.GASOLINERA().getColor());
    params.put("poisMapPinColorEstacionamientos", PoiCategory.ESTACIONAMIENTO().getColor());

    List<Poi> gasolineras = pois.stream()
        .filter(i -> i.primaryCategoryIs(PoiCategory.GASOLINERA().getKey()))
        .collect(Collectors.toList());
    List<Poi> estacionamientos = pois.stream()
        .filter(i -> i.primaryCategoryIs(PoiCategory.ESTACIONAMIENTO().getKey()))
        .collect(Collectors.toList());

    if (gasolineras.size() > 0) {
      gasolineras.sort(Comparator.comparing(Poi::getDistance));
      params.put("poisNumeroGasolineras", gasolineras.size());
      params.put("poisGasolineraCercana", NumberUtils.formatToInt(gasolineras.get(0).getDistance()));
    }

    if (estacionamientos.size() > 0) {
      estacionamientos.sort(Comparator.comparing(Poi::getDistance));
      params.put("poisNumeroEstacionamientos", estacionamientos.size());
      params.put("poisEstacionamientoCercano", NumberUtils.formatToInt(estacionamientos.get(0).getDistance()));
    }

    return params;
  }

  private Map<String, Object> movilidadEstacionesParams(List<Isochrone> walkIsos) {
    Map<String, Object> params = new HashMap<String, Object>();

    Isochrone isochrone = isochroneFrom(FIFTEEN_MINUTES, walkIsos);
    List<PoiCategory> categories = repository.poisMovilidadCaminando();
    List<Poi> pois = poisFrom(isochrone, categories);
    List<Marker> markers = markersFrom(pois, categories, MarkerSize.small);
    markers.add(new Marker(isochrone.getCenter()));

    byte[] img = imageService.image(new MapImageRequest(448, 448, roadmap, markers));
    params.put("mapImageMovilidadEstaciones", img);

    return params;
  }

  private Map<String, Object> bienestarIngresosParams(ReportRequest input, Municipio municipio,
      List<Isochrone> walkIsos, boolean mostrarPrecioM2) {
    Map<String, Object> params = bienestarNSEParams(input, municipio, walkIsos);

    String systemPromt = ConclusionMessages.systemMessage();
    String preponderantes = ConclusionMessages.integralNivelBienestarPreponderantes(params);
    String conclusionPreponderantes = conclusionService.conclusion(systemPromt, preponderantes);
    params.put("conclusionNivelBienestarPrepronderantes", conclusionPreponderantes);

    String zonaAnalisis = ConclusionMessages.integralNivelBienestarZonaAnalisis(params);
    String conclusionZonaAnalisis = conclusionService.conclusion(systemPromt, zonaAnalisis);
    params.put("conclusionNivelBienestarZonaAnalisis", conclusionZonaAnalisis);

    Marker marker = new Marker(new Coordinate(input.getLatitude(), input.getLongitude()));
    List<StyledPolygon> polygons = List.of(
        new StyledPolygon(isochroneFrom(FIVE_MINUTES, walkIsos).getPolygon(), "#A3DA91FF"),
        new StyledPolygon(isochroneFrom(TEN_MINUTES, walkIsos).getPolygon(), "#E3E367FF"),
        new StyledPolygon(isochroneFrom(FIFTEEN_MINUTES, walkIsos).getPolygon(), "#9BC1DBFF"));

    byte[] bienestarIngresoImg = imageService.image(new MapImageRequest(232, 416, roadmap, marker, polygons));
    params.put("mapImageBienestarIngreso", bienestarIngresoImg);

    if (mostrarPrecioM2) {
      byte[] precioInmuebleImg = imageService.image(new MapImageRequest(232, 416, roadmap, marker, polygons));
      params.put("bienestarPrecioinmueble", precioInmuebleImg);
    }

    return params;
  }

  private List<Isochrone> walkingIsochronesFrom(ReportRequest input) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .timeValues(List.of(FIVE_MINUTES, TEN_MINUTES, FIFTEEN_MINUTES))
        .transportMode(WALKING)
        .build();

    return isochronesFrom(request).getIsochrones();
  }

  private List<Isochrone> drivingIsochronesFrom(ReportRequest input, LocalDateTime departureTime) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .timeValues(List.of(TEN_MINUTES, THIRTY_MINUTES, SIXTY_MINUTES))
        .transportMode(DRIVING)
        .departureTime(departureTime)
        .build();

    return isochronesFrom(request).getIsochrones();
  }

  private Isochrone isochroneFrom(Time time, List<Isochrone> isochrones) {
    return isochrones.stream().filter(i -> i.getTime() == time).findFirst().get();
  }

  private Map<String, Object> bienestarNSEParams(ReportRequest input, Municipio municipio,
      List<Isochrone> walkIsos) {

    String wktIso5 = isochroneFrom(FIVE_MINUTES, walkIsos).getPolygon().getWkt();
    String wktIso10 = isochroneFrom(TEN_MINUTES, walkIsos).getPolygon().getWkt();
    String wktIso15 = isochroneFrom(FIFTEEN_MINUTES, walkIsos).getPolygon().getWkt();

    List<NsePoblacion> nse = repository.nsePoblacion(wktIso5, wktIso10, wktIso15);
    NsePoblacion nseTop1 = nse.get(0);
    NsePoblacion nseTop2 = nse.get(1);
    NsePoblacion nseTop3 = nse.get(2);

    double iso5Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso5).sum();
    double iso10Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso10).sum();
    double iso15Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso15).sum();

    double poblacionTotal = iso5Poblacion + iso10Poblacion + iso15Poblacion;
    double top1Poblacion = nseTop1.getPobtot20Iso5() + nseTop1.getPobtot20Iso10() + nseTop1.getPobtot20Iso15();
    double top2Poblacion = nseTop2.getPobtot20Iso5() + nseTop2.getPobtot20Iso10() + nseTop2.getPobtot20Iso15();
    double top3Poblacion = nseTop3.getPobtot20Iso5() + nseTop3.getPobtot20Iso10() + nseTop3.getPobtot20Iso15();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("nse_top1_poblacion_porcentaje", percent(top1Poblacion, poblacionTotal));
    params.put("nse_top2_poblacion_porcentaje", percent(top2Poblacion, poblacionTotal));
    params.put("nse_top3_poblacion_porcentaje", percent(top3Poblacion, poblacionTotal));

    params.put("nse_mostrar_top_3_nivel_bienestar", "NO");
    double topPorcentaje = NumberUtils.percentageOf(top1Poblacion + top2Poblacion, poblacionTotal);
    double porcentajePreponderante = 60;
    if (topPorcentaje < porcentajePreponderante) {
      topPorcentaje = NumberUtils.percentageOf(top1Poblacion + top2Poblacion + top3Poblacion, poblacionTotal);
      params.put("nse_mostrar_top_3_nivel_bienestar", "SI");
    }
    params.put("nse_tops_poblacion_porcentaje", NumberUtils.formatToOneDecimal(topPorcentaje));

    params.put("nse_ingreso_promedio",
        NumberUtils.formatToInt((nseTop1.getImppheIso5() + nseTop1.getImppheIso10() + nseTop1.getImppheIso15()) / 3));

    params.put("nse_iso5_poblacion", NumberUtils.formatToInt(iso5Poblacion));
    params.put("nse_iso10_poblacion", NumberUtils.formatToInt(iso10Poblacion));
    params.put("nse_iso15_poblacion", NumberUtils.formatToInt(iso15Poblacion));
    params.put("nse_top_1_desc", nseTop1.getDescripcion());
    params.put("nse_top_1_nombre", nseTop1.getNombre());
    params.put("nse_top_1_nse", nseTop1.getNse());
    params.put("nse_top_1_color", nseTop1.getColorHex());
    params.put("nse_top_2_desc", nseTop2.getDescripcion());
    params.put("nse_top_2_nombre", nseTop2.getNombre());
    params.put("nse_top_2_nse", nseTop2.getNse());
    params.put("nse_top_2_color", nseTop2.getColorHex());
    params.put("nse_top_3_desc", nseTop3.getDescripcion());
    params.put("nse_top_3_nombre", nseTop3.getNombre());
    params.put("nse_top_3_nse", nseTop3.getNse());
    params.put("nse_top_3_color", nseTop3.getColorHex());

    params.put("nse_iso5_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso5()));
    params.put("nse_iso5_top_1_porcentaje", percent(nseTop1.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso10()));
    params.put("nse_iso10_top_1_porcentaje", percent(nseTop1.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso15()));
    params.put("nse_iso15_top_1_porcentaje", percent(nseTop1.getPobtot20Iso15(), iso15Poblacion));

    params.put("nse_iso5_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso5()));
    params.put("nse_iso5_top_2_porcentaje", percent(nseTop2.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso10()));
    params.put("nse_iso10_top_2_porcentaje", percent(nseTop2.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso15()));
    params.put("nse_iso15_top_2_porcentaje", percent(nseTop2.getPobtot20Iso15(), iso15Poblacion));

    params.put("nse_iso5_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso5()));
    params.put("nse_iso5_top_3_porcentaje", percent(nseTop3.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso10()));
    params.put("nse_iso10_top_3_porcentaje", percent(nseTop3.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso15()));
    params.put("nse_iso15_top_3_porcentaje", percent(nseTop3.getPobtot20Iso15(), iso15Poblacion));

    // Para poner en negrita los porcentajes mayores en cada isocrona nse
    int indexMaximo;
    double valorActual;
    double valorMaximo = Double.MIN_VALUE;
    String key = "nse_iso[ISO]_top_[TOP]_porcentaje";
    for (String isoTime : List.of("5", "10", "15")) {
      indexMaximo = 1;
      valorMaximo = Double.MIN_VALUE;
      for (int i = 1; i < 4; i++) {
        valorActual = Double
            .valueOf(params.get(key.replace("[ISO]", isoTime).replace("[TOP]", String.valueOf(i))).toString());
        if (valorActual > valorMaximo) {
          valorMaximo = valorActual;
          indexMaximo = i;
        }
      }
      params.put("nse_iso[ISO]_maximo_index".replace("[ISO]", isoTime), String.valueOf(indexMaximo));
    }

    return params;
  }

  private String percent(double value, double total) {
    return NumberUtils.percentageIntFormat(value, total);
  }

  private Map<String, Object> basicReportParams(ReportRequest input, Municipio municipio, List<Isochrone> walkIsos,
      Optional<PrecioMetroCuadrado> preciom2) {
    Isochrone isochrone = isochroneFrom(FIFTEEN_MINUTES, walkIsos);

    Poblacion poblacion = repository.poblacion(isochrone);
    List<PorcentajeEstudios> estudios = repository.porcentajeEstudios(isochrone, municipio.getClaveEdo());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(generalParams(input, municipio));
    params.putAll(poblacionParams(poblacion));
    params.putAll(gruposEdadParams(isochrone));
    params.putAll(poblacionResumenParams(municipio, poblacion));
    params.putAll(poblacionPorcentajeEstudiosParams(estudios));
    params.putAll(basicReportPoisParams(isochrone));

    if (preciom2.isPresent()) {
      params.putAll(precioMetroCuadradoParams(preciom2.get()));
    }

    params.putAll(basicReportMapImagesParams(isochrone, preciom2.isPresent()));

    return params;
  }

  private Map<String, Object> basicReportMapImagesParams(Isochrone isochrone, boolean mostrarPrecioM2) {
    Marker marker = new Marker(isochrone.getCenter());
    StyledPolygon polygon = new StyledPolygon(isochrone.getPolygon(), "#9BC1DBFF");

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] generales = imageService.image(new MapImageRequest(447, 263, roadmap, marker));
    params.put("mapImageGenerales", generales);

    int hight = mostrarPrecioM2 ? 280 : 388;
    byte[] habitantes = imageService.image(new MapImageRequest(232, hight, roadmap, marker, polygon));
    params.put("mapImageHabitantes", habitantes);

    byte[] tablero = imageService.image(new MapImageRequest(262, 556, hybrid, marker, polygon));
    params.put("mapImageTablero", tablero);

    byte[] familias = imageService.image(new MapImageRequest(232, 434, roadmap, marker, polygon));
    params.put("mapImageFamilias", familias);

    byte[] conclusion = imageService.image(new MapImageRequest(227, 408, roadmap, marker));
    params.put("mapImageConclusion", conclusion);

    return params;
  }

}
