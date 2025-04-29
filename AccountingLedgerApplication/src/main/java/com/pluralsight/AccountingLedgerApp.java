package com.pluralsight;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AccountingLedgerApp {
    private static final String FILE_NAME = "transactions.csv";
    private static List<Transaction> transactions = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static String owner;

    public static void main(String[] args) {
        // 1) Ask for owner/title
        System.out.print("Enter your name or account title: ");
        owner = scanner.nextLine().trim();

        // 2) Load past transactions and compute balances
        loadTransactions();
        calculateBalances();

        // 3) Show custom banner + menu
        showHomeMenu();
    }

    private static void loadTransactions() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                transactions.add(0, Transaction.fromCsv(line));
            }
        } catch (IOException e) {
            System.out.println("Error Did Not Load: " + e.getMessage());
        }
    }

    private static void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(t.toCsv());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error Did Not Save: " + e.getMessage());
        }
    }

    /** Overwrite the entire CSV from current list */
    private static void saveAllTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                pw.println(t.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }

    private static void showHomeMenu() {

        System.out.println("LL      GGGG  TTTTTT   EEEEE  NN   NN ");
        System.out.println("LL     GG       TT     EE     NNN  NN ");
        System.out.println("LL     GG  GGG  TT     EEEE   NN N NN ");
        System.out.println("LL     GG   GG  TT     EE     NN  NNN ");
        System.out.println("LLLLL   GGGGG   TT     EEEEE  NN   NN ");

        System.out.println("\u001B[32mWelcome, " + owner + "! Here's your personalized ledger.\u001B[0m");

        while (true) {
            System.out.println("\n=== Home Screen ===");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D":
                    addDeposit();
                    break;
                case "P":
                    makePayment();
                    break;
                case "L":
                    showLedgerMenu();
                    break;
                case "X":
                    printSummary();
                    System.out.println("Goodbye, " + owner + "!");
                    return;
                default:
                    System.out.println("Invalid choice. Choose one from the list.");
            }
        }
    }

    private static void addDeposit() {
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        System.out.print("Vendor: ");
        String ven = scanner.nextLine();
        System.out.print("Category (e.g. Food, Rent): ");
        String cat = scanner.nextLine();
        System.out.print("Amount: ");
        double amt = Double.parseDouble(scanner.nextLine());
        String[] dt = currentDateTime();

        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println("✅ Deposit recorded.");
    }

    private static void makePayment() {
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        System.out.print("Vendor: ");
        String ven = scanner.nextLine();
        System.out.print("Category (e.g. Utilities, Subscription): ");
        String cat = scanner.nextLine();
        System.out.print("Amount: ");
        double amt = -Math.abs(Double.parseDouble(scanner.nextLine()));
        String[] dt = currentDateTime();

        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println("✅ Payment recorded.");
    }

    private static void showLedgerMenu() {
        while (true) {
            System.out.println("\n=== Ledger ===");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("S) Search");
            System.out.println("R) Remove Transaction(s)");
            System.out.println("H) Home");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    displayTransactions(transactions);
                    break;
                case "D":
                    displayTransactions(filterDeposits());
                    break;
                case "P":
                    displayTransactions(filterPayments());
                    break;
                case "S":
                    searchTransactions();
                    break;
                case "R":
                    deleteTransaction();
                    break;
                case "H":
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static List<Transaction> filterDeposits() {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) out.add(t);
        }
        return out;
    }

    private static List<Transaction> filterPayments() {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) out.add(t);
        }
        return out;
    }

    private static void displayTransactions(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No transactions to display.");
        } else {
            for (Transaction t : list) {
                System.out.println(t);
            }
        }
    }

    private static void calculateBalances() {
        double bal = 0;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            bal += transactions.get(i).getAmount();
            transactions.get(i).setBalance(bal);
        }
    }

    private static void printSummary() {
        double totalDeposits = 0, totalPayments = 0;
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) totalDeposits += t.getAmount();
            else totalPayments += t.getAmount();
        }
        double endingBalance = transactions.isEmpty()
                ? 0
                : transactions.get(0).getBalance();
        System.out.printf(
                "\n=== Summary ===%nDeposits:  $%.2f%nPayments:  $%.2f%nEnding Balance: $%.2f%n",
                totalDeposits,
                -totalPayments,
                endingBalance
        );
    }

    private static String[] currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return new String[]{date, time};
    }

    private static void searchTransactions() {
        System.out.print("Start Date (yyyy-MM-dd) or leave blank: ");
        String startDate = scanner.nextLine().trim();
        System.out.print("End Date (yyyy-MM-dd) or leave blank: ");
        String endDate = scanner.nextLine().trim();
        System.out.print("Description contains or leave blank: ");
        String description = scanner.nextLine().trim().toLowerCase();
        System.out.print("Vendor contains or leave blank: ");
        String vendor = scanner.nextLine().trim().toLowerCase();
        System.out.print("Exact Amount or leave blank: ");
        String amountStr = scanner.nextLine().trim();

        List<Transaction> filtered = new ArrayList<>(transactions);

        if (!startDate.isEmpty()) {
            filtered.removeIf(t -> t.getDate().compareTo(startDate) < 0);
        }
        if (!endDate.isEmpty()) {
            filtered.removeIf(t -> t.getDate().compareTo(endDate) > 0);
        }
        if (!description.isEmpty()) {
            filtered.removeIf(t -> !t.getDescription().toLowerCase().contains(description));
        }
        if (!vendor.isEmpty()) {
            filtered.removeIf(t -> !t.getVendor().toLowerCase().contains(vendor));
        }
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                filtered.removeIf(t -> t.getAmount() != amount);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount entered. Skipping amount filter.");
            }
        }

        displayTransactions(filtered);
    }

    private static void deleteTransaction() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions to delete.");
            return;
        }

        System.out.println("Delete by:");
        System.out.println("  1) ID");
        System.out.println("  2) Date (YYYY-MM-DD)");
        System.out.println("  3) Vendor");
        System.out.print("Choose mode: ");
        String mode = scanner.nextLine().trim();

        switch (mode) {
            case "1":
                // Delete by ID
                for (int i = 0; i < transactions.size(); i++) {
                    System.out.printf("%2d) %s%n", i + 1, transactions.get(i));
                }
                System.out.print("Enter the ID to delete: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (id < 0 || id >= transactions.size()) {
                        System.out.println("ID out of range.");
                        return;
                    }
                    Transaction removed = transactions.remove(id);
                    System.out.println("Deleted: " + removed);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number.");
                    return;
                }
                break;

            case "2":
                // Delete by date
                System.out.print("Enter date (YYYY-MM-DD): ");
                String date = scanner.nextLine().trim();
                List<Transaction> byDate = transactions.stream()
                        .filter(tx -> tx.getDate().equals(date))
                        .collect(Collectors.toList());
                if (byDate.isEmpty()) {
                    System.out.println("No transactions found on " + date);
                    return;
                }
                transactions.removeAll(byDate);
                System.out.println("Removed " + byDate.size() + " transaction(s) on " + date);
                break;

            case "3":
                // Delete by vendor
                System.out.print("Enter vendor name: ");
                String vendorTerm = scanner.nextLine().trim().toLowerCase();
                List<Transaction> byVendor = transactions.stream()
                        .filter(tx -> tx.getVendor().toLowerCase().contains(vendorTerm))
                        .collect(Collectors.toList());
                if (byVendor.isEmpty()) {
                    System.out.println("No transactions found for vendor containing \"" + vendorTerm + "\"");
                    return;
                }
                transactions.removeAll(byVendor);
                System.out.println("Removed " + byVendor.size() + " transaction(s) for vendor \"" + vendorTerm + "\"");
                break;

            default:
                System.out.println("Unknown option.");
                return;
        }

        // Recalculate and persist
        calculateBalances();
        saveAllTransactions();
        System.out.println("Deletion complete.");
    }
}
