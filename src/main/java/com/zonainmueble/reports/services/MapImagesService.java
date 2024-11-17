package com.zonainmueble.reports.services;

import com.zonainmueble.reports.maps.MapImageRequest;

public interface MapImagesService {
  byte[] image(MapImageRequest input);
}
