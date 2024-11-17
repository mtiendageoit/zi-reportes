package com.zonainmueble.reports.services.impl;

import static com.zonainmueble.reports.maps.MapType.roadmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.conclusion.ConclusionMessages;
import com.zonainmueble.reports.dto.ReportRequest;
import com.zonainmueble.reports.geometry.Coordinate;
import com.zonainmueble.reports.geometry.Isochrone;
import com.zonainmueble.reports.geometry.Isochrone.Time;
import com.zonainmueble.reports.geometry.Isochrone.TransportMode;
import com.zonainmueble.reports.isochrone.IsochroneRequest;
import com.zonainmueble.reports.maps.MapImageRequest;
import com.zonainmueble.reports.maps.Marker;
import com.zonainmueble.reports.maps.StyledPolygon;
import com.zonainmueble.reports.models.CategoryData;
import com.zonainmueble.reports.models.GrupoEdad;
import com.zonainmueble.reports.models.Municipio;
import com.zonainmueble.reports.models.Poblacion;
import com.zonainmueble.reports.models.PoblacionResumen;
import com.zonainmueble.reports.models.PoiCategory;
import com.zonainmueble.reports.models.PorcentajeEstudios;
import com.zonainmueble.reports.models.PrecioMetroCuadrado;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.services.AbstractReportService;
import com.zonainmueble.reports.services.ConclusionService;
import com.zonainmueble.reports.services.IsochroneService;
import com.zonainmueble.reports.services.JasperReportService;
import com.zonainmueble.reports.services.MapImagesService;
import com.zonainmueble.reports.services.PoisService;
import com.zonainmueble.reports.utils.NumberUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Slf4j
@Service("basicReportService")
public class BasicReportService extends AbstractReportService {
  private final TransportMode ISOCHRONE_TRANSPORT_MODE = TransportMode.WALKING;
  private final String JASPER_REPORT_PATH = "/static/reportes/basico/basico.jasper";

  private final ReportRepository repository;
  final ConclusionService conclusionService;
  private final MapImagesService imageService;

  public BasicReportService(
      final IsochroneService isochrone,
      final PoisService pois,
      final MapImagesService imageService,
      final ConclusionService conclusionService,
      final JasperReportService jasper,
      final ReportRepository repository) {
    super(isochrone, pois, jasper, repository);

    this.repository = repository;
    this.imageService = imageService;
    this.conclusionService = conclusionService;
  }

  @Override
  protected String getReportPath() {
    return JASPER_REPORT_PATH;
  }

  protected Time getIsochroneTime() {
    return Time.FIFTEEN_MINUTES;
  }

  @Override
  protected Map<String, Object> getReportParams(ReportRequest input, Municipio municipio) {
    Isochrone isochrone = basicReportIsochrone(input);

    Poblacion poblacion = repository.poblacion(isochrone);
    List<PorcentajeEstudios> estudios = repository.porcentajeEstudios(isochrone, municipio.getClaveEdo());

    Map<String, Object> params = new HashMap<String, Object>();

    params.putAll(generalParams(input, municipio));
    params.putAll(poblacionParams(poblacion));
    params.putAll(gruposEdadParams(isochrone));
    params.putAll(poblacionResumenParams(municipio, poblacion));
    params.putAll(poblacionPorcentajeEstudiosParams(estudios));
    params.putAll(graficaPorcentajeEstudiosParams(estudios));
    params.putAll(basicReportPoisParams(isochrone));

    Optional<PrecioMetroCuadrado> preciom2 = repository
        .precioMetroCuadrado(municipio.getClaveEdo(), municipio.getClaveMun());
    if (preciom2.isPresent()) {
      params.putAll(precioMetroCuadradoParams(preciom2.get()));
    }
    params.putAll(mapImagesParams(isochrone, preciom2.isPresent()));
    params.putAll(conclusionParams(params));

    return params;
  }

  private Map<String, Object> conclusionParams(Map<String, Object> params) {
    String systemPromt = ConclusionMessages.systemMessage();
    String message = ConclusionMessages.basicoConclusionMessage(params);

    String conclusion = conclusionService.conclusion(systemPromt, message);
    return Map.of("conclusion", conclusion);
  }

  private Map<String, Object> mapImagesParams(Isochrone isochrone, boolean mostrarPrecioM2) {
    Marker marker = new Marker(isochrone.getCenter());
    StyledPolygon polygon = new StyledPolygon(isochrone.getPolygon());

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] generales = imageService.image(new MapImageRequest(447, 263, roadmap, marker));
    params.put("mapImageGenerales", generales);

    int hight = mostrarPrecioM2 ? 280 : 388;
    byte[] habitantes = imageService.image(new MapImageRequest(232, hight, roadmap, marker, polygon));
    params.put("mapImageHabitantes", habitantes);

    byte[] tablero = imageService.image(new MapImageRequest(232, 372, roadmap, marker, polygon));
    params.put("mapImageTablero", tablero);

    return params;
  }

  protected Map<String, Object> basicReportPoisParams(Isochrone isochrone) {
    List<PoiCategory> pois = groupPoisCategoriesFrom(isochrone, repository.poisReporteBasico());

    Map<String, Object> params = new HashMap<String, Object>();

    // Mostrar top de pois
    if (!pois.isEmpty()) {
      params.put("pois_nombre_1", pois.get(0).getName());
      params.put("pois_numero_1", NumberUtils.formatToInt(pois.get(0).getCount()));
      params.put("pois_icono_1", pois.get(0).getIcon());

      if (pois.size() > 1) {
        params.put("pois_nombre_2", pois.get(1).getName());
        params.put("pois_numero_2", NumberUtils.formatToInt(pois.get(1).getCount()));
        params.put("pois_icono_2", pois.get(1).getIcon());
      }
    }

    return params;
  }

  protected Map<String, Object> generalParams(ReportRequest input, Municipio municipio) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(addressParam(input));
    params.putAll(municipioParams(municipio));
    params.put("isochrone_time_minutes", getIsochroneTime().getValue());
    params.put("isochrone_transport_type", ISOCHRONE_TRANSPORT_MODE.getName().toLowerCase());
    return params;
  }

  protected Map<String, Object> poblacionParams(Poblacion pob) {
    Map<String, Object> params = new HashMap<String, Object>();

    params.put("habitantes", NumberUtils.formatToInt(pob.getPobtot20()));
    params.put("habitantes2010", NumberUtils.formatToInt(pob.getPobtot10()));
    params.put("ninos", NumberUtils.formatToInt(pob.getNinos()));
    params.put("hombres", NumberUtils.formatToInt(pob.getHombres()));
    params.put("pHombres", NumberUtils.formatToInt(pob.getPHombres()));
    params.put("mujeres", NumberUtils.formatToInt(pob.getMujeres()));
    params.put("pMujeres", NumberUtils.formatToInt(pob.getPMujeres()));

    params.put("familias", NumberUtils.formatToInt(pob.getTothog20()));
    params.put("viviendas", NumberUtils.formatToInt(pob.getVivtot20()));
    params.put("habitantes_vivienda", NumberUtils.formatToOneDecimal(pob.getHabitantesVivienda()));

    params.put("viviendas_habitadas", NumberUtils.formatToInt(pob.getTvivpah20()));
    params.put("viviendas_1_dormitorio", NumberUtils.formatToInt(pob.getVph1dor20()));
    params.put("viviendas_2mas_dormitorios", NumberUtils.formatToInt(pob.getVph2ymd20()));

    params.put("gentrificacion", NumberUtils.formatToInt(pob.getGentrificacion()));

    return params;
  }

  protected Map<String, Object> gruposEdadParams(Isochrone isochrone) {
    GrupoEdad data = repository.grupoEdad(isochrone);

    double total = data.getP0a2f20() + data.getP3a520() + data.getP6a1120() +
        data.getP12a1420() + data.getP15a1720() + data.getP18a2420() + data.getPob156420() +
        data.getPmay6020() + data.getPob65ma20();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("habitantes_0a2", NumberUtils.percentageFormat(data.getP0a2f20(), total));
    params.put("habitantes_3a5", NumberUtils.percentageFormat(data.getP3a520(), total));
    params.put("habitantes_6a11", NumberUtils.percentageFormat(data.getP6a1120(), total));
    params.put("habitantes_12a14", NumberUtils.percentageFormat(data.getP12a1420(), total));
    params.put("habitantes_15a17", NumberUtils.percentageFormat(data.getP15a1720(), total));
    params.put("habitantes_18a24", NumberUtils.percentageFormat(data.getP18a2420(), total));
    params.put("habitantes_15a64", NumberUtils.percentageFormat(data.getPob156420(), total));
    params.put("habitantes_mas60", NumberUtils.percentageFormat(data.getPmay6020(), total));
    params.put("habitantes_mas65", NumberUtils.percentageFormat(data.getPob65ma20(), total));

    return params;
  }

  protected Map<String, Object> poblacionResumenParams(Municipio municipio, Poblacion poblacion) {
    PoblacionResumen data = repository.poblacionResumen(municipio.getClaveEdo(), poblacion);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("zona_habitantes", data.getHabitantesDesc());
    params.put("zona_habitantes_infantil", data.getHabitantesInfantilDesc());
    params.put("zona_densidad", data.getDensidadDesc());
    return params;
  }

  protected Map<String, Object> poblacionPorcentajeEstudiosParams(List<PorcentajeEstudios> estudios) {
    Map<String, Object> params = new HashMap<String, Object>();

    if (estudios.size() > 0) {
      params.put("zona_peh_porcentaje_1", NumberUtils.formatToInt(estudios.get(0).getPParticipacionRango() * 100));
      params.put("zona_peh_desc_1", estudios.get(0).getDescripcion());
      params.put("zona_peh_porcentaje_edo_1", NumberUtils.formatToInt(estudios.get(0).getPParticipacionEdo() * 100));
      params.put("zona_peh_grado_promedio_1", NumberUtils.formatToInt(estudios.get(0).getGrpesc20Isocrona()));
    }

    if (estudios.size() > 1) {
      params.put("zona_peh_porcentaje_2", NumberUtils.formatToInt(estudios.get(1).getPParticipacionRango() * 100));
      params.put("zona_peh_desc_2", estudios.get(1).getDescripcion());
      params.put("zona_peh_porcentaje_edo_2", NumberUtils.formatToInt(estudios.get(1).getPParticipacionEdo() * 100));
    }

    return params;
  }

  private Map<String, Object> graficaPorcentajeEstudiosParams(List<PorcentajeEstudios> estudios) {
    List<CategoryData> data = new ArrayList<>();

    Double percentage;
    for (PorcentajeEstudios item : estudios) {
      percentage = item.getPParticipacionRango();
      if (percentage >= 0.01) {// No mostrar los valores menores a 1%
        data.add(new CategoryData(item.getDescripcion(), percentage, item.getColor()));
      }
    }

    data.sort(Comparator.comparingDouble(CategoryData::getValue).reversed());
    data = data.subList(0, Math.min(4, data.size()));

    Map<String, Object> params = new HashMap<String, Object>();
    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
    params.put("grafica_dataset", dataSource);

    for (int i = 0; i < data.size(); i++) {
      params.put("grafica_category_" + (i + 1), data.get(i).getCategory());
      params.put("grafica_value_" + (i + 1), data.get(i).getValue());
      params.put("grafica_color_" + (i + 1), data.get(i).getColor());
    }

    return params;
  }

  protected Map<String, Object> precioMetroCuadradoParams(PrecioMetroCuadrado preciom2) {
    Map<String, Object> params = new HashMap<String, Object>();

    params.put("precio_m2_min", NumberUtils.formatToOneDecimal(preciom2.getPrecioMinimo()));
    params.put("precio_m2_max", NumberUtils.formatToOneDecimal(preciom2.getPrecioMaximo()));
    params.put("precio_m2", NumberUtils.formatToOneDecimal(preciom2.getPrecio()));
    params.put("precio_m2_90", NumberUtils.formatToInt(preciom2.getPrecio() * 90));

    return params;
  }

  private Isochrone basicReportIsochrone(ReportRequest input) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .transportMode(ISOCHRONE_TRANSPORT_MODE)
        .timeValues(List.of(getIsochroneTime()))
        .build();

    return isochronesFrom(request).getIsochrones().get(0);
  }

}
