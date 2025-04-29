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
                try {
                    transactions.add(0, Transaction.fromCsv(line));
                } catch (IllegalArgumentException ignored) {
                    // skip invalid lines
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }

    /** Append a single new transaction to the CSV */
    private static void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(t.toCsv());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    /** Rewrites the entire CSV from the in-memory list */
    private static void saveAllTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                pw.println(t.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }

    // LG10 banner and home screen
    private static void showHomeMenu() {
        System.out.println("LL      GGGG  TTTTTT   EEEEE  NN   NN ");
        System.out.println("LL     GG       TT     EE     NNN  NN ");
        System.out.println("LL     GG  GGG  TT     EEEE   NN N NN ");
        System.out.println("LL     GG   GG  TT     EE     NN  NNN ");
        System.out.println("LLLLL   GGGGG   TT     EEEEE  NN   NN ");
        System.out.println("\u001B[32mWelcome, " + owner + "! Here's your ledger.\u001B[0m");
        // main menu loop
        while (true) {
            System.out.println("\n=== Home Screen ===");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("C) Check Balance");
            System.out.println("S) Spending by Category");
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
                case "C":
                    printCurrentBalance();
                    break;
                case "S":
                    checkSpendingByCategory();
                    break;
                case "L":
                    showLedgerMenu();
                    break;
                case "X":
                    printSummary();
                    System.out.println("Goodbye, " + owner + "!");
                    return;
                default:
                    System.out.println("Invalid choice; please try again.");
            }
        }
    }

    // deposit logic
    private static void addDeposit() {
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Vendor: ");
        String ven = scanner.nextLine().trim();
        System.out.print("Category (e.g. Food, Rent): ");
        String cat = scanner.nextLine().trim();

        double amt;
        while (true) {
            System.out.print("Amount: ");
            String in = scanner.nextLine().trim();
            try {
                amt = Double.parseDouble(in);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount; enter a number.");
            }
        }

        String[] dt = currentDateTime();
        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println("Deposit recorded.");
    }

    // payment logic negative payments
    private static void makePayment() {
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Vendor: ");
        String ven = scanner.nextLine().trim();
        System.out.print("Category (e.g. Utilities, Subscription): ");
        String cat = scanner.nextLine().trim();

        double amt;
        while (true) {
            System.out.print("Amount: ");
            String in = scanner.nextLine().trim();
            try {
                amt = -Math.abs(Double.parseDouble(in));
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount; enter a number.");
            }
        }

        String[] dt = currentDateTime();
        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println("Payment recorded.");
    }

    // the sub menu
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
                case "A": displayTransactions(transactions); break;
                case "D": displayTransactions(filterDeposits()); break;
                case "P": displayTransactions(filterPayments()); break;
                case "S": searchTransactions(); break;
                case "R": deleteTransaction(); break;
                case "H": return;
                default:  System.out.println("Invalid option; please try again.");
            }
        }
    }

    // list only the deposits
    private static List<Transaction> filterDeposits() {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) out.add(t);
        }
        return out;
    }

    // list only the payments
    private static List<Transaction> filterPayments() {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) out.add(t);
        }
        return out;
    }

    // if the list is empty prints each transaction
    private static void displayTransactions(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No transactions to display.");
        } else {
            for (Transaction t : list) {
                System.out.println(t);
            }
        }
    }

    // recalculates the running balance
    private static void calculateBalances() {
        double bal = 0;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            bal += transactions.get(i).getAmount();
            transactions.get(i).setBalance(bal);
        }
    }

    // prints the balance
    private static void printSummary() {
        double totalDeposits = 0, totalPayments = 0;
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) totalDeposits += t.getAmount();
            else totalPayments += t.getAmount();
        }
        double endingBalance = transactions.isEmpty() ? 0 : transactions.get(0).getBalance();
        System.out.printf(
                "\n=== Summary ===%nDeposits:  $%.2f%nPayments:  $%.2f%nEnding Balance: $%.2f%n",
                totalDeposits, -totalPayments, endingBalance
        );
    }

    // prints the current balance
    private static void printCurrentBalance() {
        calculateBalances();
        double bal = transactions.isEmpty() ? 0.0 : transactions.get(0).getBalance();
        System.out.printf("Current balance: $%.2f%n", bal);
    }


     // Prompts for a category and shows total spending (payments) in that category.
    private static void checkSpendingByCategory() {
        System.out.print("Enter category to check spending: ");
        String cat = scanner.nextLine().trim().toLowerCase();
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getCategory().toLowerCase().equals(cat) && t.getAmount() < 0) {
                total += t.getAmount();
            }
        }
        System.out.printf("Total spending for category '%s': $%.2f%n", cat, -total);
    }

    // get current date and time
    private static String[] currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return new String[]{date, time};
    }

    // prompts for filters
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
        if (!startDate.isEmpty()) filtered.removeIf(t -> t.getDate().compareTo(startDate) < 0);
        if (!endDate.isEmpty())   filtered.removeIf(t -> t.getDate().compareTo(endDate) > 0);
        if (!description.isEmpty()) filtered.removeIf(t -> !t.getDescription().toLowerCase().contains(description));
        if (!vendor.isEmpty())      filtered.removeIf(t -> !t.getVendor().toLowerCase().contains(vendor));
        if (!amountStr.isEmpty()) {
            try {
                double amt = Double.parseDouble(amountStr);
                filtered.removeIf(t -> t.getAmount() != amt);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount; skipping that filter.");
            }
        }
        displayTransactions(filtered);
    }

    // handles deletes
    private static void deleteTransaction() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions to delete.");
            return;
        }

        while (true) {
            System.out.println("\nRemove by:");
            System.out.println("  1) ID");
            System.out.println("  2) Date (YYYY-MM-DD)");
            System.out.println("  3) Vendor");
            System.out.print("Choose mode: ");
            String mode = scanner.nextLine().trim();

            switch (mode) {
                case "1":
                    for (int i = 0; i < transactions.size(); i++) {
                        System.out.printf("%2d) %s%n", i+1, transactions.get(i));
                    }
                    while (true) {
                        System.out.print("Enter ID to delete: ");
                        String in = scanner.nextLine().trim();
                        try {
                            int id = Integer.parseInt(in)-1;
                            if (id < 0 || id >= transactions.size()) {
                                System.out.println("ID out of range; try again.");
                            } else {
                                Transaction removed = transactions.remove(id);
                                System.out.println("Deleted: " + removed);
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number; try again.");
                        }
                    }
                    break;

                case "2":
                    while (true) {
                        System.out.print("Enter date (YYYY-MM-DD): ");
                        String dt = scanner.nextLine().trim();
                        List<Transaction> byDate = transactions.stream()
                                .filter(tx -> tx.getDate().equals(dt))
                                .collect(Collectors.toList());
                        if (byDate.isEmpty()) {
                            System.out.println("No entries on that date; try again.");
                        }
                        else {
                            transactions.removeAll(byDate);
                            System.out.println("Removed " + byDate.size() + " entries on " + dt);
                            break;
                        }
                    }
                    break;

                case "3":
                    while (true) {
                        System.out.print("Enter vendor substring: ");
                        String vend = scanner.nextLine().trim().toLowerCase();
                        List<Transaction> byVend = transactions.stream()
                                .filter(tx -> tx.getVendor().toLowerCase().contains(vend))
                                .collect(Collectors.toList());
                        if (byVend.isEmpty()) {
                            System.out.println("No entries for that vendor; try again.");
                        } else {
                            transactions.removeAll(byVend);
                            System.out.println("Removed " + byVend.size() + " entries for vendor '" + vend + "'");
                            break;
                        }
                    }
                    break;

                default:
                    System.out.println("Invalid mode; please choose 1, 2, or 3.");
                    continue;
            }

            calculateBalances();
            saveAllTransactions();
            System.out.println("Deletion complete.");
            break;
        }
    }
}
