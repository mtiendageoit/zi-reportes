package com.zonainmueble.reports.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ReportException.class)
  public ResponseEntity<ErrorResponse> handleReportException(ReportException ex) {
    return new ResponseEntity<>(new ErrorResponse(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(LocationDataUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleLocationDataUnavailableException(LocationDataUnavailableException ex) {
    return new ResponseEntity<>(new ErrorResponse(ex.getCode(), ex.getMessage()), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(
        new ErrorResponse("INTERNAL_SERVER_ERROR", "Ocurrió un error interno, por favor intenta más tarde."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ErrorResponse> handleThrowable(Throwable ex) {
    log.error("Fatal error: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse("UNKNOWN_ERROR", "Ocurrió un error desconocido."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
