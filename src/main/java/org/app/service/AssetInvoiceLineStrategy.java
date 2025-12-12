package org.app.service;

import org.app.model.InvoiceLine;
import org.app.model.SourceRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class AssetInvoiceLineStrategy implements InvoiceLineStrategy {
    private static final String CUSTOMER_TAX_CODE = "DE351004592";
    private static final String DEVIZ = "EUR";
    private static final String DESC_BASE = "E-COMMERCE DECLARATION";
    private static final String DESC_CUSTOMS = "CUSTOMS CLEARANCE OUTSIDE OPENING HOURS/TYPE OF TRANSPORT";
    private static final BigDecimal VAT_RATE = BigDecimal.ZERO;
    private static final BigDecimal PRICE_ZERO = BigDecimal.ZERO;
    private static final BigDecimal PRICE_CUSTOMS = BigDecimal.valueOf(180);

    @Override
    public List<InvoiceLine> buildLines(SourceRow row) {
        List<InvoiceLine> result = new ArrayList<>(2);
        result.add(buildBaseLine(row));
        if (row.hasCustomsOutsideWorkingHours()) {
            result.add(buildCustomsLine(row));
        }
        return result;
    }

    private InvoiceLine buildBaseLine(SourceRow row) {
        return new InvoiceLine(
                CUSTOMER_TAX_CODE,
                DEVIZ,
                DESC_BASE,
                BigDecimal.valueOf(row.getLinesCount()),
                PRICE_ZERO,
                VAT_RATE,
                row.buildProductNote()
        );
    }

    private InvoiceLine buildCustomsLine(SourceRow row) {
        return new InvoiceLine(
                "",
                DEVIZ,
                DESC_CUSTOMS,
                BigDecimal.ONE,
                PRICE_CUSTOMS,
                VAT_RATE,
                row.buildProductNote()
        );
    }
}
