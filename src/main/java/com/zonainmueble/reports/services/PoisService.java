package com.zonainmueble.reports.services;

import com.zonainmueble.reports.pois.PoisRequest;
import com.zonainmueble.reports.pois.PoisResponse;

public interface PoisService {
  public PoisResponse pois(PoisRequest input);
}
