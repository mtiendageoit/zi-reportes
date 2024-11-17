package com.zonainmueble.reports.enums;

public enum NivelMaslow {
  Nivel1(1), Nivel2(2), Nivel3(3), Nivel4(4), Nivel5(5);

  private final int value;

  private NivelMaslow(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
