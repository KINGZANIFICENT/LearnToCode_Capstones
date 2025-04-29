package com.pluralsight;

public class Transaction {
    private String date;
    private String time;
    private String description;
    private String vendor;
    private String category;
    private double amount;
    private double balance;

    // constructor without balance (balance defaults to 0)
    public Transaction(String date, String time, String description,
                       String vendor, String category, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.category = category;
        this.amount = amount;
        this.balance = 0;
    }

    // overloaded constructor with balance
    public Transaction(String date, String time, String description,
                       String vendor, String category,
                       double amount, double balance) {
        this(date, time, description, vendor, category, amount);
        this.balance = balance;
    }

    // Getters
    public String getDate()         { return date; }
    public String getTime()         { return time; }
    public String getDescription()  { return description; }
    public String getVendor()       { return vendor; }
    public String getCategory()     { return category; }
    public double getAmount()       { return amount; }
    public double getBalance()      { return balance; }

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
        String[] parts = csvLine.split("\\|");

        try {
            switch (parts.length) {
                case 5: {
                    // date | time | desc | vendor | amount
                    String date        = parts[0];
                    String time        = parts[1];
                    String description = parts[2];
                    String vendor      = parts[3];
                    double amount      = Double.parseDouble(parts[4]);
                    return new Transaction(date, time, description, vendor, "Uncategorized", amount);
                }
                case 6: {
                    // date | time | desc | vendor | category | amount
                    String date        = parts[0];
                    String time        = parts[1];
                    String description = parts[2];
                    String vendor      = parts[3];
                    String category    = parts[4];
                    double amount      = Double.parseDouble(parts[5]);
                    return new Transaction(date, time, description, vendor, category, amount);
                }
                case 7: {
                    // date | time | desc | vendor | category | amount | balance
                    String date        = parts[0];
                    String time        = parts[1];
                    String description = parts[2];
                    String vendor      = parts[3];
                    String category    = parts[4];
                    double amount      = Double.parseDouble(parts[5]);
                    double balance     = Double.parseDouble(parts[6]);
                    return new Transaction(date, time, description, vendor, category, amount, balance);
                }
                default:
                    throw new IllegalArgumentException(
                            "Invalid CSV format (expected 5–7 fields, got "
                                    + parts.length + "): " + csvLine);
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "Invalid numeric value in CSV: " + csvLine, nfe);
        }
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


