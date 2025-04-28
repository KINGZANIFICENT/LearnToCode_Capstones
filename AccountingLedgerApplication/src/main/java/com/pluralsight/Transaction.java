package com.pluralsight;

public class Transaction {
    private String date;
    private String time;
    private String description;
    private String vendor;
    private String category;
    private double amount;
    private double balance;      // running balance

    public Transaction(String date, String time,
                       String description, String vendor,
                       String category, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.category = category;
        this.amount = amount;
    }

    public static Transaction fromCsv(String line) {
        String[] p = line.split("\\|");
        if (p.length < 5 || p.length > 6) {
            throw new IllegalArgumentException("Invalid CSV line: " + line);
        }

        String date        = p[0];
        String time        = p[1];
        String description = p[2];
        String vendor      = p[3];

        String category;
        double amount;

        if (p.length == 6) {
            // new format: category present
            category = p[4];
            amount   = Double.parseDouble(p[5]);
        } else {
            // old format: no category
            category = "";
            amount   = Double.parseDouble(p[4]);
        }

        return new Transaction(date, time, description, vendor, category, amount);
    }


    public String toCsv() {
        return date + "|"
                + time + "|"
                + description + "|"
                + vendor + "|"
                + category + "|"
                + amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setBalance(double b) {
        this.balance = b;
    }

    // ← Add this getter so you can read the balance elsewhere
    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return date + " " + time
                + " | " + description
                + " | " + vendor
                + " | " + category
                + String.format(" | %8.2f", amount)
                + String.format(" | Bal: %8.2f", balance);
    }
}

