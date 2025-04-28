package com.pluralsight;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountingLedgerApp {
    private static final String FILE_NAME = "transactions.csv";
    private static List<Transaction> transactions = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadTransactions();
        showHomeMenu();
    }

    private static void LoadTransactions(){
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                Transaction t = Transaction.fromCsv(line);
                transactions.add(0, t);
            }
        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());
        }
    }
















}


