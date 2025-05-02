package com.pluralsight;

import com.pluralsight.ui.UIUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.pluralsight.ui.UIUtils.*;

public class AccountingLedgerApp {
    // CSV file name for storing transactions
    private static final String FILE_NAME = "transactions.csv";
    // In-memory list of all transactions
    private static List<Transaction> transactions = new ArrayList<>();
    // Scanner for reading user input
    private static Scanner scanner = new Scanner(System.in);
    // Owner or account title for customization
    private static String owner;

    public static void main(String[] args) {
        // 1) Ask for owner/title
        System.out.print(YELLOW + "Enter your name or account title: " + RESET);
        owner = scanner.nextLine().trim();

        // 2) Load past transactions and compute balances with a spinner animation
        UIUtils.spinner("Loading transactions", () -> {
            loadTransactions();   // Read from CSV into memory
            calculateBalances();  // Recalculate running balances for each transaction
        });

        // 3) Display ASCII art banner and welcome message
        System.out.println(CYAN + BOLD + "LL      GGGG  TTTTTT   EEEEE  NN   NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG       TT     EE     NNN  NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG  GGG  TT     EEEE   NN N NN " + RESET);
        System.out.println(CYAN + BOLD + "LL     GG   GG  TT     EE     NN  NNN " + RESET);
        System.out.println(CYAN + BOLD + "LLLLL   GGGGG   TT     EEEEE  NN   NN " + RESET);
        System.out.println(GREEN + BOLD + "🙌🏾 Welcome, " + owner + "! Here's your ledger." + RESET);

        // 4) Enter the main menu loop
        showHomeMenu();
    }

    /**
     * home screen
     */
    private static void showHomeMenu() {
        while (true) {
            // Print menu options
            System.out.println("\n" + BOLD + CYAN + "=== Home Screen ===" + RESET);
            System.out.println(GREEN  + "D) Add Deposit 💰" + RESET);
            System.out.println(RED    + "P) Make Payment 💸" + RESET);
            System.out.println(YELLOW + "C) Check Balance 📈" + RESET);
            System.out.println(CYAN   + "S) Spending by Category 💲" + RESET);
            System.out.println(BLUE   + "L) Ledger 📖" + RESET);
            System.out.println(MAGENTA+ "X) Exit 💥" + RESET);
            System.out.print(BOLD + "Choose an option: " + RESET);

            // Read and normalize user input
            String choice = scanner.nextLine().trim().toUpperCase();

            // Handle menu selection using switch-case
            switch (choice) {
                case "D": addDeposit(); break;                     // Add a deposit transaction
                case "P": makePayment(); break;                   // Record an outgoing payment
                case "C": printCurrentBalance(); break;           // Show current balance
                case "S": checkSpendingByCategory(); break;       // Show spending by a specific category
                case "L": showLedgerMenu(); break;                // Enter ledger submenu
                case "X":                                        // Exit the application
                    printSummary();                                // Print deposit/payment summary
                    System.out.println(YELLOW + "👋🏾 Goodbye, " + owner + "!" + RESET);
                    return;                                        // Break out of the loop and end program
                default:
                    // Handle invalid selections
                    System.out.println(RED + "❌ Invalid choice; please try again." + RESET);
            }

            // After each action, display a sparkline chart of balances
            UIUtils.printBalanceSparkline(transactions);
        }
    }

    /**
     * submenu
     */
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
                    UIUtils.printTable(transactions);      // Show all transactions
                    break;
                case "D":
                    UIUtils.printTable(filterDeposits()); // Show only positive amounts
                    break;
                case "P":
                    UIUtils.printTable(filterPayments()); // Show only negative amounts
                    break;
                case "S":
                    searchTransactions();                  // Interactive search by date/desc/vendor
                    break;
                case "R":
                    deleteTransaction();                  // Remove transactions based on criteria
                    break;
                case "H":
                    return;                               // Return to home menu
                default:
                    System.out.println(RED + "❌ Invalid option; please try again." + RESET);
            }
        }
    }

    /**
     * Reads transactions from the CSV file into the in-memory list
     */
    private static void loadTransactions() {
        File file = new File(FILE_NAME);
        // If file doesn't exist, skip loading
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    // Parse CSV line into Transaction and insert at beginning
                    transactions.add(0, Transaction.fromCsv(line));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "❌ Error loading transactions: " + e.getMessage() + RESET);
        }
    }

    /**
     * Appends a single transaction to the CSV file
     */
    private static void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(t.toCsv());   // Convert to CSV format and write
            bw.newLine();
        } catch (IOException e) {
            System.out.println(RED + "❌ Error saving transaction: " + e.getMessage() + RESET);
        }
    }

    /**
     * Overwrites the entire CSV file with current in-memory transactions
     */
    private static void saveAllTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) pw.println(t.toCsv());
        } catch (IOException e) {
            System.out.println(RED + "❌ Error saving transactions: " + e.getMessage() + RESET);
        }
    }

    /**
     * Prompts the user to add a deposit transaction
     */
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
                // Parse positive amount for deposit
                amt = Double.parseDouble(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid amount; enter a number." + RESET);
            }
        }
        // Get current date/time and build Transaction
        String[] dt = currentDateTime();
        Transaction t = new Transaction(dt[0], dt[1], desc, ven, cat, amt);
        // Add at beginning of list, recalculate balances, and persist
        transactions.add(0, t);
        calculateBalances();
        saveTransaction(t);
        System.out.println(GREEN + "✅ 💵 Deposit recorded!" + RESET);
    }

    /**
     * Prompts to record a payment (negative amount)
     */
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
                // Convert input to a negative value for payments
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

    /**
     * Returns only deposits (amount > 0)
     */
    private static List<Transaction> filterDeposits() {
        return transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Returns only payments (amount < 0)
     */
    private static List<Transaction> filterPayments() {
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.toList());
    }

    /**
     * Prints a list of transactions or shows a message if empty
     */
    private static void displayTransactions(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println(YELLOW + "No transactions to display." + RESET);
        } else {
            for (Transaction t : list) System.out.println(t);
        }
    }

    /**
     * Calculates running balance for each transaction in chronological order
     */
    private static void calculateBalances() {
        double bal = 0;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            bal += transactions.get(i).getAmount();
            transactions.get(i).setBalance(bal);
        }
    }

    /**
     * Prints a summary of total deposit payments and ending balance
     */
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

    /**
     * Prints the current
     */
    private static void printCurrentBalance() {
        calculateBalances(); // Ensure balances are up-to-date
        double bal = transactions.isEmpty() ? 0 : transactions.get(0).getBalance();
        System.out.printf(GREEN + "💼 Current balance: $%.2f%n" + RESET, bal);
    }

    /**
     * Prompts for a category
     */
    private static void checkSpendingByCategory() {
        System.out.print("Enter category to check spending: ");
        String cat = scanner.nextLine().trim().toLowerCase();
        // Sum all negative amounts matching the given category
        double total = transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(cat) && t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.printf(RED + "Spent in '%s': $%.2f%n" + RESET, cat, -total);
    }

    /**
     * Returns current date and time as formatted strings
     */
    private static String[] currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return new String[]{
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), // e.g. 2025-05-02
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))    // e.g. 14:05:03
        };
    }

    /**
     *  search for transactions
     */
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
        // Remove transactions before the start date if provided
        if (!start.isEmpty()) filtered.removeIf(t -> t.getDate().compareTo(start) < 0);
        // Remove transactions after the end date if provided
        if (!end.isEmpty())   filtered.removeIf(t -> t.getDate().compareTo(end) > 0);
        // Filter by description keyword if provided
        if (!desc.isEmpty())  filtered.removeIf(t -> !t.getDescription().toLowerCase().contains(desc));
        // Filter by vendor substring if provided
        if (!ven.isEmpty())   filtered.removeIf(t -> !t.getVendor().toLowerCase().contains(ven));

        UIUtils.printTable(filtered); // Display filtered results in table format
    }

    /**
     * Removes transactions
     */
    private static void deleteTransaction() {
        // If no transactions exist, nothing to delete
        if (transactions.isEmpty()) {
            System.out.println(YELLOW + "No transactions to delete." + RESET);
            return;
        }

        // Prompt user for deletion mode
        System.out.println("\n" + RED + BOLD + "⚠️  Remove Transaction(s)" + RESET);
        System.out.println("1) By ID   2) By Date   3) By Vendor");
        System.out.print("Choose mode: ");
        String mode = scanner.nextLine().trim();

        switch (mode) {
            case "1": // Delete by index ID
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

            case "2": // Delete all transactions on a specified date
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

            case "3": // Delete by vendor substring
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

        // After deletion, recalculate balances and overwrite CSV
        calculateBalances();
        saveAllTransactions();
    }
}
