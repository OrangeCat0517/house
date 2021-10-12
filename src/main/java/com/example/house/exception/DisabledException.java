package com.example.house.exception;

public class DisabledException extends RuntimeException{
    private String message;

    public DisabledException(String message) {
        this.message = message;
    }
}
