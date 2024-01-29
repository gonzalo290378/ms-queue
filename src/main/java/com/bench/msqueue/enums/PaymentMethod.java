package com.bench.msqueue.enums;

public enum PaymentMethod {
    TRANSFER("TRANSFER"),
    ECHEQ("ECHEQ");

    private String paymentMethod;

    PaymentMethod(String paymentMethod) {
        this.setPaymentMethod(paymentMethod);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
