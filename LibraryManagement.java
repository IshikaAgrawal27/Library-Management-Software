import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class LibraryManagement extends JFrame {
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);  // Indigo
    private static final Color SECONDARY_COLOR = new Color(103, 58, 183); // Deep Purple
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);  // Off White
    private static final Color ACCENT_COLOR = new Color(33, 33, 33);  // Dark Gray
    private static final Color HIGHLIGHT_COLOR = new Color(92, 107, 192);  // Light Indigo
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);  // Green
    private static final Color WARNING_COLOR = new Color(255, 152, 0);  // Orange
    private static final Color ERROR_COLOR = new Color(244, 67, 54);  // Red

    // Constants for indices
    private static final int BOOK_ID_INDEX = 0;
    private static final int BOOK_TITLE_INDEX = 1;
    private static final int BOOK_AUTHOR_INDEX = 2;
    private static final int BOOK_PUBLISHER_INDEX = 3;
    private static final int BOOK_YEAR_INDEX = 4;
    private static final int BOOK_COPIES_INDEX = 5;
    private static final int BOOK_GENRE_INDEX = 6;

    private static final int USER_ID_INDEX = 0;
    private static final int USER_NAME_INDEX = 1;
    private static final int USER_CONTACT_INDEX = 2;
    private static final int USER_PASSWORD_INDEX = 3;

    private static final int ISSUED_BOOK_ID_INDEX = 0;
    private static final int ISSUED_USER_ID_INDEX = 1;
    private static final int ISSUED_USER_NAME_INDEX = 2;
    private static final int ISSUED_CONTACT_INDEX = 3;
    private static final int ISSUED_DATE_TIME_INDEX = 4;

    // Borrowing period constant
    private static final int BORROWING_PERIOD_DAYS = 14;

    // Data structures
    private ArrayList<String[]> books = new ArrayList<>();
    private ArrayList<String[]> issuedBooks = new ArrayList<>();
    private ArrayList<String[]> users = new ArrayList<>();
    private boolean isAdmin = false;
    private String currentUserId, currentUserName, currentUserContact;

    // UI components
    private BooksTableModel booksTableModel;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    // Theme properties
    private Color primaryColor = new Color(63, 81, 181); // Indigo
    private Color secondaryColor = new Color(255, 152, 0); // Orange
    private Color textColor = new Color(33, 33, 33);
    private Color backgroundColor = new Color(245, 245, 245);
    private Font headerFont = new Font("Arial", Font.BOLD, 16);
    private Font regularFont = new Font("Arial", Font.PLAIN, 14);
    
    // Theme and layout managers
    private ThemeManager themeManager;
    private LayoutManager layoutManager;

    public LibraryManagement() {
        initializeUI();
        loadData();
        loadIssuedData();
        loadUsersData();
        showLoginDialog();
    }

    private void initializeUI() {
        setTitle("Library Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Custom UI tweaks
        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("TabbedPane.selected", HIGHLIGHT_COLOR);
        UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
        UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
        UIManager.put("TableHeader.foreground", Color.BLACK);
        
        // Initialize theme and layout managers
        themeManager = new ThemeManager();
        layoutManager = new LayoutManager();
    
        // Apply default theme
        themeManager.applyTheme("light");
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Library Management System - Login", true);
        loginDialog.setLayout(new BorderLayout());
        
        // Header panel with logo
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        JLabel idLabel = new JLabel("Login Credentials:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField idField = new JTextField(15);
        idField.setPreferredSize(new Dimension(200, 35));
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField passField = new JPasswordField(15);
        passField.setPreferredSize(new Dimension(200, 35));
        
        JButton loginButton = createStyledButton("Login", 120, 40, "Login to the system");
        JButton registerButton = createStyledButton("Register", 120, 40, "Register a new user");
        registerButton.setBackground(SECONDARY_COLOR);
    
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(idLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(passField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        formPanel.add(buttonPanel, gbc);
    
        loginButton.addActionListener(e -> {
            String userId = idField.getText().trim();
            String password = new String(passField.getPassword());
            if (userId.equals("admin") && password.equals("admin123")) {
                isAdmin = true;
                currentUserId = "admin";
                currentUserName = "Admin";
                currentUserContact = "";
                loginDialog.dispose();
                showMainPanel();
            } else {
                for (String[] user : users) {
                    if (user[USER_ID_INDEX].equals(userId) && user[USER_PASSWORD_INDEX].equals(password)) {
                        isAdmin = false;
                        currentUserId = user[USER_ID_INDEX];
                        currentUserName = user[USER_NAME_INDEX];
                        currentUserContact = user[USER_CONTACT_INDEX];
                        loginDialog.dispose();
                        showMainPanel();
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        registerButton.addActionListener(e -> {
            loginDialog.setVisible(false);
            registerUser();
            loginDialog.setVisible(true);
        });
        
        // Add key listener for Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        };
        idField.addKeyListener(enterKeyListener);
        passField.addKeyListener(enterKeyListener);
        
        // Add components to dialog
        loginDialog.add(headerPanel, BorderLayout.NORTH);
        loginDialog.add(formPanel, BorderLayout.CENTER);
        
        loginDialog.pack();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(loginDialog);
        
        loginDialog.setVisible(true);
    }

    // Register a new user
    private void registerUser() {
        JDialog dialog = new JDialog(this, "Register New User", true);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(SECONDARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Create New Account");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(250, 35));
        JTextField contactField = new JTextField(20);
        contactField.setPreferredSize(new Dimension(250, 35));
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 35));
        JPasswordField confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(250, 35));
        
        String[] labels = {"Full Name:", "Contact Number:", "Password:", "Confirm Password:"};
        JTextField[] fields = {nameField, contactField, passwordField, confirmPasswordField};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0; gbc.gridy = i;
            formPanel.add(label, gbc);
            gbc.gridx = 1;
            formPanel.add(fields[i], gbc);
        }

        JButton submitButton = createStyledButton("Register", 120, 40, "Submit registration");
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel registration");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        submitButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String[] user = {
                "USER-" + (users.size() + 1000),
                nameField.getText().trim(),
                contactField.getText().trim(),
                password
            };
            if (validateRegistration(user)) {
                users.add(user);
                saveUsersData();
                JOptionPane.showMessageDialog(dialog, 
                    "Registration successful!\nYour User ID: " + user[USER_ID_INDEX], 
                    "Registration Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Validate user registration
    private boolean validateRegistration(String[] user) {
        for (String field : user) {
            if (field.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are mandatory!", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        // Validate contact number (only digits)
        if (!user[USER_CONTACT_INDEX].matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Contact number should contain only digits!", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate password length
        if (user[USER_PASSWORD_INDEX].length() < 6) {
            JOptionPane.showMessageDialog(this, "Password should be at least 6 characters long!", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    // Main panel with tabs
    private void showMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel(isAdmin ? "Admin" : currentUserName);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setForeground(PRIMARY_COLOR);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                getContentPane().removeAll();
                showLoginDialog();
            }
        });

        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(logoutButton);
        
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        // Tabbed pane
        tabbedPane = createMainTabbedPane();
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusPanel.setBackground(BACKGROUND_COLOR);
        
        statusLabel = new JLabel("Logged in as: " + currentUserName);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(dateLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();
        setVisible(true);
    }

    // Create tabs based on user type
    private JTabbedPane createMainTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        
        if (isAdmin) {
            tabbedPane.addTab("Books", createBooksPanel());
            tabbedPane.addTab("Issued Books", createIssuedBooksPanel());
            tabbedPane.addTab("Users", createUsersPanel());
            tabbedPane.addTab("Dashboard", createDashboardPanel());
        } else {
            tabbedPane.addTab("Books", createBooksPanel());
            tabbedPane.addTab("My Issued Books", createMyIssuedBooksPanel());
            tabbedPane.addTab("Profile", createProfilePanel());
        }
        return tabbedPane;
    }

    // Books panel with table and actions
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Book Catalog");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Table
        booksTableModel = new BooksTableModel();
        JTable table = new JTable(booksTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);
        
        TableRowSorter<BooksTableModel> sorter = new TableRowSorter<>(booksTableModel);
        table.setRowSorter(sorter);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(BOOK_YEAR_INDEX).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(BOOK_COPIES_INDEX).setCellRenderer(rightRenderer);
        
        // Center-align ID column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(BOOK_ID_INDEX).setCellRenderer(centerRenderer);
        
        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(BOOK_ID_INDEX).setPreferredWidth(80);
        columnModel.getColumn(BOOK_TITLE_INDEX).setPreferredWidth(200);
        columnModel.getColumn(BOOK_AUTHOR_INDEX).setPreferredWidth(150);
        columnModel.getColumn(BOOK_PUBLISHER_INDEX).setPreferredWidth(150);
        columnModel.getColumn(BOOK_YEAR_INDEX).setPreferredWidth(60);
        columnModel.getColumn(BOOK_COPIES_INDEX).setPreferredWidth(60);
        columnModel.getColumn(BOOK_GENRE_INDEX).setPreferredWidth(100);

        // Double-click to edit for admins or view details for users
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (isAdmin) {
                        showEditBookDialog(table);
                    } else {
                        showBookDetailsDialog(table);
                    }
                }
            }
        });
        
        // Search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        if (isAdmin) {
            JButton addButton = createStyledButton("Add Book", 120, 40, "Add a new book");
            addButton.setBackground(SUCCESS_COLOR);
            addButton.addActionListener(e -> showAddBookDialog());
            
            JButton editButton = createStyledButton("Edit Book", 120, 40, "Edit selected book");
            editButton.addActionListener(e -> showEditBookDialog(table));
            
            JButton deleteButton = createStyledButton("Delete Book", 120, 40, "Delete selected book");
            deleteButton.setBackground(ERROR_COLOR);
            deleteButton.addActionListener(e -> deleteBook(table));
            
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        } else {
            JButton issueButton = createStyledButton("Issue Book", 120, 40, "Issue selected book");
            issueButton.setBackground(SUCCESS_COLOR);
            issueButton.addActionListener(e -> issueBookFromTable(table));
            
            JButton detailsButton = createStyledButton("View Details", 120, 40, "View book details");
            detailsButton.addActionListener(e -> showBookDetailsDialog(table));
            
            buttonPanel.add(issueButton);
            buttonPanel.add(detailsButton);
        }
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Show book details dialog
    private void showBookDetailsDialog(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String[] book = books.get(modelRow);
        
        JDialog dialog = new JDialog(this, "Book Details", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Book Details");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(BACKGROUND_COLOR);
        detailsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        String[] labels = {"Book ID:", "Title:", "Author:", "Publisher:", "Year:", "Available Copies:", "Genre:"};
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            detailsPanel.add(label, gbc);
            
            gbc.gridx = 1;
            JLabel value = new JLabel(book[i]);
            value.setFont(new Font("Arial", Font.PLAIN, 14));
            detailsPanel.add(value, gbc);
        }
        
        JButton closeButton = createStyledButton("Close", 100, 35, "Close dialog");
        JButton issueButton = createStyledButton("Issue Book", 120, 35, "Issue this book");
        issueButton.setBackground(SUCCESS_COLOR);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        if (!isAdmin) {
            buttonPanel.add(issueButton);
            issueButton.addActionListener(e -> {
                dialog.dispose();
                issueBook(book[BOOK_ID_INDEX], currentUserId, currentUserName, currentUserContact);
                booksTableModel.fireTableDataChanged();
            });
        }
        
        buttonPanel.add(closeButton);
        closeButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Issued books panel for admins
    private JPanel createIssuedBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Issued Books");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Table
        IssuedBooksTableModel model = new IssuedBooksTableModel(issuedBooks);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);
        
        TableRowSorter<IssuedBooksTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Color-code overdue books
        IssuedBooksTableRenderer renderer = new IssuedBooksTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Book ID
        columnModel.getColumn(1).setPreferredWidth(80);  // User ID
        columnModel.getColumn(2).setPreferredWidth(150); // User Name
        columnModel.getColumn(3).setPreferredWidth(100); // Contact
        columnModel.getColumn(4).setPreferredWidth(150); // Issue Date
        columnModel.getColumn(5).setPreferredWidth(150); // Due Date
        columnModel.getColumn(6).setPreferredWidth(100); // Days Remaining

        // Search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton issueButton = createStyledButton("Issue Book", 120, 40, "Issue a book");
        issueButton.setBackground(SUCCESS_COLOR);
        issueButton.addActionListener(e -> showIssueBookDialog());
        
        JButton returnButton = createStyledButton("Return Book", 120, 40, "Return a book");
        returnButton.setBackground(WARNING_COLOR);
        returnButton.addActionListener(e -> showReturnBookDialog());
        
        JButton refreshButton = createStyledButton("Refresh", 120, 40, "Refresh the table");
        refreshButton.addActionListener(e -> {
            model.fireTableDataChanged();
            statusLabel.setText("Table refreshed");
        });
        
        buttonPanel.add(issueButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(refreshButton);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // User's issued books panel
    private JPanel createMyIssuedBooksPanel() {
        ArrayList<String[]> myIssuedBooks = new ArrayList<>();
        for (String[] issuedBook : issuedBooks) {
            if (issuedBook[ISSUED_USER_ID_INDEX].equals(currentUserId)) {
                myIssuedBooks.add(issuedBook);
            }
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("My Issued Books");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Table
        IssuedBooksTableModel model = new IssuedBooksTableModel(myIssuedBooks);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);
        
        TableRowSorter<IssuedBooksTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        IssuedBooksTableRenderer renderer = new IssuedBooksTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Book ID
        columnModel.getColumn(4).setPreferredWidth(150); // Issue Date
        columnModel.getColumn(5).setPreferredWidth(150); // Due Date
        columnModel.getColumn(6).setPreferredWidth(100); // Days Remaining

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel infoLabel = new JLabel("Double-click on a book to view details");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoPanel.add(infoLabel);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton returnButton = createStyledButton("Return Book", 120, 40, "Return selected book");
        returnButton.setBackground(WARNING_COLOR);
        returnButton.addActionListener(e -> returnBookFromTable(table, model));
        
        JButton viewButton = createStyledButton("View Details", 120, 40, "View book details");
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a book to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String[] issuedBook = myIssuedBooks.get(modelRow);
            showIssuedBookDetailsDialog(issuedBook);
        });
        
        buttonPanel.add(returnButton);
        buttonPanel.add(viewButton);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Show issued book details dialog
    private void showIssuedBookDetailsDialog(String[] issuedBook) {
        String bookId = issuedBook[ISSUED_BOOK_ID_INDEX];
        String[] book = null;
        
        for (String[] b : books) {
            if (b[BOOK_ID_INDEX].equals(bookId)) {
                book = b;
                break;
            }
        }
        
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Book details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Issued Book Details", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Issued Book Details");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(BACKGROUND_COLOR);
        detailsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        String[] labels = {
            "Book ID:", "Title:", "Author:", "Publisher:", "Year:", "Genre:",
            "Issue Date:", "Due Date:", "Days Remaining:"
        };
        
        String[] values = {
            book[BOOK_ID_INDEX], book[BOOK_TITLE_INDEX], book[BOOK_AUTHOR_INDEX],
            book[BOOK_PUBLISHER_INDEX], book[BOOK_YEAR_INDEX], book[BOOK_GENRE_INDEX],
            issuedBook[ISSUED_DATE_TIME_INDEX], getDueDate(issuedBook[ISSUED_DATE_TIME_INDEX]),
            getDaysRemaining(issuedBook[ISSUED_DATE_TIME_INDEX])
        };
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            detailsPanel.add(label, gbc);
            
            gbc.gridx = 1;
            JLabel value = new JLabel(values[i]);
            value.setFont(new Font("Arial", Font.PLAIN, 14));
            
            // Highlight overdue status
            if (i == 8 && values[i].contains("overdue")) {
                value.setForeground(ERROR_COLOR);
                value.setFont(new Font("Arial", Font.BOLD, 14));
            }
            
            detailsPanel.add(value, gbc);
        }
        
        JButton closeButton = createStyledButton("Close", 100, 35, "Close dialog");
        JButton returnButton = createStyledButton("Return Book", 120, 35, "Return this book");
        returnButton.setBackground(WARNING_COLOR);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(returnButton);
        buttonPanel.add(closeButton);
        
        returnButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, 
                "Are you sure you want to return this book?", 
                "Confirm Return", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 0; i < issuedBooks.size(); i++) {
                    String[] entry = issuedBooks.get(i);
                    if (entry[ISSUED_BOOK_ID_INDEX].equals(bookId) && 
                        entry[ISSUED_USER_ID_INDEX].equals(currentUserId)) {
                        issuedBooks.remove(i);
                        updateBookCopies(bookId);
                        saveData();
                        saveIssuedData();
                        statusLabel.setText("Book " + bookId + " returned successfully");
                        dialog.dispose();
                        
                        // Refresh the My Issued Books tab
                        tabbedPane.setComponentAt(1, createMyIssuedBooksPanel());
                        return;
                    }
                }
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }
    
    // Create users panel for admin
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Table
        UsersTableModel model = new UsersTableModel();
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);
        
        TableRowSorter<UsersTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton addButton = createStyledButton("Add User", 120, 40, "Add a new user");
        addButton.setBackground(SUCCESS_COLOR);
        addButton.addActionListener(e -> registerUser());
        
        JButton viewButton = createStyledButton("View User", 120, 40, "View user details");
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String userId = (String) table.getValueAt(selectedRow, USER_ID_INDEX);
            
            for (String[] user : users) {
                if (user[USER_ID_INDEX].equals(userId)) {
                    showUserDetailsDialog(user);
                    break;
                }
            }
        });
        
        JButton deleteButton = createStyledButton("Delete User", 120, 40, "Delete selected user");
        deleteButton.setBackground(ERROR_COLOR);
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String userId = (String) table.getValueAt(selectedRow, USER_ID_INDEX);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete user " + userId + "?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // Check if user has issued books
                boolean hasIssuedBooks = false;
                for (String[] issuedBook : issuedBooks) {
                    if (issuedBook[ISSUED_USER_ID_INDEX].equals(userId)) {
                        hasIssuedBooks = true;
                        break;
                    }
                }
                
                if (hasIssuedBooks) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot delete user with issued books.\nPlease return all books first.", 
                        "Delete Failed", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                users.removeIf(user -> user[USER_ID_INDEX].equals(userId));
                saveUsersData();
                model.fireTableDataChanged();
                statusLabel.setText("User " + userId + " deleted successfully");
            }
        });
        
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(deleteButton);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Show user details dialog
    private void showUserDetailsDialog(String[] user) {
        JDialog dialog = new JDialog(this, "User Details", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("User Details");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(BACKGROUND_COLOR);
        detailsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        String[] labels = {"User ID:", "Full Name:", "Contact Number:"};
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            detailsPanel.add(label, gbc);
            
            gbc.gridx = 1;
            JLabel value = new JLabel(user[i]);
            value.setFont(new Font("Arial", Font.PLAIN, 14));
            detailsPanel.add(value, gbc);
        }
        
        // Count issued books
        int issuedCount = 0;
        for (String[] issuedBook : issuedBooks) {
            if (issuedBook[ISSUED_USER_ID_INDEX].equals(user[USER_ID_INDEX])) {
                issuedCount++;
            }
        }
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel issuedLabel = new JLabel("Books Issued:");
        issuedLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(issuedLabel, gbc);
        
        gbc.gridx = 1;
        JLabel issuedValue = new JLabel(String.valueOf(issuedCount));
        issuedValue.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsPanel.add(issuedValue, gbc);
        
        JButton closeButton = createStyledButton("Close", 100, 35, "Close dialog");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(closeButton);
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }
    
    // Create dashboard panel for admin
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Total books
        JPanel totalBooksPanel = createStatPanel("Total Books", String.valueOf(books.size()), PRIMARY_COLOR);
        
        // Total users
        JPanel totalUsersPanel = createStatPanel("Total Users", String.valueOf(users.size()), SECONDARY_COLOR);
        
        // Books issued
        JPanel booksIssuedPanel = createStatPanel("Books Issued", String.valueOf(issuedBooks.size()), SUCCESS_COLOR);
        
        // Overdue books
        int overdueCount = 0;
        for (String[] issuedBook : issuedBooks) {
            String daysRemaining = getDaysRemaining(issuedBook[ISSUED_DATE_TIME_INDEX]);
            if (daysRemaining.contains("overdue")) {
                overdueCount++;
            }
        }
        JPanel overdueBooksPanel = createStatPanel("Overdue Books", String.valueOf(overdueCount), ERROR_COLOR);
        
        statsPanel.add(totalBooksPanel);
        statsPanel.add(totalUsersPanel);
        statsPanel.add(booksIssuedPanel);
        statsPanel.add(overdueBooksPanel);
        
        // Recent activity panel
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(BACKGROUND_COLOR);
        activityPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR),
            "Recent Activity",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        // Sort issued books by date (most recent first)
        ArrayList<String[]> recentActivity = new ArrayList<>(issuedBooks);
        recentActivity.sort((a, b) -> b[ISSUED_DATE_TIME_INDEX].compareTo(a[ISSUED_DATE_TIME_INDEX]));
        
        // Take only the 5 most recent
        if (recentActivity.size() > 5) {
            recentActivity = new ArrayList<>(recentActivity.subList(0, 5));
        }
        
        // Create activity list
        DefaultListModel<String> activityListModel = new DefaultListModel<>();
        for (String[] activity : recentActivity) {
            String bookId = activity[ISSUED_BOOK_ID_INDEX];
            String bookTitle = "";
            for (String[] book : books) {
                if (book[BOOK_ID_INDEX].equals(bookId)) {
                    bookTitle = book[BOOK_TITLE_INDEX];
                    break;
                }
            }
            
            activityListModel.addElement(String.format(
                "<html><b>%s</b> issued to <b>%s</b> on %s</html>",
                bookTitle.isEmpty() ? bookId : bookTitle,
                activity[ISSUED_USER_NAME_INDEX],
                activity[ISSUED_DATE_TIME_INDEX]
            ));
        }
        
        JList<String> activityList = new JList<>(activityListModel);
        activityList.setFont(new Font("Arial", Font.PLAIN, 14));
        activityList.setFixedCellHeight(40);
        
        JScrollPane activityScrollPane = new JScrollPane(activityList);
        activityScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        activityPanel.add(activityScrollPane, BorderLayout.CENTER);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(activityPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Create a stat panel for dashboard
    private JPanel createStatPanel(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(color);
        titleLabel.setBorder(new EmptyBorder(10, 15, 5, 15));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setForeground(ACCENT_COLOR);
        valueLabel.setBorder(new EmptyBorder(10, 15, 20, 15));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Create profile panel for users
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Profile panel
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(BACKGROUND_COLOR);
        profilePanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Find current user
        String[] currentUser = null;
        for (String[] user : users) {
            if (user[USER_ID_INDEX].equals(currentUserId)) {
                currentUser = user;
                break;
            }
        }
        
        if (currentUser != null) {
            String[] labels = {"User ID:", "Full Name:", "Contact Number:"};
            String[] values = {
                currentUser[USER_ID_INDEX],
                currentUser[USER_NAME_INDEX],
                currentUser[USER_CONTACT_INDEX]
            };
            
            for (int i = 0; i < labels.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                JLabel label = new JLabel(labels[i]);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                profilePanel.add(label, gbc);
                
                gbc.gridx = 1;
                JLabel value = new JLabel(values[i]);
                value.setFont(new Font("Arial", Font.PLAIN, 16));
                profilePanel.add(value, gbc);
            }
            
            // Count issued books
            int issuedCount = 0;
            for (String[] issuedBook : issuedBooks) {
                if (issuedBook[ISSUED_USER_ID_INDEX].equals(currentUserId)) {
                    issuedCount++;
                }
            }
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            JLabel issuedLabel = new JLabel("Books Issued:");
            issuedLabel.setFont(new Font("Arial", Font.BOLD, 16));
            profilePanel.add(issuedLabel, gbc);
            
            gbc.gridx = 1;
            JLabel issuedValue = new JLabel(String.valueOf(issuedCount));
            issuedValue.setFont(new Font("Arial", Font.PLAIN, 16));
            profilePanel.add(issuedValue, gbc);
        }
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton changePasswordButton = createStyledButton("Change Password", 180, 40, "Change your password");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        
        buttonPanel.add(changePasswordButton);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(profilePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Show change password dialog
    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Change Password");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel currentPassLabel = new JLabel("Current Password:");
        currentPassLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField currentPassField = new JPasswordField(20);
        currentPassField.setPreferredSize(new Dimension(250, 35));
        
        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField newPassField = new JPasswordField(20);
        newPassField.setPreferredSize(new Dimension(250, 35));
        
        JLabel confirmPassLabel = new JLabel("Confirm New Password:");
        confirmPassLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField confirmPassField = new JPasswordField(20);
        confirmPassField.setPreferredSize(new Dimension(250, 35));
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(currentPassLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(currentPassField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(newPassLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(newPassField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(confirmPassLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPassField, gbc);
        
        JButton saveButton = createStyledButton("Save", 120, 40, "Save new password");
        saveButton.setBackground(SUCCESS_COLOR);
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        saveButton.addActionListener(e -> {
            String currentPass = new String(currentPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            
            // Find current user
            String[] currentUser = null;
            for (String[] user : users) {
                if (user[USER_ID_INDEX].equals(currentUserId)) {
                    currentUser = user;
                    break;
                }
            }
            
            if (currentUser != null) {
                if (!currentUser[USER_PASSWORD_INDEX].equals(currentPass)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Current password is incorrect!", 
                        "Password Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (newPass.length() < 6) {
                    JOptionPane.showMessageDialog(dialog, 
                        "New password should be at least 6 characters long!", 
                        "Password Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!newPass.equals(confirmPass)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "New passwords do not match!", 
                        "Password Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                currentUser[USER_PASSWORD_INDEX] = newPass;
                saveUsersData();
                JOptionPane.showMessageDialog(dialog, 
                    "Password changed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Styled button with tooltip and hover effect
    private JButton createStyledButton(String text, int width, int height, String tooltip) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(HIGHLIGHT_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    // Dialog to add a new book
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add Book", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Add New Book");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Book ID:", "Title:", "Author:", "Publisher:", "Year:", "Copies:", "Genre:"};
        JTextField[] fields = new JTextField[7];
        
        // Generate a unique book ID
        String bookId = "BOOK-" + (books.size() + 1000);
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            formPanel.add(label, gbc);
            
            gbc.gridx = 1;
            fields[i] = new JTextField(20);
            fields[i].setPreferredSize(new Dimension(250, 35));
            
            if (i == BOOK_ID_INDEX) {
                fields[i].setText(bookId);
                fields[i].setEditable(false);
                fields[i].setBackground(Color.LIGHT_GRAY);
            }
            
            formPanel.add(fields[i], gbc);
        }

        JButton submitButton = createStyledButton("Add Book", 120, 40, "Add book to library");
        submitButton.setBackground(SUCCESS_COLOR);
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        submitButton.addActionListener(e -> {
            String[] book = new String[7];
            for (int i = 0; i < fields.length; i++) {
                book[i] = fields[i].getText().trim();
            }
            if (validateBook(book)) {
                books.add(book);
                saveData();
                booksTableModel.fireTableDataChanged();
                statusLabel.setText("Book added successfully");
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Dialog to edit a selected book
    private void showEditBookDialog(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String bookId = (String) table.getValueAt(selectedRow, BOOK_ID_INDEX);
        String[] bookToEdit = null;
        
        for (String[] book : books) {
            if (book[BOOK_ID_INDEX].equals(bookId)) {
                bookToEdit = book;
                break;
            }
        }
        
        if (bookToEdit == null) {
            JOptionPane.showMessageDialog(this, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Book", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Edit Book");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Book ID:", "Title:", "Author:", "Publisher:", "Year:", "Copies:", "Genre:"};
        JTextField[] fields = new JTextField[7];
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            formPanel.add(label, gbc);
            
            gbc.gridx = 1;
            fields[i] = new JTextField(bookToEdit[i], 20);
            fields[i].setPreferredSize(new Dimension(250, 35));
            
            if (i == BOOK_ID_INDEX) {
                fields[i].setEditable(false);
                fields[i].setBackground(Color.LIGHT_GRAY);
            }
            
            formPanel.add(fields[i], gbc);
        }

        JButton submitButton = createStyledButton("Save Changes", 150, 40, "Save changes");
        submitButton.setBackground(SUCCESS_COLOR);
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        submitButton.addActionListener(e -> {
            String[] updatedBook = new String[7];
            for (int i = 0; i < fields.length; i++) {
                updatedBook[i] = fields[i].getText().trim();
            }
            if (validateBook(updatedBook)) {
                for (int i = 0; i < books.size(); i++) {
                    if (books.get(i)[BOOK_ID_INDEX].equals(bookId)) {
                        books.set(i, updatedBook);
                        break;
                    }
                }
                saveData();
                booksTableModel.fireTableDataChanged();
                statusLabel.setText("Book updated successfully");
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Validate book data
    private boolean validateBook(String[] book) {
        for (String field : book) {
            if (field.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are mandatory!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        try {
            Integer.parseInt(book[BOOK_YEAR_INDEX]);
            int copies = Integer.parseInt(book[BOOK_COPIES_INDEX]);
            if (copies < 0) {
                JOptionPane.showMessageDialog(this, "Copies must be non-negative!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Year and Copies must be numbers!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if year is valid
        int year = Integer.parseInt(book[BOOK_YEAR_INDEX]);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year < 1000 || year > currentYear) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year (1000-" + currentYear + ")!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check for duplicate book ID (only for new books)
        // Fixed: Don't check for duplicate ID when editing an existing book
        String bookId = book[BOOK_ID_INDEX];
        boolean isExistingBook = false;
        
        // Check if this is an existing book being edited
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i)[BOOK_ID_INDEX].equals(bookId)) {
                isExistingBook = true;
                break;
            }
        }
        
        // Only check for duplicate ID if it's a new book
        if (!isExistingBook) {
            for (String[] b : books) {
                if (b[BOOK_ID_INDEX].equals(bookId)) {
                    JOptionPane.showMessageDialog(this, "Book ID already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        
        return true;
    }

    // Delete a selected book
    private void deleteBook(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String bookId = (String) table.getValueAt(selectedRow, BOOK_ID_INDEX);
        
        // Check if book is issued
        for (String[] issuedBook : issuedBooks) {
            if (issuedBook[ISSUED_BOOK_ID_INDEX].equals(bookId)) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot delete a book that is currently issued.\nPlease return all copies first.", 
                    "Delete Failed", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete book " + bookId + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            books.removeIf(book -> book[BOOK_ID_INDEX].equals(bookId));
            saveData();
            booksTableModel.fireTableDataChanged();
            statusLabel.setText("Book deleted successfully");
        }
    }

    // Issue a book from the table for users
    private void issueBookFromTable(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to issue.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String bookId = (String) table.getValueAt(selectedRow, BOOK_ID_INDEX);
        
        // Check if user already has this book
        for (String[] issuedBook : issuedBooks) {
            if (issuedBook[ISSUED_BOOK_ID_INDEX].equals(bookId) && 
                issuedBook[ISSUED_USER_ID_INDEX].equals(currentUserId)) {
                JOptionPane.showMessageDialog(this, 
                    "You already have this book issued.", 
                    "Issue Failed", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Do you want to issue this book?", 
            "Confirm Issue", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            issueBook(bookId, currentUserId, currentUserName, currentUserContact);
            booksTableModel.fireTableDataChanged();
            
            // Refresh the My Issued Books tab if it exists
            if (tabbedPane.getTabCount() > 1) {
                tabbedPane.setComponentAt(1, createMyIssuedBooksPanel());
            }
        }
    }

    // Admin dialog to issue a book
    private void showIssueBookDialog() {
        JDialog dialog = new JDialog(this, "Issue Book", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Issue Book");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel bookIdLabel = new JLabel("Book ID:");
        bookIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField bookIdField = new JTextField(20);
        bookIdField.setPreferredSize(new Dimension(250, 35));
        
        JCheckBox guestCheck = new JCheckBox("Issue to Guest");
        guestCheck.setFont(new Font("Arial", Font.BOLD, 14));
        guestCheck.setBackground(BACKGROUND_COLOR);
        
        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create combo box with user IDs and names
        Vector<String> userOptions = new Vector<>();
        for (String[] user : users) {
            userOptions.add(user[USER_ID_INDEX] + " - " + user[USER_NAME_INDEX]);
        }
        JComboBox<String> userIdCombo = new JComboBox<>(userOptions);
        userIdCombo.setPreferredSize(new Dimension(250, 35));
        
        JLabel nameLabel = new JLabel("Guest Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(250, 35));
        
        JLabel contactLabel = new JLabel("Guest Contact:");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField contactField = new JTextField(20);
        contactField.setPreferredSize(new Dimension(250, 35));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(bookIdLabel, gbc);
        gbc.gridx = 1; formPanel.add(bookIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; formPanel.add(guestCheck, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1; formPanel.add(userIdCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(contactLabel, gbc);
        gbc.gridx = 1; formPanel.add(contactField, gbc);

        nameField.setEnabled(false);
        contactField.setEnabled(false);
        guestCheck.addActionListener(e -> {
            boolean isGuest = guestCheck.isSelected();
            userIdCombo.setEnabled(!isGuest);
            nameField.setEnabled(isGuest);
            contactField.setEnabled(isGuest);
        });

        JButton submitButton = createStyledButton("Issue Book", 150, 40, "Issue the book");
        submitButton.setBackground(SUCCESS_COLOR);
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        submitButton.addActionListener(e -> {
            String bookId = bookIdField.getText().trim();
            if (bookId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Book ID is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if book exists and has copies
            boolean bookFound = false;
            for (String[] book : books) {
                if (book[BOOK_ID_INDEX].equals(bookId)) {
                    bookFound = true;
                    int copies = Integer.parseInt(book[BOOK_COPIES_INDEX]);
                    if (copies <= 0) {
                        JOptionPane.showMessageDialog(dialog, "No copies available for this book!", "Issue Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    break;
                }
            }
            
            if (!bookFound) {
                JOptionPane.showMessageDialog(dialog, "Book not found!", "Issue Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (guestCheck.isSelected()) {
                String name = nameField.getText().trim();
                String contact = contactField.getText().trim();
                if (name.isEmpty() || contact.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and contact are required for guests!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                issueBook(bookId, "", name, contact);
            } else {
                String selectedUser = (String) userIdCombo.getSelectedItem();
                if (selectedUser != null) {
                    String userId = selectedUser.split(" - ")[0];
                    String[] user = null;
                    for (String[] u : users) {
                        if (u[USER_ID_INDEX].equals(userId)) {
                            user = u;
                            break;
                        }
                    }
                    if (user != null) {
                        issueBook(bookId, user[USER_ID_INDEX], user[USER_NAME_INDEX], user[USER_CONTACT_INDEX]);
                    }
                }
            }
            booksTableModel.fireTableDataChanged();
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Issue a book with given details
    private void issueBook(String bookId, String userId, String userName, String contact) {
        for (String[] book : books) {
            if (book[BOOK_ID_INDEX].equals(bookId)) {
                int copies = Integer.parseInt(book[BOOK_COPIES_INDEX]);
                if (copies > 0) {
                    String issueDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    issuedBooks.add(new String[]{bookId, userId, userName, contact, issueDateTime});
                    book[BOOK_COPIES_INDEX] = String.valueOf(copies - 1);
                    saveData();
                    saveIssuedData();
                    statusLabel.setText("Book " + bookId + " issued successfully");
                    
                    // Show success message
                    JOptionPane.showMessageDialog(this, 
                        "Book issued successfully!\nDue date: " + getDueDate(issueDateTime), 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "No copies available!", "Issue Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Book not found!", "Issue Failed", JOptionPane.ERROR_MESSAGE);
    }

    // Admin dialog to return a book
    private void showReturnBookDialog() {
        JDialog dialog = new JDialog(this, "Return Book", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Return Book");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel bookIdLabel = new JLabel("Book ID:");
        bookIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField bookIdField = new JTextField(20);
        bookIdField.setPreferredSize(new Dimension(250, 35));
        
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(bookIdLabel, gbc);
        gbc.gridx = 1; formPanel.add(bookIdField, gbc);

        JButton submitButton = createStyledButton("Find Book", 150, 40, "Find the book");
        submitButton.setBackground(PRIMARY_COLOR);
        JButton cancelButton = createStyledButton("Cancel", 120, 40, "Cancel");
        cancelButton.setBackground(Color.GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        submitButton.addActionListener(e -> {
            String bookId = bookIdField.getText().trim();
            if (bookId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Book ID is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ArrayList<String[]> candidates = new ArrayList<>();
            for (String[] entry : issuedBooks) {
                if (entry[ISSUED_BOOK_ID_INDEX].equals(bookId)) {
                    candidates.add(entry);
                }
            }
            
            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No issued books found for this Book ID.", "Not Found", JOptionPane.WARNING_MESSAGE);
            } else if (candidates.size() == 1) {
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                    "Return book issued to " + candidates.get(0)[ISSUED_USER_NAME_INDEX] + "?", 
                    "Confirm Return", 
                    JOptionPane.YES_NO_OPTION);
        
                
                if (confirm == JOptionPane.YES_OPTION) {
                    issuedBooks.remove(candidates.get(0));
                    updateBookCopies(bookId);
                    saveData();
                    saveIssuedData();
                    statusLabel.setText("Book " + bookId + " returned successfully");
                    JOptionPane.showMessageDialog(dialog, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            } else {
                // Multiple copies of the same book issued to different users
                String[] options = candidates.stream()
                    .map(entry -> "User: " + entry[ISSUED_USER_NAME_INDEX] + ", Issued: " + entry[ISSUED_DATE_TIME_INDEX])
                    .toArray(String[]::new);
                    
                String selected = (String) JOptionPane.showInputDialog(
                    dialog, "Select the book to return:", "Return Book",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                    
                if (selected != null) {
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(selected)) {
                            issuedBooks.remove(candidates.get(i));
                            updateBookCopies(bookId);
                            saveData();
                            saveIssuedData();
                            statusLabel.setText("Book " + bookId + " returned successfully");
                            JOptionPane.showMessageDialog(dialog, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            break;
                        }
                    }
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Apply styling to dialog buttons
        styleDialogButtons(dialog);
        
        dialog.setVisible(true);
    }

    // Return a book from user's table
    private void returnBookFromTable(JTable table, IssuedBooksTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to return.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String bookId = (String) table.getValueAt(selectedRow, ISSUED_BOOK_ID_INDEX);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to return this book?", 
            "Confirm Return", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = 0; i < issuedBooks.size(); i++) {
                String[] entry = issuedBooks.get(i);
                if (entry[ISSUED_BOOK_ID_INDEX].equals(bookId) && entry[ISSUED_USER_ID_INDEX].equals(currentUserId)) {
                    issuedBooks.remove(i);
                    updateBookCopies(bookId);
                    saveData();
                    saveIssuedData();
                    model.fireTableDataChanged();
                    statusLabel.setText("Book " + bookId + " returned successfully");
                    JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Book not found in your issued list.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update book copies after return
    private void updateBookCopies(String bookId) {
        for (String[] book : books) {
            if (book[BOOK_ID_INDEX].equals(bookId)) {
                int copies = Integer.parseInt(book[BOOK_COPIES_INDEX]);
                book[BOOK_COPIES_INDEX] = String.valueOf(copies + 1);
                break;
            }
        }
    }

    // Table model for books
    class BooksTableModel extends AbstractTableModel {
        private String[] columnNames = {"Book ID", "Title", "Author", "Publisher", "Year", "Copies", "Genre"};

        @Override
        public int getRowCount() {
            return books.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return books.get(row)[col];
        }
    }

    // Table model for issued books
    class IssuedBooksTableModel extends AbstractTableModel {
        private String[] columnNames = {"Book ID", "User ID", "User Name", "Contact", "Issue Date/Time", "Due Date", "Days Remaining"};
        private ArrayList<String[]> issuedBooksList;

        public IssuedBooksTableModel(ArrayList<String[]> issuedBooksList) {
            this.issuedBooksList = new ArrayList<>(issuedBooksList);
        }

        @Override
        public int getRowCount() {
            return issuedBooksList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            String[] issuedBook = issuedBooksList.get(row);
            switch (col) {
                case 0: return issuedBook[ISSUED_BOOK_ID_INDEX];
                case 1: return issuedBook[ISSUED_USER_ID_INDEX];
                case 2: return issuedBook[ISSUED_USER_NAME_INDEX];
                case 3: return issuedBook[ISSUED_CONTACT_INDEX];
                case 4: return issuedBook[ISSUED_DATE_TIME_INDEX];
                case 5: return getDueDate(issuedBook[ISSUED_DATE_TIME_INDEX]);
                case 6: return getDaysRemaining(issuedBook[ISSUED_DATE_TIME_INDEX]);
                default: return "";
            }
        }
    }
    
    // Table model for users
    class UsersTableModel extends AbstractTableModel {
        private String[] columnNames = {"User ID", "Full Name", "Contact Number"};

        @Override
        public int getRowCount() {
            return users.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return users.get(row)[col];
        }
    }

    // Renderer to color-code overdue books
    class IssuedBooksTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String daysRemaining = (String) table.getValueAt(row, 6);
                if (daysRemaining.contains("overdue")) {
                    c.setBackground(new Color(255, 235, 235)); // Light red
                    if (column == 6) {
                        c.setForeground(ERROR_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(table.getForeground());
                    }
                } else if (daysRemaining.contains("today")) {
                    c.setBackground(new Color(255, 252, 235)); // Light yellow
                    if (column == 6) {
                        c.setForeground(WARNING_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(table.getForeground());
                    }
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
            
            // Center align certain columns
            if (column == 0 || column == 1 || column == 6) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }
            
            return c;
        }
    }

    // Calculate due date
    private String getDueDate(String issueDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date issueDate = sdf.parse(issueDateTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(issueDate);
            cal.add(Calendar.DAY_OF_MONTH, BORROWING_PERIOD_DAYS);
            return sdf.format(cal.getTime());
        } catch (ParseException e) {
            return "Invalid date";
        }
    }

    // Calculate days remaining
    private String getDaysRemaining(String issueDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date issueDate = sdf.parse(issueDateTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(issueDate);
            cal.add(Calendar.DAY_OF_MONTH, BORROWING_PERIOD_DAYS);
            Date dueDate = cal.getTime();
            Date currentDate = new Date();
            long diff = dueDate.getTime() - currentDate.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 0) return days + " days remaining";
            else if (days < 0) return (-days) + " days overdue";
            else return "Due today";
        } catch (ParseException e) {
            return "Invalid date";
        }
    }

    // Save and load methods
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("books.dat"))) {
            oos.writeObject(books);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save books data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveIssuedData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("issued_books.dat"))) {
            oos.writeObject(issuedBooks);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save issued books data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUsersData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save users data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("books.dat"))) {
            books = (ArrayList<String[]>) ois.readObject();
        } catch (FileNotFoundException e) {
            // No data file found, create sample data
            createSampleBooks();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Failed to load books data: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadIssuedData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("issued_books.dat"))) {
            issuedBooks = (ArrayList<String[]>) ois.readObject();
            // Update old format if needed
            for (int i = 0; i < issuedBooks.size(); i++) {
                String[] oldEntry = issuedBooks.get(i);
                if (oldEntry.length == 4) {
                    String[] newEntry = new String[5];
                    newEntry[ISSUED_BOOK_ID_INDEX] = oldEntry[0];
                    newEntry[ISSUED_USER_ID_INDEX] = "";
                    newEntry[ISSUED_USER_NAME_INDEX] = oldEntry[1];
                    newEntry[ISSUED_CONTACT_INDEX] = oldEntry[2];
                    newEntry[ISSUED_DATE_TIME_INDEX] = oldEntry[3];
                    issuedBooks.set(i, newEntry);
                }
            }
        } catch (FileNotFoundException e) {
            // No issued books file found
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Failed to load issued books data: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsersData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            users = (ArrayList<String[]>) ois.readObject();
        } catch (FileNotFoundException e) {
            // No users file found, create sample users
            createSampleUsers();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users data: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Create sample books for first run
    private void createSampleBooks() {
        // Don't create any sample books - rely on books.dat file
        saveData();
    }
    
    // Create sample users for first run
    private void createSampleUsers() {
        // No sample users - they will be created during registration
        saveUsersData();
    }

    // Method to style dialog buttons - fixes the text color issue
    private void styleDialogButtons(JDialog dialog) {
    // Force dark text on all buttons in the dialog
    UIManager.put("Button.foreground", new Color(33, 33, 33)); // Dark text
    UIManager.put("Button.background", new Color(240, 240, 240)); // Light background
    
    // Apply to all components in the dialog
    styleComponentsInContainer(dialog.getContentPane());
    
    // Also handle any potential JOptionPane dialogs that might be created
    UIManager.put("OptionPane.buttonFont", regularFont);
    
    // Ensure JOptionPane buttons have proper colors
    UIManager.put("OptionPane.background", backgroundColor);
    UIManager.put("Panel.background", backgroundColor);
}

// Helper method to recursively style components
private void styleComponentsInContainer(Container container) {
    for (Component comp : container.getComponents()) {
        if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            // Set contrasting colors - dark text on light background
            button.setForeground(new Color(33, 33, 33)); // Dark text - hardcoded to ensure visibility
            button.setBackground(new Color(240, 240, 240)); // Light background
            button.setFont(regularFont);
            button.setBorderPainted(true);
            button.setOpaque(true);
        }
        if (comp instanceof Container) {
            styleComponentsInContainer((Container) comp);
        }
    }
}

    // Theme manager class for modular theming
    private class ThemeManager {
        public void applyTheme(String themeName) {
            switch (themeName) {
                case "dark":
                    primaryColor = new Color(98, 0, 238);
                    secondaryColor = new Color(255, 145, 0);
                    textColor = new Color(255, 255, 255);
                    backgroundColor = new Color(33, 33, 33);
                    break;
                case "light":
                    primaryColor = new Color(63, 81, 181);
                    secondaryColor = new Color(255, 152, 0);
                    textColor = new Color(33, 33, 33);
                    backgroundColor = new Color(245, 245, 245);
                    break;
                case "high-contrast":
                    primaryColor = new Color(0, 0, 0);
                    secondaryColor = new Color(255, 255, 0);
                    textColor = new Color(255, 255, 255);
                    backgroundColor = new Color(0, 0, 0);
                    break;
            }
            refreshUI();
        }
        
        // Method to refresh UI when theme changes
        private void refreshUI() {
            // Apply colors and fonts to all components
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                applyThemeToComponents(window);
            }
        }
        
        // Apply theme to all components recursively
        private void applyThemeToComponents(Component component) {
            if (component instanceof JPanel) {
                component.setBackground(backgroundColor);
            }
            if (component instanceof JLabel) {
                component.setForeground(textColor);
                ((JLabel) component).setFont(regularFont);
            }
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setBackground(primaryColor);
                button.setForeground(Color.WHITE);
                button.setFont(regularFont);
            }
            if (component instanceof JTextField || component instanceof JTextArea) {
                component.setForeground(textColor);
                component.setBackground(Color.WHITE);
                ((JTextComponent) component).setFont(regularFont);
            }
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    applyThemeToComponents(child);
                }
            }
        }
    }

    // Layout manager class for modular layouts
    private class LayoutManager {
        public void applyLayout(String layoutName, JPanel container) {
            container.removeAll();
            
            switch (layoutName) {
                case "grid":
                    container.setLayout(new GridLayout(0, 2, 10, 10));
                    break;
                case "flow":
                    container.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
                    break;
                case "border":
                    container.setLayout(new BorderLayout(10, 10));
                    break;
            }
            
            container.revalidate();
            container.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LibraryManagement().setVisible(true);
        });
    }
}