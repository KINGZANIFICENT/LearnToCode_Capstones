package com.pluralsight.UserInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Simple console I/O helper for prompting and reading user input.
 */
public class Console {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Prints a message to the console.
     */
    public void println(String message) {
        System.out.println(message);
    }

    /**
     * Prompts the user for an integer, re-prompting until valid.
     */
    public int promptForInt(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for a string (returns the raw line).
     */
    public String promptForString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the user for a yes/no (y/n) response, returns true for yes.
     */
    public boolean promptForBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * Displays a list of options and prompts the user to select multiple choices by number.
     * @param options the list of option objects to choose from
     * @param <T> the type of options
     */
    public <T> List<T> selectMultiple(List<T> options, String prompt) {
        println(prompt);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, options.get(i).toString());
        }
        while (true) {
            System.out.print("Enter numbers (comma-separated), or blank for none: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return new ArrayList<>();
            }
            String[] parts = line.split(",");
            List<T> chosen = new ArrayList<>();
            boolean allValid = true;
            for (String part : parts) {
                try {
                    int idx = Integer.parseInt(part.trim());
                    if (idx < 1 || idx > options.size()) {
                        allValid = false;
                        break;
                    }
                    chosen.add(options.get(idx - 1));
                } catch (NumberFormatException e) {
                    allValid = false;
                    break;
                }
            }
            if (allValid) {
                return chosen;
            } else {
                System.out.println("Invalid selection. Please enter valid numbers.");
            }
        }
    }
}
