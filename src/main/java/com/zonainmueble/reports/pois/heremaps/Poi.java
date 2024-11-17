package com.zonainmueble.reports.pois.heremaps;

import java.util.*;

import com.zonainmueble.reports.pois.Category;

import lombok.Data;

@Data
public class Poi {
  private String id;
  private String title;
  private String language;
  private String resultType;

  private Address address;
  private Location position;
  private List<Location> access;

  private Double distance;
  private List<Category> categories;

  public Optional<Category> getPrimaryCategory() {
    if (categories != null && !categories.isEmpty()) {
      return categories.stream().filter(item -> item.getPrimary()).findFirst();
    }
    return Optional.empty();
  }
}
