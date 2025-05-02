package com.pluralsight.ui;

import com.pluralsight.Transaction;

import java.util.List;
import java.util.Map;

public class UIUtils {
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";


    public static void printBanner(String text) {
        System.out.println(CYAN + BOLD + "=== " + text + " ===" + RESET);
    }

    /**
     * 2) Spinner animation
     */
    public static void spinner(String message, Runnable work) {
        Thread spin = new Thread(() -> {
            String[] frames = {"|", "/", "-", "\\"};
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("\r" + message + " " + frames[i++ % frames.length]);
                try { Thread.sleep(100); } catch (InterruptedException ex) { break; }
            }
        });
        spin.start();
        work.run();
        spin.interrupt();
        System.out.print("\r" + message + " ✓\n");
    }

    /**
     * 3) ASCII table for transactions including Category column
     */
    public static void printTable(List<Transaction> list) {
        // Header line with Category column added
        System.out.format("%s+------------+--------+----------------------+-----------------+-----------------+----------+%n%s",
                CYAN, RESET);
        System.out.format("%s| Date       | Time   | Description          | Vendor          | Category        | Amount   |%n%s",
                BOLD, RESET);
        System.out.format("%s+------------+--------+----------------------+-----------------+-----------------+----------+%n%s",
                CYAN, RESET);
        for (Transaction t : list) {
            String col = t.getAmount() > 0 ? GREEN : RED;
            System.out.format(
                    "| %s | %s | %-20s | %-15s | %-15s | %s%8.2f%s |%n",
                    t.getDate(),
                    t.getTime(),
                    t.getDescription(),
                    t.getVendor(),
                    t.getCategory(),
                    col,
                    t.getAmount(),
                    RESET
            );
        }
        System.out.format("%s+------------+--------+----------------------+-----------------+-----------------+----------+%n%s",
                CYAN, RESET);
    }

    /**
     * 4) Spending bar chart
     */
    public static void printSpendingChart(Map<String, Double> spend) {
        double max = spend.values().stream().mapToDouble(d -> Math.abs(d)).max().orElse(1);
        System.out.println(BOLD + "\nSpending by Category:" + RESET);
        spend.forEach((cat, amt) -> {
            int len = (int)(Math.abs(amt) / max * 30);
            System.out.printf("%-12s | %s%n", cat, "█".repeat(len));
        });
    }

    /**
     * 5) Sparkline of balances
     */
    public static void printBalanceSparkline(List<Transaction> list) {
        String bars = "▁▂▃▄▅▆▇█";
        double min = list.stream().mapToDouble(Transaction::getBalance).min().orElse(0);
        double max = list.stream().mapToDouble(Transaction::getBalance).max().orElse(1);
        System.out.print(BOLD + "\nBalance over time: " + RESET);
        for (Transaction t : list) {
            int idx = (int)((t.getBalance() - min) / (max - min + 1e-6) * (bars.length() - 1));
            System.out.print(bars.charAt(idx));
        }
        System.out.println();
    }

    /**
     * 6) Export transactions to Markdown
     */
    public static String toMarkdown(List<Transaction> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("| Date       | Time   | Description          | Vendor          | Category        | Amount   |\n")
                .append("|------------|--------|----------------------|-----------------|-----------------|----------|\n");
        for (Transaction t : list) {
            sb.append(String.format(
                    "| %s | %s | %-20s | %-15s | %-15s | %8.2f |\n",
                    t.getDate(),
                    t.getTime(),
                    t.getDescription(),
                    t.getVendor(),
                    t.getCategory(),
                    t.getAmount()
            ));
        }
        return sb.toString();
    }
}
