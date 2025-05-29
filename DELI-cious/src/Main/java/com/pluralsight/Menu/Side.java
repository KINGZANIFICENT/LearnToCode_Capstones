package com.pluralsight.Menu;

/**
 * Represents a side item (drink or chips) with a flavor.
 */
public class Side implements OrderItem {
    private final SideType type;
    private final String flavor;

    public Side(SideType type, String flavor) {
        this.type   = type;
        this.flavor = flavor;
    }

    @Override
    public String orderItemDescription() {
        return type.label + " (" + flavor + ")";
    }

    @Override
    public double orderItemPrice() {
        return type.basePrice;
    }
}
