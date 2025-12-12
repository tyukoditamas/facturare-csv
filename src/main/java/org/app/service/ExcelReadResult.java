package org.app.service;

import org.app.model.SourceRow;

import java.util.Collections;
import java.util.List;

public class ExcelReadResult {
    private final List<SourceRow> rows;
    private final int totalDataRows;
    private final int skippedRowCount;

    public ExcelReadResult(List<SourceRow> rows, int totalDataRows, int skippedRowCount) {
        this.rows = Collections.unmodifiableList(rows);
        this.totalDataRows = totalDataRows;
        this.skippedRowCount = skippedRowCount;
    }

    public List<SourceRow> getRows() {
        return rows;
    }

    public int getTotalDataRows() {
        return totalDataRows;
    }

    public int getSkippedRowCount() {
        return skippedRowCount;
    }
}
