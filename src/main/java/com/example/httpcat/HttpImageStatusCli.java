
package com.example.httpcat;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

public class HttpImageStatusCli {
    private final HttpStatusImageDownloader downloader;
    private final Scanner scanner;

    public static void main(String[] args) {
        HttpStatusImageDownloader downloader = new HttpStatusImageDownloader();
        Scanner scanner = new Scanner(System.in);

        HttpImageStatusCli cli = new HttpImageStatusCli(downloader, scanner);
        cli.askStatus();
    }

    HttpImageStatusCli(HttpStatusImageDownloader downloader, Scanner scanner) {
        this.downloader = Objects.requireNonNull(downloader);
        this.scanner = Objects.requireNonNull(scanner);
    }

    public void askStatus() {
        askStatus(System.out);
    }

    // This version is used in local tests.
    void askStatus(PrintStream output) {
        output.println("Enter HTTP status code");

        if (!scanner.hasNextLine()) {
            output.println("Please enter valid number");
            return;
        }

        int code;
        try {
            code = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            output.println("Please enter valid number");
            return;
        }

        try {
            downloader.downloadStatusImage(code);
        } catch (HttpStatusImageNotFoundException e) {
            output.println("There is not image for HTTP status " + code);
        } catch (HttpCatException e) {
            output.println("Could not download image: " + e.getMessage());
        }
    }
}