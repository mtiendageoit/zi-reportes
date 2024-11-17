package com.zonainmueble.reports.jasper;

import java.util.List;

import com.zonainmueble.reports.pois.Poi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaslowCategory {
  private String nombre;
  private List<Poi> pois;

  public long getCount() {
    if (pois == null || pois.isEmpty())
      return 0;
    return pois.size();
  }
}
