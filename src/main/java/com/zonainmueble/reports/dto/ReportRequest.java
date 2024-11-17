package com.zonainmueble.reports.dto;

import com.zonainmueble.reports.validations.ValidCoordinates;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ValidCoordinates
public class ReportRequest {
  @NotBlank
  private String address;

  private double longitude;
  private double latitude;
}
