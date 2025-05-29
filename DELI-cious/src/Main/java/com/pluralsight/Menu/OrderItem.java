package com.pluralsight.Menu;

/**
 * Represents a general item that can be ordered (sandwich, side, etc.).
 */
public interface OrderItem {

    /**
     * A human-readable description of the item for receipts.
     * @return description text
     */
    String orderItemDescription();

    /**
     * The price of the item for calculating totals.
     * @return price in dollars
     */
    double orderItemPrice();
}
