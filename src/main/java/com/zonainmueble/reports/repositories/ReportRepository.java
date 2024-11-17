package com.zonainmueble.reports.repositories;

import java.util.*;

import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import com.zonainmueble.reports.geometry.Isochrone;
import com.zonainmueble.reports.models.*;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ReportRepository {
  private final JdbcTemplate jdbcTemplate;

  public Optional<Municipio> municipio(double latitude, double longitude) {
    String sql = "SELECT * FROM public.___zi_municipio_intersectado(?, ?)";

    List<Municipio> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Municipio.class), latitude,
        longitude);

    if (data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(data.get(0));
  }

  public Poblacion poblacion(Isochrone isochrone) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_poblacion(?)";
    List<Poblacion> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Poblacion.class),
        isochrone.getPolygon().getWkt());
    return data.get(0);
  }

  public List<PorcentajeEstudios> porcentajeEstudios(Isochrone isochrone, String claveEdo) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_porcentajes_estudios_poblacion(?,?)";
    List<PorcentajeEstudios> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PorcentajeEstudios.class),
        isochrone.getPolygon().getWkt(), claveEdo);
    return data;
  }

  public GrupoEdad grupoEdad(Isochrone isochrone) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_grupos_edad(?)";
    List<GrupoEdad> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(GrupoEdad.class), isochrone.getPolygon().getWkt());
    return data.get(0);
  }

  public PoblacionResumen poblacionResumen(String claveEdo, Poblacion pob) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_poblacion_resumen_descripciones(?,?,?,?)";
    List<PoblacionResumen> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoblacionResumen.class),
        claveEdo, pob.getAvgpobtot20(), pob.getAvgp0a1120(),
        pob.getHabitantesVivienda());
    return data.get(0);
  }

  public Optional<PrecioMetroCuadrado> precioMetroCuadrado(String claveEdo,
      String claveMun) {
    String sql = "SELECT * FROM public.___zi_precio_tierra_metro_cuadrado(?, ?)";

    List<PrecioMetroCuadrado> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PrecioMetroCuadrado.class), claveEdo,
        claveMun);

    if (data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(data.get(0));
  }

  public List<PoiCategory> poisReporteBasico() {
    String sql = "SELECT * FROM public.___zi_puntos_interes_basico()";

    List<PoiCategory> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoiCategory.class));
    return data;
  }

  public List<NsePoblacion> nsePoblacion(String wktIso5, String wktIso10,
      String wktIso15) {
    String sql = "SELECT * FROM public.___zi_nivel_bienestar_ingresos_isocronas(?, ?, ?)";

    List<NsePoblacion> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(NsePoblacion.class),
        wktIso5, wktIso10, wktIso15);
    return data;
  }

  public List<PoiCategory> poisMovilidadCaminando() {
    String sql = "SELECT * FROM public.___zi_puntos_interes_integral_movilidad_caminando()";

    List<PoiCategory> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoiCategory.class));
    return data;
  }

  public List<PoiCategory> poisMovilidadAutomovil() {
    String sql = "SELECT * FROM public.___zi_puntos_interes_integral_movilidad_automovil()";

    List<PoiCategory> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoiCategory.class));
    return data;
  }

  public List<PoiMaslowCategory> poisMaslow() {
    String sql = "SELECT * FROM public.___zi_puntos_interes_integral_piramide_maslow()";

    List<PoiMaslowCategory> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoiMaslowCategory.class));
    return data;
  }

  public List<CategoriaMaslow> categoriasMaslow() {
    String sql = "SELECT * FROM public.___zi_pois_categorias_maslow()";

    List<CategoriaMaslow> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(CategoriaMaslow.class));
    return data;
  }

}
