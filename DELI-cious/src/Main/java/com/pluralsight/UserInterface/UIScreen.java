package com.pluralsight.UserInterface;

import com.pluralsight.FileManager.OrderManager;
import com.pluralsight.Menu.*;
import java.util.List;

/**
 * Console-based UI to drive the Fix-Me-A-Sandwich application.
 */
public class UIScreen {
    private final Console console = new Console();

    /**
     * Entry point for the user interface.
     */
    public void run() {
        console.println("Welcome to Fix-Me-A-Sandwich!");
        Order order = new Order();

        // --- Build Sandwich ---
        int sizeChoice = console.promptForInt(
                "Choose sandwich size:\n" +
                        "[1] 4\"\n" +
                        "[2] 8\"\n" +
                        "[3] 12\""
        );
        int inches = switch (sizeChoice) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 12;
            default -> {
                console.println("Invalid choice, defaulting to 8\"");
                yield 8;
            }
        };

        String bread    = console.promptForString("Bread type?");
        boolean toasted = console.promptForBoolean("Toasted? (y/n)");

        Sandwich.Builder sb = new Sandwich.Builder()
                .size(inches)
                .bread(bread)
                .toasted(toasted);

        // Add sauces
        List<Topping> sauces = console.selectMultiple(
                Topping.availableSauces(), "Select sauces (comma-separated):"
        );
        sauces.forEach(sb::addSauce);

        // Add toppings
        List<Topping> toppings = console.selectMultiple(
                Topping.availableToppings(), "Select toppings (comma-separated):"
        );
        toppings.forEach(sb::addTopping);

        order.addItem(sb.build());

        // --- Add Sides ---
        while (true) {
            String opt = console.promptForString(
                    "Add a side?\n" +
                            "[1] Drink\n" +
                            "[2] Chips\n" +
                            "[C] Checkout"
            );
            if (opt.equals("1")) {
                String flavor = console.promptForString("Which drink?");
                order.addItem(new Side(SideType.DRINK, flavor));
            } else if (opt.equals("2")) {
                String flavor = console.promptForString("Which chips?");
                order.addItem(new Side(SideType.CHIPS, flavor));
            } else if (opt.equalsIgnoreCase("C")) {
                break;
            } else {
                console.println("Please choose 1, 2, or C.");
            }
        }

        // --- Checkout ---
        console.println("\n--- Your Order ---");
        order.showCompleteOrder();

        // Persist order
        OrderManager.save(order);
        console.println("Thank you! Your order has been saved.");
    }
}
