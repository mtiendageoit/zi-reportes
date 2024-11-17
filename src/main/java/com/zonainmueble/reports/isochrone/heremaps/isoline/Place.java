package com.zonainmueble.reports.isochrone.heremaps.isoline;

import lombok.Data;

@Data
public class Place {
  private String type;
  private Location location;
  private Location originalLocation;
}
