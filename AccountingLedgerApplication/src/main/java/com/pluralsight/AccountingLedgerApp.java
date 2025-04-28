package com.pluralsight;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AccountingLedgerApp {
    private static final String FILE_NAME = "transactions.csv";
    private static List<Transaction> transactions = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadTransactions();
        showHomeMenu();
    }

    //error outputs

    private static void loadTransactions() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Transaction t = Transaction.fromCsv(line);
                transactions.add(0, t);
            }
        } catch (IOException e) {
            System.out.println("Error Did Not Load: " + e.getMessage());
        }
    }

    private static void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter((new FileWriter(FILE_NAME, true)))) {
            bw.write(t.toCsv());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error Did Not Save: " + e.getMessage());
        }
    }

    //the home menu
    private static void showHomeMenu() {
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
                    System.out.println("Goodbye");
                    return;
                default:
                    System.out.println("Invalid choice Choose One From The Above List.");
            }
        }
    }
}
// handles the deposit
private static void addDeposit() {
    System.out.print("Description: ");
    String desc = scanner.nextLine();
    System.out.print("Vendor: ");
    String ven = scanner.nextLine();
    System.out.print("Amount: ");
    double amt = Double.parseDouble(scanner.nextLine());
    String[] dt = currentDateTime();
    Transaction t = new Transaction(dt[0], dt[1], desc, ven, amt);
    transactions.add(0, t);
    saveTransaction(t);
    System.out.println("Deposit recorded.");
}

private static void makePayment() {
    System.out.print("Description: ");
    String desc = scanner.nextLine();
    System.out.print("Vendor: ");
    String ven = scanner.nextLine();
    System.out.print("Amount: ");
    double amt = -Math.abs(Double.parseDouble(scanner.nextLine()));
    String[] dt = currentDateTime();
    Transaction t = new Transaction(dt[0], dt[1], desc, ven, amt);
    transactions.add(0, t);
    saveTransaction(t);
    System.out.println("Payment recorded.");
}

private static void showLedgerMenu() {
    while (true) {
        System.out.println("\n=== Ledger ===");
        System.out.println("A) All");
        System.out.println("D) Deposits");
        System.out.println("P) Payments");
        System.out.println("H) Home");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        switch (choice) {
            case "A": displayTransactions(transactions);          break;
            case "D": displayTransactions(filterDeposits());      break;
            case "P": displayTransactions(filterPayments());      break;
            case "H": return;
            default:  System.out.println("Invalid choice, try again.");
        }
    }
}