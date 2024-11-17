package com.zonainmueble.reports.utils;

import java.util.*;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeometryUtils {
  private static final double METERS_PER_DEGREE_LATITUDE = 111_320.0; // Aproximadamente 111.32 km

  private static final GeometryFactory geometryFactory;

  static {
    geometryFactory = new GeometryFactory();
  }

  public static double farthestPointDistanceKM(com.zonainmueble.reports.geometry.Polygon polygon,
      com.zonainmueble.reports.geometry.Coordinate center) {
    Point from = point(center.getLatitude(), center.getLongitude());
    Polygon poly = fromPolygon(polygon);

    return farthestPointsKM(poly, from);
  }

  private static double farthestPointsKM(Polygon polygon, Point center) {
    double maxDistance = 0;

    for (Coordinate vertex : polygon.getCoordinates()) {
      double distance = center.getCoordinate().distance(vertex); // Distancia euclidiana entre el punto y el vértice

      if (distance > maxDistance) {
        maxDistance = distance;
      }
    }
    return (maxDistance * METERS_PER_DEGREE_LATITUDE) / 1000;
  }

  public static com.zonainmueble.reports.geometry.Extent boundingBox(
      com.zonainmueble.reports.geometry.Polygon polygon) {
    Polygon poly = fromPolygon(polygon);
    Polygon envelope = (Polygon) poly.getEnvelope();
    double xmin = envelope.getCoordinates()[0].x;
    double ymin = envelope.getCoordinates()[0].y;

    double xmax = envelope.getCoordinates()[2].x;
    double ymax = envelope.getCoordinates()[2].y;
    return new com.zonainmueble.reports.geometry.Extent(ymin, xmin, ymax, xmax);
  }

  public static com.zonainmueble.reports.geometry.Polygon buffer(com.zonainmueble.reports.geometry.Polygon polygon,
      double distance) {
    Polygon poly = fromPolygon(polygon);
    return toPolygon((Polygon) buffer(poly, distance));
  }

  public static String polygonToWKT(com.zonainmueble.reports.geometry.Polygon polygon) {
    return toWKT(fromPolygon(polygon));
  }

  private static Polygon fromPolygon(com.zonainmueble.reports.geometry.Polygon polygon) {
    List<Point> points = polygon.getCoordinates().stream()
        .map(coord -> point(coord.getLatitude(), coord.getLongitude())).collect(Collectors.toList());
    return toPolygon(points);
  }

  private static com.zonainmueble.reports.geometry.Polygon toPolygon(Polygon polygon) {
    List<com.zonainmueble.reports.geometry.Coordinate> coords = new ArrayList<>();

    for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
      coords.add(new com.zonainmueble.reports.geometry.Coordinate(coord.getY(), coord.getX()));
    }

    return new com.zonainmueble.reports.geometry.Polygon(coords);
  }

  // private Optional<Geometry> fromWKT(String wkt) {
  // WKTReader reader = new WKTReader(geometryFactory);
  // try {
  // return Optional.of(reader.read(wkt));
  // } catch (ParseException e) {
  // log.error(e.getMessage(), e);
  // log.error("Failed to get Geometry from wkt: {}", wkt);
  // }
  // return Optional.empty();
  // }

  private static Geometry buffer(Geometry geometry, double distanceMeters) {
    double distance = distanceMeters / METERS_PER_DEGREE_LATITUDE;
    Geometry buffer = geometry.buffer(distance);
    Geometry simplify = TopologyPreservingSimplifier.simplify(buffer, 0.0000488);
    return simplify;
  }

  private static String toWKT(Geometry geometry) {
    return geometry.toText();
  }

  private static Point point(double latitude, double longitude) {
    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }

  private static boolean coversPoint(Geometry geometry, Point point) {
    return geometry.covers(point);
  }

  private static Polygon toPolygon(List<Point> points) {
    List<Coordinate> coords = points.stream().map(point -> point.getCoordinate()).collect(Collectors.toList());
    return geometryFactory.createPolygon(coords.toArray(new Coordinate[0]));
  }

  public static List<com.zonainmueble.reports.pois.Poi> within(
      List<com.zonainmueble.reports.pois.Poi> pois,
      com.zonainmueble.reports.geometry.Polygon isochrone) {
    Polygon poly = fromPolygon(isochrone);

    List<com.zonainmueble.reports.pois.Poi> result = new ArrayList<>();

    for (com.zonainmueble.reports.pois.Poi poi : pois) {
      boolean isWithin = coversPoint(poly, point(poi.getLocation().getLatitude(),
          poi.getLocation().getLongitude()));
      if (isWithin) {
        result.add(poi);
      }
    }
    return result;
  }
}
