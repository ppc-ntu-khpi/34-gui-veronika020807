package com.mybank.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

// ↓↓↓ Ці класи краще винести окремо, але зараз — усе в одному файлі

class Account {
    protected double balance;

    public Account(double initBalance) {
        balance = initBalance;
    }

    public double getBalance() {
        return balance;
    }
}

class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(double balance, double interestRate) {
        super(balance);
        this.interestRate = interestRate;
    }
}

class CheckingAccount extends Account {
    private double overdraftProtection;

    public CheckingAccount(double balance, double overdraftProtection) {
        super(balance);
        this.overdraftProtection = overdraftProtection;
    }
}

class Customer {
    private String firstName;
    private String lastName;
    private ArrayList<Account> accounts;

    public Customer(String f, String l) {
        firstName = f;
        lastName = l;
        accounts = new ArrayList<>();
    }

    public void addAccount(Account acc) {
        accounts.add(acc);
    }

    public Account getAccount(int i) {
        return accounts.get(i);
    }

    public int getNumberOfAccounts() {
        return accounts.size();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}

class Bank {
    private static ArrayList<Customer> customers = new ArrayList<>();

    public static void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public static Customer getCustomer(int index) {
        return customers.get(index);
    }

    public static int getNumberOfCustomers() {
        return customers.size();
    }
}

// === GUI та логіка ===
public class SWINGDemo {

    private final JEditorPane log;
    private final JButton show;
    private final JButton report;
    private final JComboBox<String> clients;

    public SWINGDemo() {
        loadCustomersFromFile("C:\\Users\\aleks\\OneDrive\\Desktop\\GUIdemo\\src\\data\\test.dat");

        log = new JEditorPane("text/html", "");
        log.setPreferredSize(new Dimension(300, 300));
        show = new JButton("Show");
        report = new JButton("Report");
        clients = new JComboBox<>();

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer c = Bank.getCustomer(i);
            clients.addItem(c.getLastName() + ", " + c.getFirstName());
        }
    }

    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());
        JPanel cpane = new JPanel(new GridLayout(1, 3));
        cpane.add(clients);
        cpane.add(show);
        cpane.add(report);
        frame.add(cpane, BorderLayout.NORTH);
        frame.add(log, BorderLayout.CENTER);

        // кнопка Show - показати вибраного клієнта
        show.addActionListener(e -> {
            Customer current = Bank.getCustomer(clients.getSelectedIndex());
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            sb.append("<b><span style=\"font-size:1.5em;\">")
              .append(current.getLastName()).append(", ").append(current.getFirstName())
              .append("</span></b><hr>");

            for (int i = 0; i < current.getNumberOfAccounts(); i++) {
                Account acc = current.getAccount(i);
                sb.append("<p>&nbsp;<b>Acc Type:</b> ");
                if (acc instanceof CheckingAccount) {
                    sb.append("Checking");
                } else if (acc instanceof SavingsAccount) {
                    sb.append("Savings");
                }
                sb.append("<br>&nbsp;<b>Balance:</b> $")
                  .append(String.format("%.2f", acc.getBalance()))
                  .append("</p><hr>");
            }

            sb.append("</body></html>");
            log.setText(sb.toString());
        });

        // кнопка Report - згенерувати звіт по всіх клієнтах
        report.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            sb.append("<h2>Customer Report</h2>");
            sb.append("<hr>");

            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                Customer customer = Bank.getCustomer(i);
                sb.append("<p><b>Customer:</b> ")
                  .append(customer.getFirstName())
                  .append(" ")
                  .append(customer.getLastName())
                  .append("</p>");

                for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                    Account acc = customer.getAccount(j);
                    sb.append("<p style=\"margin-left:20px;\">");
                    if (acc instanceof CheckingAccount) {
                        sb.append("Checking Account: ");
                    } else if (acc instanceof SavingsAccount) {
                        sb.append("Savings Account: ");
                    } else {
                        sb.append("Unknown Account: ");
                    }
                    sb.append("$").append(String.format("%.2f", acc.getBalance()));
                    sb.append("</p>");
                }

                sb.append("<hr>");
            }

            sb.append("</body></html>");
            log.setText(sb.toString());
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void loadCustomersFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int customerCount = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < customerCount; i++) {
                String line = br.readLine();
                if (line == null || line.trim().isEmpty()) continue;

                String[] nameLine = line.trim().split("\t");
                if (nameLine.length < 3) {
                    System.err.println("Неправильний формат імені: " + line);
                    continue;
                }

                // У твоєму файлі: Ім'я Прізвище Кількість_рахунків
                String firstName = nameLine[0];
                String lastName = nameLine[1];
                int numAccounts = Integer.parseInt(nameLine[2]);

                Customer customer = new Customer(firstName, lastName);

                for (int j = 0; j < numAccounts; j++) {
                    String accLine = br.readLine();
                    if (accLine == null || accLine.trim().isEmpty()) continue;
                    String[] accData = accLine.trim().split("\t");
                    if (accData.length < 3) {
                        System.err.println("Неправильний формат рахунку: " + accLine);
                        continue;
                    }

                    String type = accData[0];
                    double balance = Double.parseDouble(accData[1]);
                    double extra = Double.parseDouble(accData[2]);

                    if (type.equals("S")) {
                        customer.addAccount(new SavingsAccount(balance, extra));
                    } else if (type.equals("C")) {
                        customer.addAccount(new CheckingAccount(balance, extra));
                    } else {
                        System.err.println("Невідомий тип рахунку: " + type);
                    }
                }

                Bank.addCustomer(customer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Помилка читання test.dat", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }
}
