package com.pluralsight;

import com.pluralsight.ui.UIUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.pluralsight.ui.UIUtils.*;

public class AccountingLedgerApp {
    private static final String FILE_NAME = "transactions.csv";
    private static List<Transaction> transactions = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static String owner;

    public static void main(String[] args) {
        // 1) Ask for owner/title
        System.out.print(YELLOW + "Enter your name or account title: " + RESET);
        owner = scanner.nextLine().trim();

        // 2) Load past transactions and compute balances with spinner
        UIUtils.spinner("Loading transactions", () -> {
            loadTransactions();
            calculateBalances();
        });

        // 3) LG10 ASCII banner + welcome
        System.out.println(CYAN + BOLD + "LL      GGGG  TTTTTT   EEEEE  NN   NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG       TT     EE     NNN  NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG  GGG  TT     EEEE   NN N NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG   GG  TT     EE     NN  NNN " + RESET);
        System.out.println(CYAN + BOLD + "LLLLL   GGGGG   TT     EEEEE  NN   NN " + RESET);
        System.out.println(GREEN + BOLD + "🙌🏾 Welcome, " + owner + "! Here's your ledger." + RESET);

        // 4) Scanner-based menu
        showHomeMenu();
    }

    private static void showHomeMenu() {
        while (true) {
            System.out.println("\n" + BOLD + CYAN + "=== Home Screen ===" + RESET);
            System.out.println(GREEN  + "D) Add Deposit 💰" + RESET);
            System.out.println(RED    + "P) Make Payment 💸" + RESET);
            System.out.println(YELLOW + "C) Check Balance 📈" + RESET);
            System.out.println(CYAN   + "S) Spending by Category 💲" + RESET);
            System.out.println(BLUE   + "L) Ledger 📖" + RESET);
            System.out.println(MAGENTA+ "X) Exit 💥" + RESET);
            System.out.print(BOLD + "Choose an option: " + RESET);
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D": addDeposit(); break;
                case "P": makePayment(); break;
                case "C": printCurrentBalance(); break;
                case "S": checkSpendingByCategory(); break;
                case "L": showLedgerMenu(); break;
                case "X":
                    printSummary();
                    System.out.println(YELLOW + "👋🏾 Goodbye, " + owner + "!" + RESET);
                    return;
                default:
                    System.out.println(RED + "❌ Invalid choice; please try again." + RESET);
            }

            // show sparkline after each action
            UIUtils.printBalanceSparkline(transactions);
        }
    }

    private static void showLedgerMenu() {
        while (true) {
            System.out.println("\n" + BOLD + CYAN + "=== Ledger ===" + RESET);
            System.out.println(GREEN  + "A) All Transactions 💰" + RESET);
            System.out.println(GREEN  + "D) Deposits Only 📈" + RESET);
            System.out.println(RED    + "P) Payments Only 📉" + RESET);
            System.out.println(YELLOW + "S) Search 🔍" + RESET);
            System.out.println(RED    + "R) Remove Transaction(s) ❌" + RESET);
            System.out.println(MAGENTA+ "H) Home 🏠" + RESET);
            System.out.print(BOLD + "Choose an option: " + RESET);
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    UIUtils.printTable(transactions);
                    break;
                case "D":
                    UIUtils.printTable(filterDeposits());
                    break;
                case "P":
                    UIUtils.printTable(filterPayments());
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
                    System.out.println(RED + "❌ Invalid option; please try again." + RESET);
            }
        }
    }

    private static void loadTransactions() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    transactions.add(0, Transaction.fromCsv(line));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.out.println(RED + "❌ Error loading transactions: " + e.getMessage() + RESET);
        }
    }

    private static void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(t.toCsv());
            bw.newLine();
        } catch (IOException e) {
            System.out.println(RED + "❌ Error saving transaction: " + e.getMessage() + RESET);
        }
    }

    private static void saveAllTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) pw.println(t.toCsv());
        } catch (IOException e) {
            System.out.println(RED + "❌ Error saving transactions: " + e.getMessage() + RESET);
        }
    }

    private static void addDeposit() {
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Savings? (yes/no): ");
        String ven = scanner.nextLine().trim();
        System.out.print("Category (e.g. Food, Rent): ");
        String cat = scanner.nextLine().trim();

        double amt;
        while (true) {
            System.out.print("Amount: ");
            try {
                amt = Double.parseDouble(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid amount; enter a number." + RESET);
            }
        }
        String[] dt = currentDateTime();
        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println(GREEN + "✅ 💵 Deposit recorded!" + RESET);
    }

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
            try {
                amt = -Math.abs(Double.parseDouble(scanner.nextLine().trim()));
                break;
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid amount; enter a number." + RESET);
            }
        }
        String[] dt = currentDateTime();
        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println(RED + "❌ 💸 Payment recorded!" + RESET);
    }

    private static List<Transaction> filterDeposits() {
        return transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .collect(Collectors.toList());
    }

    private static List<Transaction> filterPayments() {
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.toList());
    }

    private static void displayTransactions(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println(YELLOW + "No transactions to display." + RESET);
        } else {
            for (Transaction t : list) System.out.println(t);
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
        double deposits = transactions.stream().filter(t -> t.getAmount() > 0).mapToDouble(Transaction::getAmount).sum();
        double payments = transactions.stream().filter(t -> t.getAmount() < 0).mapToDouble(Transaction::getAmount).sum();
        double ending = transactions.isEmpty() ? 0 : transactions.get(0).getBalance();
        System.out.printf(
                "\n" + BOLD + CYAN + "=== Summary ===" + RESET +
                        "%nDeposits:  $%.2f%nPayments:  $%.2f%nEnding Balance: $%.2f%n",
                deposits, -payments, ending
        );
    }

    private static void printCurrentBalance() {
        calculateBalances();
        double bal = transactions.isEmpty() ? 0 : transactions.get(0).getBalance();
        System.out.printf(GREEN + "💼 Current balance: $%.2f%n" + RESET, bal);
    }

    private static void checkSpendingByCategory() {
        System.out.print("Enter category to check spending: ");
        String cat = scanner.nextLine().trim().toLowerCase();
        double total = transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(cat) && t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.printf(RED + "Spent in '%s': $%.2f%n" + RESET, cat, -total);
    }

    private static String[] currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return new String[]{
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        };
    }

    private static void searchTransactions() {
        System.out.print("Start Date (yyyy-MM-dd) or leave blank: ");
        String start = scanner.nextLine().trim();
        System.out.print("End Date (yyyy-MM-dd) or leave blank: ");
        String end = scanner.nextLine().trim();
        System.out.print("Description contains or leave blank: ");
        String desc = scanner.nextLine().trim().toLowerCase();
        System.out.print("Vendor contains or leave blank: ");
        String ven = scanner.nextLine().trim().toLowerCase();

        List<Transaction> filtered = new ArrayList<>(transactions);
        if (!start.isEmpty()) filtered.removeIf(t -> t.getDate().compareTo(start) < 0);
        if (!end.isEmpty())   filtered.removeIf(t -> t.getDate().compareTo(end) > 0);
        if (!desc.isEmpty())  filtered.removeIf(t -> !t.getDescription().toLowerCase().contains(desc));
        if (!ven.isEmpty())   filtered.removeIf(t -> !t.getVendor().toLowerCase().contains(ven));

        UIUtils.printTable(filtered);
    }

    private static void deleteTransaction() {
        if (transactions.isEmpty()) {
            System.out.println(YELLOW + "No transactions to delete." + RESET);
            return;
        }

        System.out.println("\n" + RED + BOLD + "⚠️  Remove Transaction(s)" + RESET);
        System.out.println("1) By ID   2) By Date   3) By Vendor");
        System.out.print("Choose mode: ");
        String mode = scanner.nextLine().trim();

        switch (mode) {
            case "1":
                for (int i = 0; i < transactions.size(); i++) {
                    System.out.printf("%2d) %s%n", i + 1, transactions.get(i));
                }
                System.out.print("Enter ID to delete: ");
                try {
                    int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (idx < 0 || idx >= transactions.size()) {
                        System.out.println(RED + "❌ ID out of range." + RESET);
                    } else {
                        Transaction removed = transactions.remove(idx);
                        System.out.println(GREEN + "✅ Removed: " + removed + RESET);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(RED + "❌ Invalid number." + RESET);
                }
                break;

            case "2":
                System.out.print("Enter date (YYYY-MM-DD): ");
                String dt = scanner.nextLine().trim();
                List<Transaction> byDate = transactions.stream()
                        .filter(t -> t.getDate().equals(dt))
                        .collect(Collectors.toList());
                if (byDate.isEmpty()) {
                    System.out.println(YELLOW + "No entries on that date." + RESET);
                } else {
                    transactions.removeAll(byDate);
                    System.out.println(GREEN + "✅ Removed " + byDate.size() + " entries on " + dt + RESET);
                }
                break;

            case "3":
                System.out.print("Enter vendor (substring): ");
                String vend = scanner.nextLine().trim().toLowerCase();
                List<Transaction> byVend = transactions.stream()
                        .filter(t -> t.getVendor().toLowerCase().contains(vend))
                        .collect(Collectors.toList());
                if (byVend.isEmpty()) {
                    System.out.println(YELLOW + "No entries for that vendor." + RESET);
                } else {
                    transactions.removeAll(byVend);
                    System.out.println(GREEN + "✅ Removed " + byVend.size() + " entries for vendor '" + vend + "'" + RESET);
                }
                break;

            default:
                System.out.println(RED + "❌ Invalid mode; aborting deletion." + RESET);
                return;
        }

        calculateBalances();
        saveAllTransactions();
    }
}
