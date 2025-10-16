import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/*
AccountingApp.java
 - Refreshes all views after changes
 - Fixed general ledger running balance logic
 - Uses Owner's Equity (ASCII apostrophe) consistently
 - Defensive null checks and input validation
 - Keeps in-memory storage, uses JTable and JTabbedPane
 Note: For production money calculations use BigDecimal.
*/

public class AccountingApp extends JFrame {
    private List<Account> accounts;
    private List<Transaction> transactions;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private DefaultTableModel transactionsTableModel;
    private DefaultTableModel accountsTableModel;
    private DefaultTableModel journalTableModel;
    private DefaultTableModel ledgerTableModel;
    private DefaultTableModel assetsTableModel;
    private DefaultTableModel liabilitiesTableModel;

    private JComboBox<String> ledgerAccountCombo;
    private JComboBox<String> debitComboGlobal;
    private JComboBox<String> creditComboGlobal;

    public AccountingApp() {
        sdf.setLenient(false);
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
        addPredefinedAccounts();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Add New Transaction", createAddTransactionPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Accounts", createAccountsPanel());
        tabbedPane.addTab("General Journal", createGeneralJournalPanel());
        tabbedPane.addTab("General Ledger", createGeneralLedgerPanel());
        tabbedPane.addTab("Balance Sheet", createBalanceSheetPanel());

        add(tabbedPane);
        setTitle("Accounting App");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        refreshAllViews();
    }

    private static class Account {
        private String name;
        private String type;
        private double balance;

        public Account(String name, String type, double initialBalance) {
            this.name = name;
            this.type = type;
            this.balance = initialBalance;
        }
        public String getName() { return name; }
        public String getType() { return type; }
        public double getBalance() { return balance; }

        public void applyDebit(double amount) {
            if (type.equals("Asset") || type.equals("Expense")) {
                balance += amount;
            } else {
                balance -= amount;
            }
        }
        public void applyCredit(double amount) {
            if (type.equals("Liability") || type.equals("Owner's Equity") || type.equals("Revenue")) {
                balance += amount;
            } else {
                balance -= amount;
            }
        }
    }

    private static class Transaction {
        private Date date;
        private String description;
        private String debitAccount;
        private String creditAccount;
        private double amount;

        public Transaction(Date date, String description, String debitAccount, String creditAccount, double amount) {
            this.date = date;
            this.description = description;
            this.debitAccount = debitAccount;
            this.creditAccount = creditAccount;
            this.amount = amount;
        }

        public Date getDate() { return date; }
        public String getDescription() { return description; }
        public String getDebitAccount() { return debitAccount; }
        public String getCreditAccount() { return creditAccount; }
        public double getAmount() { return amount; }
    }


    private void addPredefinedAccounts() {
        String[][] predefined = {
            {"Cash", "Asset"},
            {"Accounts Receivable", "Asset"},
            {"Inventory", "Asset"},
            {"Supplies", "Asset"},
            {"Prepaid Expenses", "Asset"},
            {"Equipment", "Asset"},
            {"Furniture and Fixtures", "Asset"},
            {"Land", "Asset"},
            {"Buildings", "Asset"},
            {"Accounts Payable", "Liability"},
            {"Notes Payable", "Liability"},
            {"Salaries Payable", "Liability"},
            {"Rent Payable", "Liability"},
            {"Interest Payable", "Liability"},
            {"Unearned Revenue", "Liability"},
            {"Owner's Capital", "Owner's Equity"},
            {"Owner's Drawing", "Owner's Equity"},
            {"Service Revenue", "Revenue"},
            {"Sales Revenue", "Revenue"},
            {"Interest Income", "Revenue"},
            {"Salaries Expense", "Expense"},
            {"Rent Expense", "Expense"},
            {"Utilities Expense", "Expense"},
            {"Supplies Expense", "Expense"},
            {"Depreciation Expense", "Expense"},
            {"Insurance Expense", "Expense"},
            {"Advertising Expense", "Expense"}
        };

        for (String[] acc : predefined) {
            accounts.add(new Account(acc[0], acc[1], 0.0));
        }
    }


    private JPanel createAddTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField dateField = new JTextField(sdf.format(new Date()));
        JTextField descField = new JTextField();
        debitComboGlobal = new JComboBox<>();
        creditComboGlobal = new JComboBox<>();
        JTextField amountField = new JTextField();

        refreshAccountCombos();

        JButton postBtn = new JButton("Post Transaction");
        JButton clearBtn = new JButton("Clear Fields");

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; form.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; form.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Debit Account:"), gbc);
        gbc.gridx = 1; form.add(debitComboGlobal, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Credit Account:"), gbc);
        gbc.gridx = 1; form.add(creditComboGlobal, gbc);

        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; form.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; form.add(postBtn, gbc);
        gbc.gridx = 1; form.add(clearBtn, gbc);

        panel.add(form, BorderLayout.NORTH);

        postBtn.addActionListener(e -> {
            String dateStr = dateField.getText().trim();
            String desc = descField.getText().trim();
            String debitAccName = (String) debitComboGlobal.getSelectedItem();
            String creditAccName = (String) creditComboGlobal.getSelectedItem();
            String amtStr = amountField.getText().trim();


            Date date;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount must be a number greater than zero.");
                return;
            }

            if (debitAccName == null || creditAccName == null) {
                JOptionPane.showMessageDialog(this, "Select both debit and credit accounts.");
                return;
            }
            if (debitAccName.equals(creditAccName)) {
                JOptionPane.showMessageDialog(this, "Debit and credit accounts cannot be the same.");
                return;
            }

            Account debitAcc = getAccountByName(debitAccName);
            Account creditAcc = getAccountByName(creditAccName);
            if (debitAcc == null || creditAcc == null) {
                JOptionPane.showMessageDialog(this, "Selected account not found.");
                return;
            }

            debitAcc.applyDebit(amount);
            creditAcc.applyCredit(amount);

            Transaction tx = new Transaction(date, desc, debitAccName, creditAccName, amount);
            transactions.add(tx);

            transactions.sort(Comparator.comparing(Transaction::getDate));

            refreshAllViews();

            JOptionPane.showMessageDialog(this, "Transaction posted.");
            dateField.setText(sdf.format(new Date()));
            descField.setText("");
            amountField.setText("");
            debitComboGlobal.setSelectedIndex(0);
            creditComboGlobal.setSelectedIndex(0);
        });

        clearBtn.addActionListener(e -> {
            dateField.setText(sdf.format(new Date()));
            descField.setText("");
            amountField.setText("");
            if (debitComboGlobal.getItemCount() > 0) debitComboGlobal.setSelectedIndex(0);
            if (creditComboGlobal.getItemCount() > 0) creditComboGlobal.setSelectedIndex(0);
        });

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(30);
        JButton searchBtn = new JButton("Search");
        top.add(new JLabel("Search (date or description):"));
        top.add(searchField);
        top.add(searchBtn);

        String[] columns = {"Date", "Description", "Debit Account", "Credit Account", "Amount"};
        transactionsTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(transactionsTableModel);
        table.setAutoCreateRowSorter(true);

        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            filterTransactions(query);
        });

        searchField.addActionListener(e -> filterTransactions(searchField.getText().trim().toLowerCase()));

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterTransactions(String query) {
        transactionsTableModel.setRowCount(0);
        for (int i = transactions.size()-1; i >= 0; i--) {
            Transaction tx = transactions.get(i);
            String dateStr = sdf.format(tx.getDate());
            if (query.isEmpty()
                    || dateStr.contains(query)
                    || tx.getDescription().toLowerCase().contains(query)
                    || tx.getDebitAccount().toLowerCase().contains(query)
                    || tx.getCreditAccount().toLowerCase().contains(query)) {
                transactionsTableModel.addRow(new Object[]{
                        dateStr,
                        tx.getDescription(),
                        tx.getDebitAccount(),
                        tx.getCreditAccount(),
                        String.format(Locale.US, "%.2f", tx.getAmount())
                });
            }
        }
    }


    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"Account Name", "Type", "Current Balance"};
        accountsTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(accountsTableModel);
        table.setAutoCreateRowSorter(true);

        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Asset", "Liability", "Owner's Equity", "Revenue", "Expense"});
        JTextField balanceField = new JTextField();
        JButton addBtn = new JButton("Add Account");

        gbc.gridx = 0; gbc.gridy = 0; addPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; addPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; addPanel.add(typeCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addPanel.add(new JLabel("Initial Balance (optional):"), gbc);
        gbc.gridx = 1; addPanel.add(balanceField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addPanel.add(new JLabel(""), gbc);
        gbc.gridx = 1; addPanel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter account name.");
                return;
            }
            if (getAccountByName(name) != null) {
                JOptionPane.showMessageDialog(this, "An account with this name already exists.");
                return;
            }
            String type = (String) typeCombo.getSelectedItem();
            double initBal = 0.0;
            if (!balanceField.getText().trim().isEmpty()) {
                try {
                    initBal = Double.parseDouble(balanceField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid initial balance. Use a number.");
                    return;
                }
            }
            accounts.add(new Account(name, type, initBal));
            refreshAllViews();
            nameField.setText("");
            balanceField.setText("");
            typeCombo.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Account added.");
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }


    private JPanel createGeneralJournalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Date", "Description", "Account", "Debit", "Credit"};
        journalTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(journalTableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createGeneralLedgerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        ledgerAccountCombo = new JComboBox<>();
        refreshLedgerAccountCombo();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Select Account:"));
        top.add(ledgerAccountCombo);

        String[] cols = {"Date", "Description", "Debit Account", "Credit Account", "Amount", "Running Balance"};
        ledgerTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(ledgerTableModel);
        table.setAutoCreateRowSorter(true);

        ledgerAccountCombo.addActionListener(e -> {
            String acc = (String) ledgerAccountCombo.getSelectedItem();
            if (acc != null) updateGeneralLedgerTable(acc);
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }


    private void updateGeneralLedgerTable(String accountName) {
        ledgerTableModel.setRowCount(0);
        Account acc = getAccountByName(accountName);
        if (acc == null) return;

        double running = 0.0;
        for (Transaction tx : transactions) {
            boolean affected = false;
            double amount = tx.getAmount();
            String dateStr = sdf.format(tx.getDate());
            if (tx.getDebitAccount().equals(accountName)) {
                if (acc.getType().equals("Asset") || acc.getType().equals("Expense")) {
                    running += amount;
                } else {
                    running -= amount;
                }
                ledgerTableModel.addRow(new Object[]{dateStr, tx.getDescription(), tx.getDebitAccount(), tx.getCreditAccount(),
                        String.format(Locale.US, "%.2f", amount),
                        String.format(Locale.US, "%.2f", running)});
                affected = true;
            }
            if (tx.getCreditAccount().equals(accountName)) {
                if (acc.getType().equals("Liability") || acc.getType().equals("Owner's Equity") || acc.getType().equals("Revenue")) {
                    running += amount;
                } else {
                    running -= amount;
                }
                ledgerTableModel.addRow(new Object[]{dateStr, tx.getDescription(), tx.getDebitAccount(), tx.getCreditAccount(),
                        String.format(Locale.US, "%.2f", amount),
                        String.format(Locale.US, "%.2f", running)});
                affected = true;
            }
        }
    }


    private JPanel createBalanceSheetPanel() {
        JPanel panel = new JPanel(new GridLayout(1,2));

        JPanel assetsPanel = new JPanel(new BorderLayout());
        assetsPanel.add(new JLabel("Assets", SwingConstants.CENTER), BorderLayout.NORTH);
        String[] assetCols = {"Account Name", "Amount"};
        assetsTableModel = new DefaultTableModel(assetCols, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable assetsTable = new JTable(assetsTableModel);
        assetsTable.setAutoCreateRowSorter(true);
        assetsPanel.add(new JScrollPane(assetsTable), BorderLayout.CENTER);
        JLabel totalAssetsLabel = new JLabel("", SwingConstants.RIGHT);
        assetsPanel.add(totalAssetsLabel, BorderLayout.SOUTH);

        JPanel liabilitiesPanel = new JPanel(new BorderLayout());
        liabilitiesPanel.add(new JLabel("Liabilities and Owner's Equity", SwingConstants.CENTER), BorderLayout.NORTH);
        String[] liabCols = {"Account Name", "Amount"};
        liabilitiesTableModel = new DefaultTableModel(liabCols, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable liabTable = new JTable(liabilitiesTableModel);
        liabTable.setAutoCreateRowSorter(true);
        liabilitiesPanel.add(new JScrollPane(liabTable), BorderLayout.CENTER);
        JLabel totalLiabLabel = new JLabel("", SwingConstants.RIGHT);
        liabilitiesPanel.add(totalLiabLabel, BorderLayout.SOUTH);

        panel.add(assetsPanel);
        panel.add(liabilitiesPanel);


        Runnable updateLabels = () -> {
            totalAssetsLabel.setText("Total Assets: " + String.format(Locale.US, "%.2f", calculateTotalAssets()));
            totalLiabLabel.setText("Total Liabilities and Equity: " + String.format(Locale.US, "%.2f", calculateTotalLiabilitiesAndEquity()));
        };

        updateLabels.run();

        panel.putClientProperty("updateLabels", updateLabels);
        return panel;
    }


    private Account getAccountByName(String name) {
        for (Account a : accounts) {
            if (a.getName().equals(name)) return a;
        }
        return null;
    }

    private List<String> getDebitAccountNames() {
        List<String> out = new ArrayList<>();
        for (Account a : accounts) {
            if (a.getType().equals("Asset") || a.getType().equals("Expense")) out.add(a.getName());
        }
        return out;
    }

    private List<String> getCreditAccountNames() {
        List<String> out = new ArrayList<>();
        for (Account a : accounts) {
            if (a.getType().equals("Liability") || a.getType().equals("Owner's Equity") || a.getType().equals("Revenue")) out.add(a.getName());
        }
        return out;
    }

    private List<String> getAllAccountNames() {
        List<String> out = new ArrayList<>();
        for (Account a : accounts) out.add(a.getName());
        return out;
    }

    private void refreshAccountCombos() {
        List<String> debits = getDebitAccountNames();
        List<String> credits = getCreditAccountNames();

        if (debitComboGlobal != null) {
            debitComboGlobal.removeAllItems();
            for (String s : debits) debitComboGlobal.addItem(s);
        }
        if (creditComboGlobal != null) {
            creditComboGlobal.removeAllItems();
            for (String s : credits) creditComboGlobal.addItem(s);
        }
    }

    private void refreshLedgerAccountCombo() {
        if (ledgerAccountCombo == null) return;
        ledgerAccountCombo.removeAllItems();
        for (String s : getAllAccountNames()) ledgerAccountCombo.addItem(s);
    }

    private void refreshLedgerAccountComboWrap() {
        refreshLedgerAccountCombo();
    }

    private void refreshAllViews() {
        if (transactionsTableModel != null) {
            filterTransactions("");
        }

        if (accountsTableModel != null) {
            accountsTableModel.setRowCount(0);
            for (Account a : accounts) {
                accountsTableModel.addRow(new Object[]{a.getName(), a.getType(), String.format(Locale.US, "%.2f", a.getBalance())});
            }
        }

        if (journalTableModel != null) {
            journalTableModel.setRowCount(0);
            for (Transaction tx : transactions) {
                String dateStr = sdf.format(tx.getDate());
                journalTableModel.addRow(new Object[]{dateStr, tx.getDescription(), tx.getDebitAccount(),
                        String.format(Locale.US, "%.2f", tx.getAmount()), ""});
                journalTableModel.addRow(new Object[]{dateStr, tx.getDescription(), tx.getCreditAccount(),
                        "", String.format(Locale.US, "%.2f", tx.getAmount())});
            }
        }

        refreshLedgerAccountCombo();
        if (ledgerAccountCombo != null && ledgerAccountCombo.getItemCount() > 0) {
            String sel = (String) ledgerAccountCombo.getSelectedItem();
            if (sel == null) sel = ledgerAccountCombo.getItemAt(0);
            ledgerAccountCombo.setSelectedItem(sel);
            updateGeneralLedgerTable(sel);
        }

        if (assetsTableModel != null && liabilitiesTableModel != null) {
            assetsTableModel.setRowCount(0);
            liabilitiesTableModel.setRowCount(0);
            for (Account a : accounts) {
                if (a.getType().equals("Asset")) {
                    assetsTableModel.addRow(new Object[]{a.getName(), String.format(Locale.US, "%.2f", a.getBalance())});
                } else if (a.getType().equals("Liability") || a.getType().equals("Owner's Equity")) {
                    liabilitiesTableModel.addRow(new Object[]{a.getName(), String.format(Locale.US, "%.2f", a.getBalance())});
                }
            }
        }

        refreshAccountCombos();

        JTabbedPane tp = (JTabbedPane) getContentPane().getComponent(0);
        for (int i = 0; i < tp.getTabCount(); i++) {
            Component c = tp.getComponentAt(i);
            if (c instanceof JPanel) {
                Object prop = ((JPanel) c).getClientProperty("updateLabels");
                if (prop instanceof Runnable) ((Runnable) prop).run();
            }
        }
    }

    private void updateGeneralLedgerTableModel(String accountName) {
        if (accountName == null) return;
        updateGeneralLedgerTable(accountName);
    }

    private double calculateTotalAssets() {
        double sum = 0;
        for (Account a : accounts) if (a.getType().equals("Asset")) sum += a.getBalance();
        return sum;
    }

    private double calculateTotalLiabilitiesAndEquity() {
        double sum = 0;
        for (Account a : accounts) if (a.getType().equals("Liability") || a.getType().equals("Owner's Equity")) sum += a.getBalance();
        return sum;
    }

    private JPanel createGeneralJournalPanelWrapper() {
        return createGeneralJournalPanel();
    }

    private JPanel createBalanceSheetPanelWrapper() {
        return createBalanceSheetPanel();
    }

    private JPanel createGeneralJournalPanelPublic() {
        return createGeneralJournalPanel();
    }

    private void updateTransactionsTableInternal() {
        filterTransactions("");
    }

    private void refreshLedgerAccountComboIfNeeded() {
        refreshLedgerAccountCombo();
    }

    private List<String> getAllAccountNamesSorted() {
        List<String> list = getAllAccountNames();
        Collections.sort(list);
        return list;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AccountingApp::new);
    }
}
