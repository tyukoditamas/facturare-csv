package org.app.model;

import java.math.BigDecimal;
import java.util.Objects;

public class InvoiceLine {
    private final Integer nrCrt;
    private final String customerTaxCode;
    private final String deviz;
    private final String productDescription;
    private final BigDecimal quantity;
    private final BigDecimal priceWithoutVat;
    private final BigDecimal vatRate;
    private final String productNote;

    public InvoiceLine(Integer nrCrt,
                       String customerTaxCode,
                       String deviz,
                       String productDescription,
                       BigDecimal quantity,
                       BigDecimal priceWithoutVat,
                       BigDecimal vatRate,
                       String productNote) {
        this.nrCrt = Objects.requireNonNull(nrCrt, "nr.crt");
        this.customerTaxCode = Objects.requireNonNull(customerTaxCode, "CIF/CNP");
        this.deviz = Objects.requireNonNull(deviz, "deviz");
        this.productDescription = Objects.requireNonNull(productDescription, "produs");
        this.quantity = Objects.requireNonNull(quantity, "cantitate");
        this.priceWithoutVat = Objects.requireNonNull(priceWithoutVat, "pret FTVA");
        this.vatRate = Objects.requireNonNull(vatRate, "cota TVA");
        this.productNote = Objects.requireNonNull(productNote, "productNote");
    }

    public Integer getNrCrt() {
        return nrCrt;
    }

    public String getCustomerTaxCode() {
        return customerTaxCode;
    }

    public String getDeviz() {
        return deviz;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceWithoutVat() {
        return priceWithoutVat;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public String getProductNote() {
        return productNote;
    }

}
