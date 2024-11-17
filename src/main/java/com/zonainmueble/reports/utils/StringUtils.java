package com.zonainmueble.reports.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  private static final Pattern PATTERN = Pattern.compile("\\[(.+?)\\]");

  public static String replaceKeysWithValues(String text, Map<String, Object> params) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    if (params == null || params.isEmpty()) {
      return text;
    }

    Matcher matcher = PATTERN.matcher(text);
    StringBuffer resultString = new StringBuffer();

    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = getReplacementValue(params, key);
      matcher.appendReplacement(resultString, Matcher.quoteReplacement(replacement));
    }

    matcher.appendTail(resultString);

    return resultString.toString();
  }

  private static String getReplacementValue(Map<String, Object> params, String key) {
    Object value = params.get(key);
    return (value != null) ? value.toString() : "";
  }

  public static String alfanumeric(String text) {
    if (text == null || text.length() == 0)
      return text;
    return text.replaceAll("[^\\p{L}\\p{N} ]+", "");
  }
}
