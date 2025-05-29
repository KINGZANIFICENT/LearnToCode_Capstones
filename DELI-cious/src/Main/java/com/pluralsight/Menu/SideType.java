package com.pluralsight.Menu;

/**
 * Defines the types of sides available, along with their base prices.
 */
public enum SideType {
    DRINK("Drink", 1.50),
    CHIPS("Chips", 1.00);

    public final String label;
    public final double basePrice;

    SideType(String label, double basePrice) {
        this.label = label;
        this.basePrice = basePrice;
    }
}
