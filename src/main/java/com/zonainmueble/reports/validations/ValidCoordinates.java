package com.zonainmueble.reports.validations;

import java.lang.annotation.*;

import jakarta.validation.*;

@Constraint(validatedBy = CoordinateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCoordinates {
  String message() default "Invalid coordinates. Latitude must be between -90 and 90, and Longitude must be between -180 and 180.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
