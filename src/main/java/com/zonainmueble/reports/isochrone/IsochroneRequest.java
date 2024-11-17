package com.zonainmueble.reports.isochrone;

import java.util.List;
import java.time.LocalDateTime;

import com.zonainmueble.reports.geometry.*;
import com.zonainmueble.reports.geometry.Isochrone.Time;
import com.zonainmueble.reports.geometry.Isochrone.TransportMode;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IsochroneRequest {
  private Coordinate center;
  private TransportMode transportMode;
  private List<Time> timeValues;
  private LocalDateTime departureTime;
}
