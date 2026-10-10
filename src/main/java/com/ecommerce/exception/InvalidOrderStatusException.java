package com.ecommerce.exception;

import com.ecommerce.model.OrderStatus;

/** Raised when an order is moved through an unsupported status transition. */
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(OrderStatus current, OrderStatus requested) {
        super("Order status cannot change from " + current + " to " + requested + ".");
    }
}
