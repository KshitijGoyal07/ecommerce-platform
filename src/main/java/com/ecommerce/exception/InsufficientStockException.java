package com.ecommerce.exception;

/** Raised when an order requests more stock than a product currently has. */
public class InsufficientStockException extends RuntimeException {
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(int requestedQuantity, int availableQuantity) {
        super("Insufficient stock: requested " + requestedQuantity
                + ", but only " + availableQuantity + " available.");
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
