// src/com/pluralsight/Menu/Topping.java
package com.pluralsight.Menu;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a topping or sauce, with category and optional quantity.
 */
public class Topping {
    public enum Category { REGULAR, PREMIUM }

    private final String name;
    private final Category category;
    private final int quantity;

    public Topping(String name, Category cat) {
        this(name, cat, 1);
    }
    public Topping(String name, Category cat, int qty) {
        this.name     = name;
        this.category = cat;
        this.quantity = qty;
    }

    public String getName()           { return name; }
    public Category getCategory()     { return category; }
    public int getQuantity()          { return quantity; }

    @Override
    public String toString() {
        return (quantity > 1 ? quantity + "× " : "") + name;
    }

    public String orderItemDescription() {
        return toString();
    }

    /** Premium toppings cost $0.50 each; regular are free. */
    public double orderItemPrice() {
        return category == Category.PREMIUM
                ? 0.50 * quantity
                : 0.0;
    }

    public static List<Topping> availableToppings() {
        return Arrays.asList(
                new Topping("Lettuce", Category.REGULAR),
                new Topping("Tomato",  Category.REGULAR),
                new Topping("Onion",   Category.REGULAR),
                new Topping("Pickles", Category.REGULAR),
                new Topping("Cheese",  Category.PREMIUM),
                new Topping("Ham",     Category.PREMIUM),
                new Topping("Turkey",  Category.PREMIUM),
                new Topping("Bacon",   Category.PREMIUM)
        );
    }

    public static List<Topping> availableSauces() {
        return Arrays.asList(
                new Topping("Mayo",     Category.REGULAR),
                new Topping("Mustard",  Category.REGULAR),
                new Topping("Ketchup",  Category.REGULAR),
                new Topping("BBQ",      Category.REGULAR)
        );
    }
}
