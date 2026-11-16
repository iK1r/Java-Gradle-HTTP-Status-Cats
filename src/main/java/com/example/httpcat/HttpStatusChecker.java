package com.example.httpcat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class HttpStatusChecker {
    private final HttpClient httpClient;
    private final URI baseUri;

    public HttpStatusChecker() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), URI.create("https://http.cat/"));
    }

    // A different URL can be supplied during local tests.
    HttpStatusChecker(HttpClient httpClient, URI baseUri) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.baseUri = Objects.requireNonNull(baseUri);
    }

    public String getStatusImage(int code) {
        URI imageUri = baseUri.resolve(code + ".jpg");
        HttpRequest request = HttpRequest.newBuilder(imageUri)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                return imageUri.toString();
            }
            if (response.statusCode() == 404) {
                throw new HttpStatusImageNotFoundException(code);
            }
            throw new HttpCatException(
                    "Could not check image for status " + code +
                            ". Server responded with HTTP " + response.statusCode());
        } catch (IOException e) {
            throw new HttpCatException("Cannot connect to http.cat", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpCatException("HTTP request was interrupted", e);
        }
    }
}
