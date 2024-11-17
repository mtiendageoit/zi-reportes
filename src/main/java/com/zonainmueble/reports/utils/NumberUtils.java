package com.zonainmueble.reports.utils;

import java.text.DecimalFormat;

public class NumberUtils {
  private static DecimalFormat INT;
  private static DecimalFormat ONE_DECIMAL;

  static {
    INT = new DecimalFormat("#,###");
    ONE_DECIMAL = new DecimalFormat("#,##0.0");
  }

  public static String formatToInt(Object number) {
    return INT.format(number);
  }

  public static String formatToOneDecimal(Object number) {
    return ONE_DECIMAL.format(number);
  }

  public static double percentageOf(double value, double total) {
    double percent = 0;
    if (total != 0) {
      percent = (value / total) * 100;
    }
    return percent;
  }

  public static String percentageIntFormat(double value, double total) {
    double percent = 0;
    if (total != 0) {
      percent = (value / total) * 100;
    }
    return formatToInt(percent);
  }

  public static String percentageFormat(double value, double total) {
    String fValue = formatToInt(value);
    String fPercentage = formatToInt(percentageOf(value, total));
    return fValue + " (" + fPercentage + "%)";
  }
}
