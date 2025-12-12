package org.app.model;

import java.util.Objects;

/**
 * Represents one validated Excel data row.
 */
public class SourceRow {
    private final int excelRowNumber; // 1-based index in the Excel file
    private final String mrn;
    private final int linesCount;
    private final boolean customsOutsideWorkingHours;

    public SourceRow(int excelRowNumber, String mrn, int linesCount, boolean customsOutsideWorkingHours) {
        this.excelRowNumber = excelRowNumber;
        this.mrn = Objects.requireNonNull(mrn, "mrn");
        this.linesCount = linesCount;
        this.customsOutsideWorkingHours = customsOutsideWorkingHours;
    }

    public int getExcelRowNumber() {
        return excelRowNumber;
    }

    public String getMrn() {
        return mrn;
    }

    public int getLinesCount() {
        return linesCount;
    }

    public boolean hasCustomsOutsideWorkingHours() {
        return customsOutsideWorkingHours;
    }

    public String buildProductNote() {
        return "OTP " + mrn;
    }
}
