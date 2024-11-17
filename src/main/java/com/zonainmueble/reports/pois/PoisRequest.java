package com.zonainmueble.reports.pois;

import java.util.*;

import com.zonainmueble.reports.geometry.*;

import lombok.Data;

@Data
public class PoisRequest {
  private Coordinate center;
  private Extent boundingBox;
  
  private List<String> categories;
  private Integer limit;
}
