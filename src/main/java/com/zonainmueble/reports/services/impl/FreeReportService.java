package com.zonainmueble.reports.services.impl;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.geometry.Isochrone.Time;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.services.ConclusionService;
import com.zonainmueble.reports.services.IsochroneService;
import com.zonainmueble.reports.services.JasperReportService;
import com.zonainmueble.reports.services.MapImagesService;
import com.zonainmueble.reports.services.PoisService;

@Service("freeReportService")
public class FreeReportService extends BasicReportService {
  public FreeReportService(
      final IsochroneService isochrone,
      final PoisService pois,
      final MapImagesService imageService,
      final ConclusionService conclusionService,
      final JasperReportService jasper,
      final ReportRepository repository) {
    super(isochrone, pois, imageService, conclusionService, jasper, repository);
  }

  protected Time getIsochroneTime() {
    return Time.FIVE_MINUTES;
  }
}
