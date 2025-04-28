package com.pluralsight;

public class Transaction {
    private String date;
    private String time;
    private String description;
    private String vendor;
    private double amount;

    public Transaction(String date, String time, String description, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public static Transaction fromCsv(String line) {
        String[] parts = line.split("\\|");
        String date        = parts[0];
        String time        = parts[1];
        String description = parts[2];
        String vendor      = parts[3];
        double amount      = Double.parseDouble(parts[4]);
        return new Transaction(date, time, description, vendor, amount);
    }

    public String toCsv() {
        return date + "|" + time + "|" + description + "|" + vendor + "|" + amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return date + " " + time
                + " | " + description
                + " | " + vendor
                + " | " + amount;
    }
}
