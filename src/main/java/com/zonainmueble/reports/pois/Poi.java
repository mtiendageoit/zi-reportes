package com.zonainmueble.reports.pois;

import java.util.List;
import java.util.Optional;

import com.zonainmueble.reports.geometry.Coordinate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poi {
  private String id;
  private String title;
  private List<Category> categories;

  private Double distance; // meters
  private Coordinate location;

  public boolean anyCategoryIs(String category) {
    if (categories != null && !categories.isEmpty()) {
      return categories.stream().anyMatch(i -> i.getId().equalsIgnoreCase(category));
    }
    return false;
  }

  public boolean primaryCategoryIs(String category) {
    if (categories != null && !categories.isEmpty()) {
      return categories.stream()
          .anyMatch(i -> i.getPrimary() != null
              && i.getPrimary()
              && i.getId().equalsIgnoreCase(category));
    }
    return false;
  }

  public Optional<Category> getPrimaryCategory() {
    if (categories != null && !categories.isEmpty()) {
      return categories.stream().filter(item -> item.getPrimary()).findFirst();
    }
    return Optional.empty();
  }
}
