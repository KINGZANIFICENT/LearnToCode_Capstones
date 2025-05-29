package com.pluralsight.Menu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's order consisting of multiple items.
 */
public class Order {
    private final LocalDateTime when = LocalDateTime.now();
    private final List<OrderItem> items = new ArrayList<>();

    public LocalDateTime getWhen() {
        return when;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    /**
     * Prints the full order to the console, including timestamp, line items, and total.
     */
    public void showCompleteOrder() {
        System.out.println("Order placed at " +
                when.format(DateTimeFormatter.ofPattern("HH:mm, MMM dd yyyy")));
        if (items.isEmpty()) {
            System.out.println("  (no items)");
            return;
        }
        double total = 0;
        for (OrderItem it : items) {
            System.out.printf("  %-30s $%.2f%n",
                    it.orderItemDescription(), it.orderItemPrice());
            total += it.orderItemPrice();
        }
        System.out.printf("TOTAL: $%.2f%n", total);
    }
}
