// src/com/pluralsight/Menu/Sandwich.java
package com.pluralsight.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a customizable sandwich with bread, size, toast option, toppings, and sauces.
 */
public class Sandwich implements OrderItem {
    private static final Map<String,Double> BREAD_COSTS = Map.of(
            "White", 0.00,
            "Wheat", 0.10,
            "Rye",   0.20,
            "Wrap",  0.30
    );

    private final String bread;
    private final int sizeInches;
    private final boolean toasted;
    private final List<Topping> toppings;
    private final List<Topping> sauces;

    private Sandwich(Builder b) {
        this.bread      = b.bread;
        this.sizeInches = b.sizeInches;
        this.toasted    = b.toasted;
        this.toppings   = List.copyOf(b.toppings);
        this.sauces     = List.copyOf(b.sauces);
    }

    @Override
    public String orderItemDescription() {
        String topDesc   = toppings.isEmpty() ? "no toppings" : "toppings " + toppings;
        String sauceDesc = sauces.isEmpty()   ? "no sauces"   : "sauces "   + sauces;
        return String.format("%d\" %s on %s with %s and %s",
                sizeInches,
                toasted ? "toasted" : "plain",
                bread,
                topDesc,
                sauceDesc
        );
    }

    @Override
    public double orderItemPrice() {
        double price = sizeInches * 0.50;               // base by size
        price += BREAD_COSTS.getOrDefault(bread, 0.0); // bread surcharge
        for (Topping t : toppings) price += t.orderItemPrice();
        for (Topping s : sauces)   price += s.orderItemPrice();
        return price;
    }

    public static class Builder {
        private String bread;
        private int sizeInches;
        private boolean toasted;
        private final List<Topping> toppings = new ArrayList<>();
        private final List<Topping> sauces   = new ArrayList<>();

        public Builder bread(String b)        { bread = b;      return this; }
        public Builder size(int in)          { sizeInches = in; return this; }
        public Builder toasted(boolean f)    { toasted = f;    return this; }
        public Builder addTopping(Topping t) { toppings.add(t); return this; }
        public Builder addSauce(Topping s)   { sauces.add(s);   return this; }
        public Sandwich build()              { return new Sandwich(this); }
    }
}
