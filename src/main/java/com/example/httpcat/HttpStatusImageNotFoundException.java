package com.example.httpcat;

public class HttpStatusImageNotFoundException extends HttpCatException {
    public HttpStatusImageNotFoundException(int code) {
        super("There is no image for HTTP status " + code);
    }
}
