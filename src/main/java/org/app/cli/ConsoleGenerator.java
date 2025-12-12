package org.app.cli;

import org.app.model.BusinessMode;
import org.app.service.InvoiceGenerationService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConsoleGenerator {
    private ConsoleGenerator() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ConsoleGenerator <FAN|ASSET> <excel-file> [output-csv]");
            System.exit(1);
        }

        BusinessMode mode;
        try {
            mode = BusinessMode.valueOf(args[0].trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.err.println("Unknown mode: " + args[0]);
            System.exit(2);
            return;
        }

        File excelFile = new File(args[1]);
        Path outputCsv = args.length >= 3
                ? Paths.get(args[2])
                : defaultOutputPath(excelFile);
        InvoiceGenerationService service = new InvoiceGenerationService();
        try {
            service.generate(excelFile, mode, outputCsv, System.out::println);
        } catch (Exception e) {
            System.err.println("Processing failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(3);
        }
    }

    private static Path defaultOutputPath(File excelFile) {
        Path excelPath = excelFile.toPath().toAbsolutePath().normalize();
        Path parent = excelPath.getParent();
        Path baseDir = parent != null ? parent : Paths.get(System.getProperty("user.dir"));
        return baseDir.resolve("facturare.csv");
    }
}
