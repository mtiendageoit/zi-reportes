package com.zonainmueble.reports.services;

import com.zonainmueble.reports.isochrone.IsochroneRequest;
import com.zonainmueble.reports.isochrone.IsochroneResponse;

public interface IsochroneService {
  public IsochroneResponse isochrone(IsochroneRequest request);
}
