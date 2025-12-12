package org.app.service;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.app.model.SourceRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ExcelSourceReader {
    private static final int MRN_COLUMN_INDEX = 2;
    private static final int LINES_COLUMN_INDEX = 7;
    private static final int CUSTOMS_COLUMN_INDEX = 11;

    private final DataFormatter dataFormatter = new DataFormatter(Locale.US, true);

    public ExcelReadResult read(File excelFile, Consumer<String> logger) throws IOException {
        List<SourceRow> validRows = new ArrayList<>();
        int totalDataRows = 0;
        int skippedRows = 0;

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            if (workbook.getNumberOfSheets() == 0) {
                logger.accept("The Excel file does not contain any sheets.");
                return new ExcelReadResult(validRows, 0, 0);
            }

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowIndex = sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= lastRowIndex; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                int excelRowNumber = rowIndex + 1; // Excel rows are 1-based
                if (row == null || isRowEmpty(row, evaluator)) {
                    logger.accept(String.format("WARN: Row %d skipped: empty row", excelRowNumber));
                    continue;
                }

                totalDataRows++;

                String mrn = readString(row.getCell(MRN_COLUMN_INDEX), evaluator);
                if (mrn == null || mrn.isEmpty()) {
                    skippedRows++;
                    logger.accept(String.format("ERROR: Row %d ignored: missing MRN", excelRowNumber));
                    continue;
                }

                Integer linesCount = readInteger(row.getCell(LINES_COLUMN_INDEX), evaluator);
                if (linesCount == null) {
                    skippedRows++;
                    logger.accept(String.format("ERROR: Row %d ignored: invalid 'linii' value", excelRowNumber));
                    continue;
                }

                boolean hasCustomsLine = hasCustomsFlag(row.getCell(CUSTOMS_COLUMN_INDEX), evaluator);
                validRows.add(new SourceRow(excelRowNumber, mrn.trim(), linesCount, hasCustomsLine));
            }
        } catch (EncryptedDocumentException e) {
            throw new IOException("The Excel file appears to be encrypted and cannot be read.", e);
        }

        return new ExcelReadResult(validRows, totalDataRows, skippedRows);
    }

    private boolean isRowEmpty(Row row, FormulaEvaluator evaluator) {
        return isCellBlank(row.getCell(MRN_COLUMN_INDEX), evaluator)
                && isCellBlank(row.getCell(LINES_COLUMN_INDEX), evaluator)
                && isCellBlank(row.getCell(CUSTOMS_COLUMN_INDEX), evaluator);
    }

    private boolean isCellBlank(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return true;
        }
        if (cell.getCellType() == CellType.BLANK) {
            return true;
        }
        String formatted = dataFormatter.formatCellValue(cell, evaluator);
        return formatted == null || formatted.trim().isEmpty();
    }

    private String readString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        return dataFormatter.formatCellValue(cell, evaluator).trim();
    }

    private Integer readInteger(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) Math.round(cell.getNumericCellValue());
        }
        String value = dataFormatter.formatCellValue(cell, evaluator).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.replace(',', '.'));
            return (int) Math.round(parsed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean hasCustomsFlag(Cell cell, FormulaEvaluator evaluator) {
        Integer value = readInteger(cell, evaluator);
        return value != null && value == 180;
    }
}
