package com.example.httpcat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

public class HttpCatSelfTest {
    private static final byte[] IMAGE = {(byte) 0xff, (byte) 0xd8, 10, 20, (byte) 0xff, (byte) 0xd9};

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/200.jpg", exchange -> send(exchange, 200, IMAGE));
        server.createContext("/404.jpg", exchange -> send(exchange, 200, IMAGE));
        server.createContext("/503.jpg", exchange -> send(exchange, 503, new byte[0]));
        server.start();

        Path testDir = Files.createTempDirectory("httpcat-test-");
        try {
            URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpStatusChecker checker = new HttpStatusChecker(client, base);
            HttpStatusImageDownloader downloader = new HttpStatusImageDownloader(checker, client, testDir);

            verify((base + "200.jpg").equals(checker.getStatusImage(200)), "Checker did not return URL");
            verify((base + "404.jpg").equals(checker.getStatusImage(404)), "404.jpg should be available");
            expectMissing(() -> checker.getStatusImage(10000));
            expectNetworkError(() -> checker.getStatusImage(503));
            System.out.println("Checker: OK");

            downloader.downloadStatusImage(200);
            verify(Arrays.equals(IMAGE, Files.readAllBytes(testDir.resolve("200.jpg"))),
                    "Downloaded file contents are wrong");
            expectMissing(() -> downloader.downloadStatusImage(10000));
            verify(!Files.exists(testDir.resolve("10000.jpg")), "Nonexistent image was saved");
            System.out.println("Downloader: OK");

            verify(cliOutput(downloader, "test").contains("Please enter valid number"),
                    "CLI did not handle nonnumeric input");
            verify(cliOutput(downloader, "10000").contains("There is not image for HTTP status 10000"),
                    "CLI did not handle unknown code");
            verify(cliOutput(downloader, "200").contains("Enter HTTP status code"),
                    "CLI prompt was not displayed");
            System.out.println("CLI: OK");
            System.out.println("All local HTTP tests passed!");
        } finally {
            server.stop(0);
            try (var files = Files.list(testDir)) {
                for (Path file : files.toList()) Files.deleteIfExists(file);
            }
            Files.deleteIfExists(testDir);
        }
    }

    private static void send(HttpExchange exchange, int code, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
        if (code == 200) {
            exchange.sendResponseHeaders(code, data.length);
            try (var body = exchange.getResponseBody()) {
                body.write(data);
            }
        } else {
            exchange.sendResponseHeaders(code, -1);
            exchange.close();
        }
    }

    private static String cliOutput(HttpStatusImageDownloader downloader, String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);
             Scanner scanner = new Scanner(input + "\n")) {
            new HttpImageStatusCli(downloader, scanner).askStatus(output);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void expectMissing(Runnable task) {
        try {
            task.run();
            throw new AssertionError("Expected missing image exception");
        } catch (HttpStatusImageNotFoundException expected) {
            // This is the expected result.
        }
    }

    private static void expectNetworkError(Runnable task) {
        try {
            task.run();
            throw new AssertionError("Expected HTTP error exception");
        } catch (HttpStatusImageNotFoundException wrong) {
            throw new AssertionError("Server error must not look like a missing image", wrong);
        } catch (HttpCatException expected) {
            // This is the expected result.
        }
    }

    private static void verify(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
