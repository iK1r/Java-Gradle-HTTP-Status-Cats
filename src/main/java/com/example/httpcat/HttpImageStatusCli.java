package com.example.httpcat;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

public class HttpImageStatusCli {
    private final HttpStatusImageDownloader downloader;
    private final Scanner scanner;
    private final PrintStream out;

    public HttpImageStatusCli() {
        this(new HttpStatusImageDownloader(), new Scanner(System.in), System.out);
    }

    HttpImageStatusCli(HttpStatusImageDownloader downloader, Scanner scanner, PrintStream out) {
        this.downloader = Objects.requireNonNull(downloader);
        this.scanner = Objects.requireNonNull(scanner);
        this.out = Objects.requireNonNull(out);
    }

    public void askStatus() {
        out.println("Enter HTTP status code");

        if (!scanner.hasNextLine()) {
            out.println("Please enter valid number");
            return;
        }

        int code;
        try {
            code = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            out.println("Please enter valid number");
            return;
        }

        try {
            downloader.downloadStatusImage(code);
        } catch (HttpStatusImageNotFoundException e) {
            // Keep the text exactly as requested in the homework.
            out.println("There is not image for HTTP status " + code);
        } catch (HttpCatException e) {
            out.println("Could not download image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new HttpImageStatusCli().askStatus();
    }
}
