package com.smartshop.entity;

public enum OrderStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmée"),
    CANCELED("Annulée"),
    REJECTED("Rejetée");


    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFinal() {
        return this == CONFIRMED || this == REJECTED || this == CANCELED;
    }


    public boolean canBeConfirmed() {
        return this == PENDING;
    }

    public boolean canBeCanceled() {
        return this == PENDING;
    }
}
