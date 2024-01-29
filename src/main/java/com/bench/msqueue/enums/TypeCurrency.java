package com.bench.msqueue.enums;

public enum TypeCurrency {

    ARS("ARS"),
    USD("USD");

    private String type;

    TypeCurrency(String type) {
        this.setType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    }
