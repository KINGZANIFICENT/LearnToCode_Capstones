package com.pluralsight;

public class Transaction {
    private String date;
    private String time;
    private String description;
    private String vendor;
    private String category;
    private double amount;
    private double balance;

    // constructor
    public Transaction(String date, String time, String description,
                       String vendor, String category, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.category = category;
        this.amount = amount;
        this.balance = 0; // default, will be updated later
    }

    // Getters
    public String getDate()     { return date; }
    public String getTime()     { return time; }
    public String getDescription() { return description; }
    public String getVendor()   { return vendor; }
    public String getCategory() { return category; }
    public double getAmount()   { return amount; }
    public double getBalance()  { return balance; }

    // Setter for balance
    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("%s %s | %-20s | %-15s | %-10s | %10.2f | %10.2f",
                date, time, description, vendor, category, amount, balance);
    }

    // Load a transaction from a pipe-delimited line
    public static Transaction fromCsv(String csvLine) {
        // split on literal '|'
        String[] parts = csvLine.split("\\|");

        // we need at least: date | time | desc | vendor | amount
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid CSV format: " + csvLine);
        }

        String date        = parts[0];
        String time        = parts[1];
        String description = parts[2];
        String vendor      = parts[3];
        double amount      = Double.parseDouble(parts[4]);

        // file has no category column, so assign a default
        String category = "Uncategorized";

        Transaction tx = new Transaction(date, time, description, vendor, category, amount);

        // if there's a sixth part, treat it as balance
        if (parts.length > 5) {
            double balance = Double.parseDouble(parts[5]);
            tx.setBalance(balance);
        }

        return tx;
    }

    // Save as pipe-delimited
    public String toCsv() {
        return String.join("|",
                date,
                time,
                description,
                vendor,
                category,
                String.format("%.2f", amount),
                String.format("%.2f", balance)
        );
    }
}


