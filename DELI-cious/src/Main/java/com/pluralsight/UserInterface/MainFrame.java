package com.pluralsight.UserInterface;

import com.pluralsight.FileManager.OrderManager;
import com.pluralsight.Menu.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Swing UI for The Sandwich Shop, updated for premium toppings & per-receipt files.
 */
public class MainFrame extends JFrame {
    private final JSpinner sizeSpinner =
            new JSpinner(new SpinnerNumberModel(8, 4, 12, 4));
    private final JComboBox<String> breadCombo =
            new JComboBox<>(new String[]{"White", "Wheat", "Rye", "Wrap"});
    private final JCheckBox toastedCheck = new JCheckBox("Toasted");
    private final JPanel toppingsPanel = new JPanel(new GridLayout(0,2));
    private final JPanel saucesPanel   = new JPanel(new GridLayout(0,2));
    private final DefaultListModel<Side> sidesModel = new DefaultListModel<>();
    private final JList<Side> sidesList = new JList<>(sidesModel);
    private final JButton addDrinkBtn = new JButton("Add Drink…");
    private final JButton addChipsBtn = new JButton("Add Chips…");
    private final JTextArea summaryArea = new JTextArea(10, 30);

    public MainFrame() {
        super("The Sandwich Shop");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Sandwich panel
        JPanel sandwichP = new JPanel(new GridBagLayout());
        sandwichP.setBorder(new TitledBorder("Sandwich"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.gridx = 0; gbc.gridy = 0;
        sandwichP.add(new JLabel("Size (inches):"), gbc);
        gbc.gridx = 1;
        sandwichP.add(sizeSpinner, gbc);
        gbc.gridy++; gbc.gridx = 0;
        sandwichP.add(new JLabel("Bread:"), gbc);
        gbc.gridx = 1;
        sandwichP.add(breadCombo, gbc);
        gbc.gridy++; gbc.gridx = 0;
        sandwichP.add(toastedCheck, gbc);

        // Toppings & Sauces
        toppingsPanel.setBorder(new TitledBorder("Toppings"));
        for (Topping t : Topping.availableToppings()) {
            JCheckBox cb = new JCheckBox(t.getName());
            cb.putClientProperty("topping", t);
            toppingsPanel.add(cb);
        }
        saucesPanel.setBorder(new TitledBorder("Sauces"));
        for (Topping s : Topping.availableSauces()) {
            JCheckBox cb = new JCheckBox(s.getName());
            cb.putClientProperty("topping", s);
            saucesPanel.add(cb);
        }

        // Sides panel
        JPanel sidesP = new JPanel(new BorderLayout());
        sidesP.setBorder(new TitledBorder("Sides"));
        sidesP.add(new JScrollPane(sidesList), BorderLayout.CENTER);
        JPanel btnBar = new JPanel();
        btnBar.add(addDrinkBtn);
        btnBar.add(addChipsBtn);
        sidesP.add(btnBar, BorderLayout.SOUTH);

        // Summary & Place Order
        JButton placeOrder = new JButton("Place Order");
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryArea.setEditable(false);

        // Layout main
        JPanel main = new JPanel(new BorderLayout(8,8));
        JPanel top = new JPanel(new GridLayout(1,3,8,8));
        top.add(sandwichP);
        top.add(toppingsPanel);
        top.add(saucesPanel);
        main.add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout());
        mid.add(sidesP, BorderLayout.WEST);
        mid.add(summaryScroll, BorderLayout.CENTER);
        main.add(mid, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(placeOrder);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
        pack();
        setLocationRelativeTo(null);

        // Hook up actions
        addDrinkBtn.addActionListener(e -> addSide(SideType.DRINK));
        addChipsBtn.addActionListener(e -> addSide(SideType.CHIPS));
        placeOrder.addActionListener(this::onPlaceOrder);
    }

    private void addSide(SideType type) {
        String flavor = JOptionPane.showInputDialog(
                this, "Enter " + type.label + " flavor:");
        if (flavor != null && !flavor.isBlank()) {
            sidesModel.addElement(new Side(type, flavor));
        }
    }

    private void onPlaceOrder(ActionEvent e) {
        Order order = new Order();

        // Sandwich builder
        Sandwich.Builder sb = new Sandwich.Builder()
                .size((Integer)sizeSpinner.getValue())
                .bread((String)breadCombo.getSelectedItem())
                .toasted(toastedCheck.isSelected());

        // Gather toppings & sauces
        for (Component c : toppingsPanel.getComponents()) {
            if (c instanceof JCheckBox cb && cb.isSelected()) {
                sb.addTopping((Topping)cb.getClientProperty("topping"));
            }
        }
        for (Component c : saucesPanel.getComponents()) {
            if (c instanceof JCheckBox cb && cb.isSelected()) {
                sb.addSauce((Topping)cb.getClientProperty("topping"));
            }
        }

        order.addItem(sb.build());
        for (int i = 0; i < sidesModel.size(); i++) {
            order.addItem(sidesModel.get(i));
        }

        // Show in summaryArea…
        StringBuilder out = new StringBuilder();
        for (OrderItem it : order.getItems()) {
            out.append(it.orderItemDescription())
                    .append(" — $")
                    .append(String.format("%.2f\n", it.orderItemPrice()));
        }
        out.append("\nTOTAL: $")
                .append(String.format("%.2f",
                        order.getItems().stream()
                                .mapToDouble(OrderItem::orderItemPrice)
                                .sum()));
        summaryArea.setText(out.toString());

        // Save into receipts/yyyyMMdd-HHmmss.txt
        OrderManager.save(order);
        JOptionPane.showMessageDialog(
                this,
                "Order saved to receipts folder!",
                "Thank you",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
