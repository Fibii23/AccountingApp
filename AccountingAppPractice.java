package accounting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;


public class AccountingAppPractice extends JFrame {

    // ------------------------
    // Account variables
    // ------------------------
    // Assets
    private final String cash_Asset = "Cash [Asset]";
    private final String accountsReceivable_Asset = "Accounts Receivable [Asset]";
    private final String inventory_Asset = "Inventory [Asset]";
    private final String supplies_Asset = "Supplies [Asset]";
    private final String prepaidExpenses_Asset = "Prepaid Expenses [Asset]";
    private final String equipment_Asset = "Equipment [Asset]";
    private final String furnitureFixtures_Asset = "Furniture and Fixtures [Asset]";
    private final String land_Asset = "Land [Asset]";
    private final String buildings_Asset = "Buildings [Asset]";

    // Liabilities
    private final String accountsPayable_Liability = "Accounts Payable [Liability]";
    private final String notesPayable_Liability = "Notes Payable [Liability]";
    private final String salariesPayable_Liability = "Salaries Payable [Liability]";
    private final String rentPayable_Liability = "Rent Payable [Liability]";
    private final String interestPayable_Liability = "Interest Payable [Liability]";
    private final String unearnedRevenue_Liability = "Unearned Revenue [Liability]";

    // Owner's Equity
    private final String ownersCapital_Equity = "Owner's Capital [Owner's Equity]";
    private final String ownersDrawing_Equity = "Owner's Drawing [Owner's Equity]";

    // Revenue
    private final String serviceRevenue_Revenue = "Service Revenue [Revenue]";
    private final String salesRevenue_Revenue = "Sales Revenue [Revenue]";
    private final String interestIncome_Revenue = "Interest Income [Revenue]";

    // Expenses
    private final String salariesExpense_Expense = "Salaries Expense [Expense]";
    private final String rentExpense_Expense = "Rent Expense [Expense]";
    private final String utilitiesExpense_Expense = "Utilities Expense [Expense]";
    private final String suppliesExpense_Expense = "Supplies Expense [Expense]";
    private final String depreciationExpense_Expense = "Depreciation Expense [Expense]";
    private final String insuranceExpense_Expense = "Insurance Expense [Expense]";
    private final String advertisingExpense_Expense = "Advertising Expense [Expense]";

    // Combined immutable list
    private final List<String> allAccounts;

    // ------------------------
    // Data models and storage
    // ------------------------
    private final Map<String, Double> accountBalances = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private final DefaultTableModel transactionsModel = new DefaultTableModel(
            new String[]{"Date", "Description", "Debit Account", "Credit Account", "Amount"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel accountsModel = new DefaultTableModel(
            new String[]{"Account", "Type", "Balance"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel journalModel = new DefaultTableModel(
            new String[]{"Date", "Description", "Account", "Debit", "Credit"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel ledgerModel = new DefaultTableModel(
            new String[]{"Date", "Description", "Debit Account", "Credit Account", "Amount", "Running Balance"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel assetsModel = new DefaultTableModel(
            new String[]{"Account", "Amount"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel liabilitiesModel = new DefaultTableModel(
            new String[]{"Account", "Amount"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    // UI components
    private JComboBox<String> debitCombo, creditCombo, ledgerAccountCombo;
    private JTextField dateField, descField, amountField, searchField;
    private JTable transactionsTable, accountsTable, journalTable, ledgerTable, assetsTable, liabilitiesTable;
    private JLabel totalAssetsLabel, totalLiabEqLabel;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // ------------------------
    // Constructor
    // ------------------------
    public AccountingAppPractice() {
        List<String> tmp = new ArrayList<>(Arrays.asList(
                cash_Asset, accountsReceivable_Asset, inventory_Asset, supplies_Asset, prepaidExpenses_Asset,
                equipment_Asset, furnitureFixtures_Asset, land_Asset, buildings_Asset,
                accountsPayable_Liability, notesPayable_Liability, salariesPayable_Liability, rentPayable_Liability,
                interestPayable_Liability, unearnedRevenue_Liability,
                ownersCapital_Equity, ownersDrawing_Equity,
                serviceRevenue_Revenue, salesRevenue_Revenue, interestIncome_Revenue,
                salariesExpense_Expense, rentExpense_Expense, utilitiesExpense_Expense, suppliesExpense_Expense,
                depreciationExpense_Expense, insuranceExpense_Expense, advertisingExpense_Expense
        ));
        allAccounts = Collections.unmodifiableList(tmp);

        for (String a : allAccounts) accountBalances.put(a, 0.0);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setTitle("AccountingAppPractice");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("New Transaction", createNewTransactionPanel());
        tabs.addTab("Transactions", createTransactionsPanel());
        tabs.addTab("Accounts", createAccountsPanel());
        tabs.addTab("General Journal", createGeneralJournalPanel());
        tabs.addTab("General Ledger", createGeneralLedgerPanel());
        tabs.addTab("Balance Sheet", createBalanceSheetPanel());

        add(tabs, BorderLayout.CENTER);

        sdf.setLenient(false);
        refreshAllViews();
    }


    private JPanel createNewTransactionPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField(sdf.format(new Date()));
        gbc.gridx = 1;
        topPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("Description:"), gbc);
        descField = new JTextField();
        gbc.gridx = 1;
        topPanel.add(descField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Debit Account:"), gbc);
        debitCombo = new JComboBox<>(allAccounts.toArray(new String[0]));
        gbc.gridx = 1;
        topPanel.add(debitCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(new JLabel("Credit Account:"), gbc);
        creditCombo = new JComboBox<>(allAccounts.toArray(new String[0]));
        gbc.gridx = 1;
        topPanel.add(creditCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        topPanel.add(new JLabel("Amount:"), gbc);
        amountField = new JTextField();
        gbc.gridx = 1;
        topPanel.add(amountField, gbc);

        // Buttons aligned under amount field (second column)
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton clearBtn = new JButton("Clear");
        JButton postBtn = new JButton("Post Transaction");
        postBtn.setBackground(new Color(25, 118, 210));
        postBtn.setForeground(Color.WHITE);
        postBtn.setOpaque(true);
        postBtn.setBorderPainted(false);
        btns.add(clearBtn);
        btns.add(postBtn);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        topPanel.add(btns, gbc);

        clearBtn.addActionListener(e -> {
            dateField.setText(sdf.format(new Date()));
            descField.setText("");
            amountField.setText("");
            debitCombo.setSelectedIndex(0);
            creditCombo.setSelectedIndex(0);
        });

        postBtn.addActionListener(e -> postTransaction());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search (date or description):"));
        searchField = new JTextField(28);
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        top.add(searchBtn);
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        transactionsTable = new JTable(transactionsModel);
        styleTable(transactionsTable);
        p.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> filterTransactions(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            filterTransactions("");
        });

        return p;
    }

    private JPanel createAccountsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        accountsTable = new JTable(accountsModel);
        styleTable(accountsTable);
        p.add(new JScrollPane(accountsTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel createGeneralJournalPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        journalTable = new JTable(journalModel);
        styleTable(journalTable);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh Journal");
        refresh.addActionListener(e -> refreshAllViews());
        top.add(refresh);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(journalTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel createGeneralLedgerPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Select Account:"));
        ledgerAccountCombo = new JComboBox<>(allAccounts.toArray(new String[0]));
        top.add(ledgerAccountCombo);
        JButton refresh = new JButton("Refresh");
        top.add(refresh);
        p.add(top, BorderLayout.NORTH);

        ledgerTable = new JTable(ledgerModel);
        styleTable(ledgerTable);
        p.add(new JScrollPane(ledgerTable), BorderLayout.CENTER);

        ledgerAccountCombo.addActionListener(e -> {
            String sel = (String) ledgerAccountCombo.getSelectedItem();
            if (sel != null) updateLedgerForAccount(sel);
        });
        refresh.addActionListener(e -> {
            String sel = (String) ledgerAccountCombo.getSelectedItem();
            if (sel != null) updateLedgerForAccount(sel);
        });

        return p;
    }

    private JPanel createBalanceSheetPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Balance Sheet");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));

        JPanel assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
        assetsTable = new JTable(assetsModel);
        styleTable(assetsTable);
        assetsPanel.add(new JScrollPane(assetsTable), BorderLayout.CENTER);
        totalAssetsLabel = new JLabel("Total Assets: 0.00", SwingConstants.RIGHT);
        totalAssetsLabel.setBorder(new EmptyBorder(6, 6, 6, 6));
        assetsPanel.add(totalAssetsLabel, BorderLayout.SOUTH);

        JPanel liabPanel = new JPanel(new BorderLayout());
        liabPanel.setBorder(BorderFactory.createTitledBorder("Liabilities and Owner's Equity"));
        liabilitiesTable = new JTable(liabilitiesModel);
        styleTable(liabilitiesTable);
        liabPanel.add(new JScrollPane(liabilitiesTable), BorderLayout.CENTER);
        totalLiabEqLabel = new JLabel("Total Liabilities + Equity: 0.00", SwingConstants.RIGHT);
        totalLiabEqLabel.setBorder(new EmptyBorder(6, 6, 6, 6));
        liabPanel.add(totalLiabEqLabel, BorderLayout.SOUTH);

        center.add(assetsPanel);
        center.add(liabPanel);
        p.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh Balance Sheet");
        bottom.add(refresh);
        p.add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> {
            refreshBalanceSheet();
            totalAssetsLabel.setText("Total Assets: " + fmt(calculateTotalAssets()));
            totalLiabEqLabel.setText("Total Liabilities + Equity: " + fmt(calculateTotalLiabilitiesAndEquity()));
        });

        refreshBalanceSheet();
        totalAssetsLabel.setText("Total Assets: " + fmt(calculateTotalAssets()));
        totalLiabEqLabel.setText("Total Liabilities + Equity: " + fmt(calculateTotalLiabilitiesAndEquity()));

        return p;
    }

    // ------------------------
    // Core logic
    // ------------------------
    private void postTransaction() {
        String dateS = dateField.getText().trim();
        String desc = descField.getText().trim();
        String debit = (String) debitCombo.getSelectedItem();
        String credit = (String) creditCombo.getSelectedItem();
        String amtS = amountField.getText().trim();

        if (debit == null || credit == null) {
            showError("Select debit and credit accounts.");
            return;
        }
        if (debit.equals(credit)) {
            showError("Debit and Credit must differ.");
            return;
        }

        Date date;
        try {
            date = sdf.parse(dateS);
        } catch (ParseException ex) {
            showError("Invalid date. Use YYYY-MM-DD.");
            return;
        }

        double amt;
        try {
            amt = Double.parseDouble(amtS);
            if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Amount must be a positive number.");
            return;
        }

        if (isAssetOrExpense(debit)) accountBalances.put(debit, accountBalances.get(debit) + amt);
        else accountBalances.put(debit, accountBalances.get(debit) - amt);

        if (isLiabilityEquityRevenue(credit)) accountBalances.put(credit, accountBalances.get(credit) + amt);
        else accountBalances.put(credit, accountBalances.get(credit) - amt);

        transactions.add(new Transaction(date, desc, debit, credit, amt));
        transactions.sort(Comparator.comparing(t -> t.date));

        refreshAllViews();
        JOptionPane.showMessageDialog(this, "Transaction posted.");
    }

    private boolean isAssetOrExpense(String acc) {
        return acc.contains("[Asset]") || acc.contains("[Expense]");
    }

    private boolean isLiabilityEquityRevenue(String acc) {
        return acc.contains("[Liability]") || acc.contains("[Owner's Equity]") || acc.contains("[Revenue]");
    }

    private void refreshAllViews() {
        transactionsModel.setRowCount(0);
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            transactionsModel.addRow(new Object[]{sdf.format(t.date), t.description, t.debitAccount, t.creditAccount, fmt(t.amount)});
        }

        journalModel.setRowCount(0);
        for (Transaction t : transactions) {
            journalModel.addRow(new Object[]{sdf.format(t.date), t.description, t.debitAccount, fmt(t.amount), ""});
            journalModel.addRow(new Object[]{sdf.format(t.date), t.description, t.creditAccount, "", fmt(t.amount)});
        }

        accountsModel.setRowCount(0);
        for (Map.Entry<String, Double> e : accountBalances.entrySet()) {
            accountsModel.addRow(new Object[]{e.getKey(), extractType(e.getKey()), fmt(e.getValue())});
        }
        if (accountsTable != null) accountsTable.setModel(accountsModel);

        if (ledgerAccountCombo != null) ledgerAccountCombo.setModel(new DefaultComboBoxModel<>(allAccounts.toArray(new String[0])));

        refreshBalanceSheet();
        if (totalAssetsLabel != null) totalAssetsLabel.setText("Total Assets: " + fmt(calculateTotalAssets()));
        if (totalLiabEqLabel != null) totalLiabEqLabel.setText("Total Liabilities + Equity: " + fmt(calculateTotalLiabilitiesAndEquity()));

        if (ledgerAccountCombo != null && ledgerAccountCombo.getItemCount() > 0) {
            String selected = (String) ledgerAccountCombo.getSelectedItem();
            if (selected != null) updateLedgerForAccount(selected);
        }
    }

    private void filterTransactions(String q) {
        String lower = q.toLowerCase();
        transactionsModel.setRowCount(0);
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            if (q.isEmpty()
                    || sdf.format(t.date).contains(lower)
                    || t.description.toLowerCase().contains(lower)
                    || t.debitAccount.toLowerCase().contains(lower)
                    || t.creditAccount.toLowerCase().contains(lower)) {
                transactionsModel.addRow(new Object[]{sdf.format(t.date), t.description, t.debitAccount, t.creditAccount, fmt(t.amount)});
            }
        }
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String extractType(String acc) {
        if (acc.contains("[Asset]")) return "Asset";
        if (acc.contains("[Liability]")) return "Liability";
        if (acc.contains("[Owner's Equity]")) return "Owner's Equity";
        if (acc.contains("[Revenue]")) return "Revenue";
        if (acc.contains("[Expense]")) return "Expense";
        return "";
    }

    private void updateLedgerForAccount(String account) {
        ledgerModel.setRowCount(0);
        double running = 0;
        for (Transaction t : transactions) {
            if (t.debitAccount.equals(account) || t.creditAccount.equals(account)) {
                double debit = t.debitAccount.equals(account) ? t.amount : 0;
                double credit = t.creditAccount.equals(account) ? t.amount : 0;
                running += debit - credit;
                ledgerModel.addRow(new Object[]{sdf.format(t.date), t.description, t.debitAccount, t.creditAccount, fmt(t.amount), fmt(running)});
            }
        }
    }

    private void refreshBalanceSheet() {
        assetsModel.setRowCount(0);
        liabilitiesModel.setRowCount(0);
        for (Map.Entry<String, Double> e : accountBalances.entrySet()) {
            String type = extractType(e.getKey());
            if (type.equals("Asset")) assetsModel.addRow(new Object[]{e.getKey(), fmt(e.getValue())});
            if (type.equals("Liability") || type.equals("Owner's Equity")) liabilitiesModel.addRow(new Object[]{e.getKey(), fmt(e.getValue())});
        }
    }

    private double calculateTotalAssets() {
        double total = 0;
        for (Map.Entry<String, Double> e : accountBalances.entrySet()) {
            if (extractType(e.getKey()).equals("Asset")) total += e.getValue();
        }
        return total;
    }

    private double calculateTotalLiabilitiesAndEquity() {
        double total = 0;
        for (Map.Entry<String, Double> e : accountBalances.entrySet()) {
            String type = extractType(e.getKey());
            if (type.equals("Liability") || type.equals("Owner's Equity")) total += e.getValue();
        }
        return total;
    }

    private String fmt(double d) {
        return String.format("%.2f", d);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AccountingAppPractice().setVisible(true));
    }

    // ------------------------
    // Transaction class
    // ------------------------
    private static class Transaction {
        Date date;
        String description, debitAccount, creditAccount;
        double amount;

        Transaction(Date date, String description, String debitAccount, String creditAccount, double amount) {
            this.date = date;
            this.description = description;
            this.debitAccount = debitAccount;
            this.creditAccount = creditAccount;
            this.amount = amount;
        }
    }
}
