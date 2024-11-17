package com.zonainmueble.reports.exceptions;

import lombok.*;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private String code;
    private String message;
}
