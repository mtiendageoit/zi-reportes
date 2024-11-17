package com.zonainmueble.reports.geometry;

import java.util.NoSuchElementException;

import lombok.*;

@Data
@Builder
public class Isochrone {
  private Time time;
  private TransportMode transportMode;

  private Coordinate center;
  private Polygon polygon;

  public enum TransportMode {
    DRIVING("Manejando"), WALKING("Caminando");

    private final String name;

    private TransportMode(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public enum Time {
    FIVE_MINUTES(5), TEN_MINUTES(10), FIFTEEN_MINUTES(15), TWENTY_MINUTES(20), THIRTY_MINUTES(30), SIXTY_MINUTES(60);

    private final int value;

    private Time(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static Time from(int minutes) {
      if (minutes == Time.FIVE_MINUTES.value)
        return Time.FIVE_MINUTES;
      if (minutes == Time.TEN_MINUTES.value)
        return Time.TEN_MINUTES;
      if (minutes == Time.FIFTEEN_MINUTES.value)
        return Time.FIFTEEN_MINUTES;
      if (minutes == Time.TWENTY_MINUTES.value)
        return Time.TWENTY_MINUTES;
      if (minutes == Time.THIRTY_MINUTES.value)
        return Time.THIRTY_MINUTES;
      if (minutes == Time.SIXTY_MINUTES.value)
        return Time.SIXTY_MINUTES;

      throw new NoSuchElementException("Time no existe para el valor en minutos: " + minutes);
    }
  }
}
