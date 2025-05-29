// src/com/pluralsight/FileManager/OrderManager.java
package com.pluralsight.FileManager;

import com.pluralsight.Menu.Order;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

/**
 * Persists each order to its own timestamped file under receipts/.
 */
public class OrderManager {
    private static final DateTimeFormatter FILE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter PRINT_FMT =
            DateTimeFormatter.ofPattern("HH:mm, MMM dd yyyy");

    public static void save(Order order) {
        try {
            Path dir = Paths.get("receipts");
            Files.createDirectories(dir);
            String ts   = order.getWhen().format(FILE_FMT);
            Path file   = dir.resolve(ts + ".txt");
            try (BufferedWriter w = Files.newBufferedWriter(file)) {
                w.write(formatOrder(order));
            }
        } catch (IOException ex) {
            System.err.println("Failed to save receipt: " + ex.getMessage());
        }
    }

    private static String formatOrder(Order order) {
        var sb = new StringBuilder();
        sb.append("Order placed at ")
                .append(order.getWhen().format(PRINT_FMT))
                .append("\n\n");
        double total = 0;
        for (var item : order.getItems()) {
            sb.append(item.orderItemDescription())
                    .append(" — $")
                    .append(String.format("%.2f", item.orderItemPrice()))
                    .append("\n");
            total += item.orderItemPrice();
        }
        sb.append("\nTOTAL: $")
                .append(String.format("%.2f", total));
        return sb.toString();
    }
}
