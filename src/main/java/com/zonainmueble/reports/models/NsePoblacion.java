package com.zonainmueble.reports.models;

import lombok.Data;

@Data
public class NsePoblacion {
  private int id;
  private String nse;
  private String nombre;
  private String descripcion;
  private String colorHex;
  private Double rangoInicial;
  private Double rangoFinal;
  private Double pobtot20Iso5;
  private Double imppheIso5;
  private Double tothog20Iso5;
  private Double pobtot20Iso10;
  private Double imppheIso10;
  private Double tothog20Iso10;
  private Double pobtot20Iso15;
  private Double imppheIso15;
  private Double tothog20Iso15;
}
