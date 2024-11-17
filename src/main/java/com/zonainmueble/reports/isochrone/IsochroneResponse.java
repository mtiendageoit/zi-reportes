package com.zonainmueble.reports.isochrone;

import java.util.List;

import com.zonainmueble.reports.geometry.Isochrone;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsochroneResponse {
  private List<Isochrone> isochrones;
}
