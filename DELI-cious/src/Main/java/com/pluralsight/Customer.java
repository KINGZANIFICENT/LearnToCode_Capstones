package com.pluralsight;

import com.pluralsight.Menu.Order;

/**
 * Represents a customer placing an order.
 */
public class Customer {
    private final String name;
    private final Order order;

    /**
     * Constructs a customer with the given name and an empty order.
     * @param name the customer's name
     */
    public Customer(String name) {
        this.name = name;
        this.order = new Order();
    }

    /**
     * Constructs a customer with the given name and order.
     * @param name the customer's name
     * @param order the pre-existing order
     */
    public Customer(String name, Order order) {
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return String.format("Customer '%s' with %d item(s)",
                name, order.getItems().size());
    }
}
