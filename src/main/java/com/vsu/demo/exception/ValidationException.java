package com.vsu.demo.exception;

import com.vsu.demo.response.ErrorCode;

public class ValidationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ValidationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
