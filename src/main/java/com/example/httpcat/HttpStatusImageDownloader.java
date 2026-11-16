package com.example.httpcat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

public class HttpStatusImageDownloader {
    private final HttpStatusChecker checker;
    private final HttpClient httpClient;
    private final Path directory;

    public HttpStatusImageDownloader() {
        this(new HttpStatusChecker(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), Path.of("."));
    }

    // Separate output folder and local HTTP server are used in tests.
    HttpStatusImageDownloader(HttpStatusChecker checker, HttpClient httpClient, Path directory) {
        this.checker = Objects.requireNonNull(checker);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.directory = Objects.requireNonNull(directory);
    }

    public void downloadStatusImage(int code) {
        String url = checker.getStatusImage(code);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());

            // The image could become unavailable between the two requests.
            if (response.statusCode() == 404) {
                throw new HttpStatusImageNotFoundException(code);
            }
            if (response.statusCode() != 200) {
                throw new HttpCatException(
                        "Download failed. Server responded with HTTP " + response.statusCode());
            }

            Path file = directory.resolve(code + ".jpg");
            Files.write(file, response.body());
            System.out.println("Downloaded image: " + file.toAbsolutePath());
        } catch (IOException e) {
            throw new HttpCatException("Cannot download or save image for status " + code, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpCatException("Image download was interrupted", e);
        }
    }
}
