package com.zonainmueble.reports.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PoiMaslowCategory extends PoiCategory {
  public static final int ID_CATEGORIA_MASLOW_ESCUELAS = 204;

  private int idCategoriaMaslow;
  private int nivelMaslow;
}
