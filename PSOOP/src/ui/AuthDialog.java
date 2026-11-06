package ui;

import database.SessionManager;
import service.AuthResult;
import service.AuthService;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Authentication dialog for AIRSTRIKE pilots.
 *
 * Provides:
 * - Tabbed and cross-linked pilot login and registration interfaces.
 * - Registration form with Email, Password, and Confirm Password verification.
 * - Asynchronous background authentication without freezing Swing EDT.
 * - Dynamic feedback banner for authentication status, errors, and email confirmation.
 * - Direct "Play Offline (Guest)" entry to ensure uninterrupted space combat.
 */
public class AuthDialog extends JDialog {

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    private JButton tabLoginBtn;
    private JButton tabRegisterBtn;

    // Login Form Fields
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JButton loginSubmitBtn;

    // Register Form Fields
    private JTextField regCallsignField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JButton regSubmitBtn;

    // Status Banner
    private JLabel statusBanner;

    public AuthDialog(JFrame parent, AuthService authService) {
        this(parent, authService, "LOGIN");
    }

    public AuthDialog(JFrame parent, AuthService authService, String initialTab) {
        super(parent, "STARFLEET REGISTRY – PILOT AUTHENTICATION", true);
        this.authService = authService;

        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(13, 17, 30));
        setLayout(new BorderLayout());

        // Header Panel with Tabs
        JPanel headerPanel = buildHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Card Container for Login & Register Forms
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(new Color(13, 17, 30));

        cardsPanel.add(buildLoginPanel(), "LOGIN");
        cardsPanel.add(buildRegisterPanel(), "REGISTER");
        add(cardsPanel, BorderLayout.CENTER);

        // Footer with Status Banner and Guest Entry
        JPanel footerPanel = buildFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        selectTab(initialTab != null ? initialTab : "LOGIN");
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 13, 24));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 42, 68)),
                BorderFactory.createEmptyBorder(16, 20, 12, 20)
        ));

        JLabel title = new JLabel("STARFLEET COMMAND ACCESS", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(new Color(0, 210, 255));
        header.add(title, BorderLayout.NORTH);

        JPanel tabs = new JPanel(new GridBagLayout());
        tabs.setBackground(new Color(10, 13, 24));
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 4, 0, 4);

        tabLoginBtn = createTabButton("PILOT LOGIN");
        tabLoginBtn.addActionListener(e -> selectTab("LOGIN"));
        tabs.add(tabLoginBtn, gbc);

        tabRegisterBtn = createTabButton("NEW REGISTRATION");
        tabRegisterBtn.addActionListener(e -> selectTab("REGISTER"));
        tabs.add(tabRegisterBtn, gbc);

        header.add(tabs, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(13, 17, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 4, 0);

        // Email Label & Field
        JLabel emailLbl = createFieldLabel("PILOT COMM / EMAIL");
        panel.add(emailLbl, gbc);

        loginEmailField = createStyledTextField();
        loginEmailField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) loginPasswordField.requestFocusInWindow();
            }
        });
        gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(loginEmailField, gbc);

        // Password Label & Field
        JLabel passLbl = createFieldLabel("SECURITY CLEARANCE PASSWORD");
        gbc.insets = new Insets(4, 0, 4, 0);
        panel.add(passLbl, gbc);

        loginPasswordField = createStyledPasswordField();
        loginPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) executeLogin();
            }
        });
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(loginPasswordField, gbc);

        // Submit Button
        loginSubmitBtn = createActionButton("AUTHENTICATE PILOT", new Color(0, 180, 255));
        loginSubmitBtn.addActionListener(e -> executeLogin());
        panel.add(loginSubmitBtn, gbc);

        // Registration Navigation link
        JButton toRegBtn = createLinkButton("Need an account? Enlist as a new pilot ->");
        toRegBtn.addActionListener(e -> selectTab("REGISTER"));
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(toRegBtn, gbc);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(13, 17, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 0, 2, 0);

        // Callsign Field
        JLabel callsignLbl = createFieldLabel("CALLSIGN / DISPLAY NAME");
        panel.add(callsignLbl, gbc);

        regCallsignField = createStyledTextField();
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(regCallsignField, gbc);

        // Email Field
        JLabel emailLbl = createFieldLabel("PILOT COMM / EMAIL");
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(emailLbl, gbc);

        regEmailField = createStyledTextField();
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(regEmailField, gbc);

        // Password Field
        JLabel passLbl = createFieldLabel("SECURITY PASSWORD (MIN 6 CHARACTERS)");
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(passLbl, gbc);

        regPasswordField = createStyledPasswordField();
        regPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) regConfirmPasswordField.requestFocusInWindow();
            }
        });
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(regPasswordField, gbc);

        // Confirm Password Field
        JLabel confirmPassLbl = createFieldLabel("CONFIRM SECURITY PASSWORD");
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(confirmPassLbl, gbc);

        regConfirmPasswordField = createStyledPasswordField();
        regConfirmPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) executeRegister();
            }
        });
        gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(regConfirmPasswordField, gbc);

        // Submit Button
        regSubmitBtn = createActionButton("REGISTER NEW PILOT", new Color(60, 220, 120));
        regSubmitBtn.addActionListener(e -> executeRegister());
        panel.add(regSubmitBtn, gbc);

        // Login Navigation link
        JButton toLoginBtn = createLinkButton("Already have clearance? Pilot Login ->");
        toLoginBtn.addActionListener(e -> selectTab("LOGIN"));
        gbc.insets = new Insets(8, 0, 0, 0);
        panel.add(toLoginBtn, gbc);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(10, 13, 24));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 42, 68)),
                BorderFactory.createEmptyBorder(10, 20, 12, 20)
        ));

        statusBanner = new JLabel("Status: Ready for authentication.", SwingConstants.CENTER);
        statusBanner.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBanner.setForeground(new Color(160, 180, 210));
        statusBanner.setPreferredSize(new Dimension(420, 22));
        footer.add(statusBanner, BorderLayout.NORTH);

        JButton guestBtn = new JButton("CONTINUE AS GUEST / OFFLINE PILOT");
        guestBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        guestBtn.setForeground(new Color(140, 160, 190));
        guestBtn.setBackground(new Color(20, 26, 42));
        guestBtn.setFocusPainted(false);
        guestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        guestBtn.addActionListener(e -> {
            SessionManager.getInstance().clearSession();
            dispose();
        });

        JPanel guestWrap = new JPanel(new BorderLayout());
        guestWrap.setBackground(new Color(10, 13, 24));
        guestWrap.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        guestWrap.add(guestBtn, BorderLayout.CENTER);

        footer.add(guestWrap, BorderLayout.SOUTH);
        return footer;
    }

    public void selectTab(String tabName) {
        cardLayout.show(cardsPanel, tabName);
        if ("LOGIN".equals(tabName)) {
            tabLoginBtn.setBackground(new Color(0, 180, 255));
            tabLoginBtn.setForeground(Color.BLACK);
            tabRegisterBtn.setBackground(new Color(22, 29, 48));
            tabRegisterBtn.setForeground(Color.LIGHT_GRAY);
            loginEmailField.requestFocusInWindow();
        } else {
            tabRegisterBtn.setBackground(new Color(60, 220, 120));
            tabRegisterBtn.setForeground(Color.BLACK);
            tabLoginBtn.setBackground(new Color(22, 29, 48));
            tabLoginBtn.setForeground(Color.LIGHT_GRAY);
            regCallsignField.requestFocusInWindow();
        }
        clearStatus();
    }

    private void executeLogin() {
        String email = loginEmailField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            setStatus("Please provide email and clearance password.", new Color(255, 90, 90));
            return;
        }

        setBusy(true, "Transmitting clearance to Starfleet Command...");

        authService.login(email, pass).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                setBusy(false, null);
                if (result.isSuccess()) {
                    setStatus("Access Granted! Welcome, Commander " + result.getSession().getUsername(), new Color(80, 250, 120));
                    new javax.swing.Timer(700, e -> dispose()).start();
                } else if (result.isConfirmationPending()) {
                    setStatus("Pending: Please verify email before logging in.", new Color(255, 190, 50));
                } else {
                    setStatus(result.getMessage(), new Color(255, 90, 90));
                }
            });
        });
    }

    private void executeRegister() {
        String callsign = regCallsignField.getText().trim();
        String email = regEmailField.getText().trim();
        String pass = new String(regPasswordField.getPassword());
        String confirmPass = new String(regConfirmPasswordField.getPassword());

        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            setStatus("Please provide email, password, and confirm password.", new Color(255, 90, 90));
            return;
        }

        if (!pass.equals(confirmPass)) {
            setStatus("Passwords do not match.", new Color(255, 90, 90));
            return;
        }

        if (pass.length() < 6) {
            setStatus("Password must contain at least 6 characters.", new Color(255, 90, 90));
            return;
        }

        setBusy(true, "Transmitting pilot registry application...");

        authService.register(email, pass, callsign).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                setBusy(false, null);
                if (result.isSuccess()) {
                    setStatus("Registry Accepted! Welcome, Pilot " + result.getSession().getUsername(), new Color(80, 250, 120));
                    new javax.swing.Timer(700, e -> dispose()).start();
                } else if (result.isConfirmationPending()) {
                    setStatus("Application accepted! Check your inbox to confirm.", new Color(80, 250, 120));
                } else {
                    setStatus(result.getMessage(), new Color(255, 90, 90));
                }
            });
        });
    }

    private void setBusy(boolean busy, String message) {
        loginSubmitBtn.setEnabled(!busy);
        regSubmitBtn.setEnabled(!busy);
        if (busy) {
            setStatus(message, new Color(0, 210, 255));
        }
    }

    private void setStatus(String message, Color color) {
        statusBanner.setText(message);
        statusBanner.setForeground(color);
    }

    private void clearStatus() {
        statusBanner.setText("Status: Ready.");
        statusBanner.setForeground(new Color(160, 180, 210));
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(new Color(140, 170, 210));
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(new Color(22, 29, 48));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(0, 210, 255));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(380, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 56, 90), 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(new Color(22, 29, 48));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(0, 210, 255));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(380, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 56, 90), 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        return field;
    }

    private JButton createActionButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(accentColor.darker());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(380, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return btn;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setForeground(new Color(120, 180, 240));
        btn.setBackground(new Color(13, 17, 30));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }
}
