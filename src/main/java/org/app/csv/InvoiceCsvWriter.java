package org.app.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.app.model.InvoiceLine;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class InvoiceCsvWriter {
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(
                    "nr.crt",
                    "CIF/CNP",
                    "den. client",
                    "deviz",
                    "Produs",
                    "Serie produs",
                    "Cant",
                    "UM",
                    "Pret FTVA",
                    "cota TVA",
                    "nota produs",
                    "scutit TVA (0/1)",
                    "motiv scutire TVA"
            ).build();

    public Path write(Path outputPath, List<InvoiceLine> lines) throws IOException {
        Path absolute = outputPath.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        boolean append = Files.exists(absolute) && Files.size(absolute) > 0;
        CSVFormat format = append ? CSVFormat.DEFAULT : CSV_FORMAT;
        StandardOpenOption[] options = append
                ? new StandardOpenOption[]{StandardOpenOption.APPEND}
                : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

        try (Writer writer = Files.newBufferedWriter(absolute, StandardCharsets.UTF_8, options);
             CSVPrinter printer = new CSVPrinter(writer, format)) {
            int rowIndex = 0;
            for (InvoiceLine line : lines) {
                boolean includeCustomerFields = !append && rowIndex == 0;
                String customerTaxCode = includeCustomerFields ? line.getCustomerTaxCode() : "";
                String customerName = includeCustomerFields ? line.getCustomerName() : "";
                rowIndex++;
                printer.printRecord(
                        line.getNrCrt(),
                        customerTaxCode == null ? "" : customerTaxCode,
                        customerName == null ? "" : customerName,
                        line.getDeviz(),
                        line.getProductDescription(),
                        "",
                        formatNumber(line.getQuantity()),
                        "BUC",
                        formatNumber(line.getPriceWithoutVat()),
                        formatNumber(line.getVatRate()),
                        line.getProductNote(),
                        "",
                        ""
                );
            }
        }
        return absolute;
    }

    private String formatNumber(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }
}
