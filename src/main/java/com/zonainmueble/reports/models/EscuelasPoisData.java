package com.zonainmueble.reports.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscuelasPoisData {
  private String tipo;
  private String nombre;
  private String direccion;
  private long distancia;
}
