package org.app.service;

import org.app.model.InvoiceLine;
import org.app.model.SourceRow;

import java.util.List;

public interface InvoiceLineStrategy {
    List<InvoiceLine> buildLines(SourceRow row);
}
