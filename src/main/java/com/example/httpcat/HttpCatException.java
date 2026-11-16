package com.example.httpcat;

public class HttpCatException extends RuntimeException {
    public HttpCatException(String message) {
        super(message);
    }

    public HttpCatException(String message, Throwable cause) {
        super(message, cause);
    }
}
