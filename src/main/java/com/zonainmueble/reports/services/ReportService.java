package com.zonainmueble.reports.services;

import com.zonainmueble.reports.dto.ReportRequest;

public interface ReportService {
  byte[] pdf(ReportRequest request);
}
