package com.pdnt.restaurant.exceptions;

import lombok.Data;

@Data
public class WebException extends RuntimeException {
    private ErrorCode errorCode;

    public WebException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
