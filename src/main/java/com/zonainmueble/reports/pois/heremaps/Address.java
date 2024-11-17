package com.zonainmueble.reports.pois.heremaps;

import lombok.Data;

@Data
public class Address {
  private String label;
  private String countryCode;
  private String countryName;
  private String stateCode;
  private String state;
  private String city;
  private String district;
  private String street;
  private String postalCode;
}
