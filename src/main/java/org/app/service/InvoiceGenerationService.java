package org.app.service;

import org.app.csv.InvoiceCsvWriter;
import org.app.model.BusinessMode;
import org.app.model.InvoiceLine;
import org.app.model.SourceRow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class InvoiceGenerationService {
    private final ExcelSourceReader sourceReader = new ExcelSourceReader();
    private final InvoiceCsvWriter csvWriter = new InvoiceCsvWriter();
    private final Map<BusinessMode, InvoiceLineStrategy> strategies = new EnumMap<>(BusinessMode.class);

    public InvoiceGenerationService() {
        strategies.put(BusinessMode.FAN, new FanInvoiceLineStrategy());
        strategies.put(BusinessMode.ASSET, new AssetInvoiceLineStrategy());
    }

    public Path generate(File excelFile, BusinessMode mode, Path outputCsv, Consumer<String> logger) throws IOException {
        Objects.requireNonNull(excelFile, "excelFile");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(outputCsv, "outputCsv");
        Objects.requireNonNull(logger, "logger");

        ensureSupportedFile(excelFile);
        logger.accept(String.format("Processing '%s' as %s", excelFile.getName(), mode.getDisplayName()));

        ExcelReadResult readResult = sourceReader.read(excelFile, logger);
        List<SourceRow> rows = readResult.getRows();
        logger.accept(String.format("Excel data rows detected: %d", readResult.getTotalDataRows()));
        logger.accept(String.format("Rows skipped due to validation: %d", readResult.getSkippedRowCount()));

        InvoiceLineStrategy strategy = strategies.get(mode);
        List<InvoiceLine> invoiceLines = new ArrayList<>(rows.size() * 3);
        int customsLines = 0;
        Path normalizedOutput = outputCsv.toAbsolutePath().normalize();
        for (SourceRow row : rows) {
            List<InvoiceLine> linesForRow = strategy.buildLines(row);
            invoiceLines.addAll(linesForRow);
            if (row.hasCustomsOutsideWorkingHours()) {
                customsLines++;
            }
            logger.accept(String.format("Row %d (MRN %s): customs line %s",
                    row.getExcelRowNumber(),
                    row.getMrn(),
                    row.hasCustomsOutsideWorkingHours() ? "GENERATED (180 flag present)" : "SKIPPED"));
        }

        logger.accept(String.format("Excel rows processed: %d", rows.size()));
        logger.accept(String.format("Rows with customs line: %d", customsLines));
        logger.accept(String.format("Total CSV invoice lines: %d", invoiceLines.size()));

        boolean appendExisting = Files.exists(normalizedOutput) && Files.size(normalizedOutput) > 0;
        logger.accept(String.format("%s CSV file: %s",
                appendExisting ? "Appending to" : "Creating",
                normalizedOutput));

        Path csvPath = csvWriter.write(normalizedOutput, invoiceLines);
        logger.accept("CSV updated: " + csvPath);
        return csvPath;
    }

    private void ensureSupportedFile(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IOException("The selected file does not exist or is not a regular file: " + file);
        }
        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        if (!(lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx"))) {
            throw new IOException("Unsupported file type. Please select an .xls or .xlsx file.");
        }
    }

}
