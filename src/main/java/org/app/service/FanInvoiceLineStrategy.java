package org.app.service;

import org.app.model.InvoiceLine;
import org.app.model.SourceRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class FanInvoiceLineStrategy implements InvoiceLineStrategy {
    private static final String CUSTOMER_TAX_CODE = "RO45827190";
    private static final String DEVIZ = "RON";
    private static final String DESC_BASE = "Prestatii H7";
    private static final String DESC_TIR = "inregistrare carnet TIR";
    private static final String DESC_CUSTOMS = "VAMUIRE IN AFARA ORELOR DE PROGRAM/MIJLOC DE TRANSPORT";
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(21);
    private static final BigDecimal PRICE_ZERO = BigDecimal.ZERO;
    private static final BigDecimal PRICE_TIR = BigDecimal.valueOf(15);
    private static final BigDecimal PRICE_CUSTOMS = BigDecimal.valueOf(180);

    @Override
    public List<InvoiceLine> buildLines(SourceRow row) {
        List<InvoiceLine> result = new ArrayList<>(3);
        result.add(buildBaseLine(row));
        result.add(buildTirLine(row));
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

    private InvoiceLine buildTirLine(SourceRow row) {
        return new InvoiceLine(
                CUSTOMER_TAX_CODE,
                DEVIZ,
                DESC_TIR,
                BigDecimal.ONE,
                PRICE_TIR,
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
