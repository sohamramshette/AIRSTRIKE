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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
 * Modern, futuristic Starfleet Authentication & Pilot Registry Dialog.
 * Implements Phase 9 dark glassmorphism, responsive status feedback, and guest entry.
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
    private CyberButton loginSubmitBtn;

    // Register Form Fields
    private JTextField regCallsignField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private CyberButton regSubmitBtn;

    // Status Banner
    private JLabel statusBanner;

    public AuthDialog(JFrame parent, AuthService authService) {
        this(parent, authService, "LOGIN");
    }

    public AuthDialog(JFrame parent, AuthService authService, String initialTab) {
        super(parent, "STARFLEET REGISTRY – PILOT AUTHENTICATION", true);
        this.authService = authService;

        setSize(480, 570);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        // Header Panel with Tabs
        JPanel headerPanel = buildHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Card Container for Login & Register Forms
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(Theme.BG_DEEP);

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
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(16, 24, 12, 24)
        ));

        JLabel title = new JLabel("STARFLEET COMMAND ACCESS", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_1);
        title.setForeground(Theme.CYAN_PRIMARY);
        header.add(title, BorderLayout.NORTH);

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        tabLoginBtn = createTabButton("PILOT LOGIN");
        tabLoginBtn.addActionListener(e -> selectTab("LOGIN"));
        tabs.add(tabLoginBtn);

        tabRegisterBtn = createTabButton("NEW REGISTRATION");
        tabRegisterBtn.addActionListener(e -> selectTab("REGISTER"));
        tabs.add(tabRegisterBtn);

        header.add(tabs, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_DEEP);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 32, 10, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 4, 0);

        // Email Label & Field
        JLabel emailLbl = createFieldLabel("PILOT COMM / ACADEMY EMAIL");
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
        loginSubmitBtn = CyberButton.hero("AUTHENTICATE PILOT");
        loginSubmitBtn.setPreferredSize(new Dimension(380, 40));
        loginSubmitBtn.addActionListener(e -> executeLogin());
        panel.add(loginSubmitBtn, gbc);

        // Registration Navigation link
        JButton toRegBtn = createLinkButton("Need starfleet clearance? Register as new pilot ->");
        toRegBtn.addActionListener(e -> selectTab("REGISTER"));
        gbc.insets = new Insets(12, 0, 0, 0);
        panel.add(toRegBtn, gbc);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_DEEP);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 32));

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
        JLabel emailLbl = createFieldLabel("PILOT COMM / ACADEMY EMAIL");
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
        regSubmitBtn = CyberButton.hero("REGISTER NEW PILOT");
        regSubmitBtn.setPreferredSize(new Dimension(380, 40));
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
        footer.setBackground(Theme.BG_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 24, 12, 24)
        ));

        statusBanner = new JLabel("Status: Ready for authentication.", SwingConstants.CENTER);
        statusBanner.setFont(Theme.BODY);
        statusBanner.setForeground(Theme.TEXT_MUTED);
        statusBanner.setPreferredSize(new Dimension(420, 22));
        footer.add(statusBanner, BorderLayout.NORTH);

        CyberButton guestBtn = CyberButton.tactical(
                "CONTINUE AS GUEST / OFFLINE PILOT",
                Theme.TEXT_MUTED,
                Theme.BG_PANEL
        );
        guestBtn.addActionListener(e -> {
            SessionManager.getInstance().clearSession();
            dispose();
        });

        JPanel guestWrap = new JPanel(new BorderLayout());
        guestWrap.setOpaque(false);
        guestWrap.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        guestWrap.add(guestBtn, BorderLayout.CENTER);

        footer.add(guestWrap, BorderLayout.SOUTH);
        return footer;
    }

    public void selectTab(String tabName) {
        cardLayout.show(cardsPanel, tabName);
        if ("LOGIN".equals(tabName)) {
            tabLoginBtn.setBackground(new Color(0, 229, 255, 40));
            tabLoginBtn.setForeground(Theme.CYAN_PRIMARY);
            tabLoginBtn.setBorder(BorderFactory.createLineBorder(Theme.CYAN_PRIMARY, 1));

            tabRegisterBtn.setBackground(Theme.BG_INPUT);
            tabRegisterBtn.setForeground(Theme.TEXT_MUTED);
            tabRegisterBtn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
            loginEmailField.requestFocusInWindow();
        } else {
            tabRegisterBtn.setBackground(new Color(52, 211, 153, 40));
            tabRegisterBtn.setForeground(Theme.EMERALD_STATUS);
            tabRegisterBtn.setBorder(BorderFactory.createLineBorder(Theme.EMERALD_STATUS, 1));

            tabLoginBtn.setBackground(Theme.BG_INPUT);
            tabLoginBtn.setForeground(Theme.TEXT_MUTED);
            tabLoginBtn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
            regCallsignField.requestFocusInWindow();
        }
        clearStatus();
    }

    private void executeLogin() {
        String email = loginEmailField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            setStatus("Please provide email and clearance password.", Theme.CORAL_DANGER);
            return;
        }

        setBusy(true, "Transmitting clearance to Starfleet Command...");

        authService.login(email, pass).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                setBusy(false, null);
                if (result.isSuccess()) {
                    setStatus("Access Granted! Welcome, Commander " + result.getSession().getUsername(), Theme.EMERALD_STATUS);
                    new javax.swing.Timer(700, e -> dispose()).start();
                } else if (result.isConfirmationPending()) {
                    setStatus("Pending: Please verify email before logging in.", Theme.GOLD_ACCENT);
                } else {
                    setStatus(result.getMessage(), Theme.CORAL_DANGER);
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
            setStatus("Please provide email, password, and confirm password.", Theme.CORAL_DANGER);
            return;
        }

        if (!pass.equals(confirmPass)) {
            setStatus("Passwords do not match.", Theme.CORAL_DANGER);
            return;
        }

        if (pass.length() < 6) {
            setStatus("Password must contain at least 6 characters.", Theme.CORAL_DANGER);
            return;
        }

        setBusy(true, "Transmitting pilot registry application...");

        authService.register(email, pass, callsign).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                setBusy(false, null);
                if (result.isSuccess()) {
                    setStatus("Registry Accepted! Welcome, Pilot " + result.getSession().getUsername(), Theme.EMERALD_STATUS);
                    new javax.swing.Timer(700, e -> dispose()).start();
                } else if (result.isConfirmationPending()) {
                    setStatus("Application accepted! Check your inbox to confirm.", Theme.EMERALD_STATUS);
                } else {
                    setStatus(result.getMessage(), Theme.CORAL_DANGER);
                }
            });
        });
    }

    private void setBusy(boolean busy, String message) {
        loginSubmitBtn.setEnabled(!busy);
        regSubmitBtn.setEnabled(!busy);
        if (busy) {
            setStatus(message, Theme.CYAN_PRIMARY);
        }
    }

    private void setStatus(String message, Color color) {
        statusBanner.setText(message);
        statusBanner.setForeground(color);
    }

    private void clearStatus() {
        statusBanner.setText("Status: Ready.");
        statusBanner.setForeground(Theme.TEXT_MUTED);
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.BODY_BOLD);
        lbl.setForeground(Theme.BLUE_ACCENT);
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Theme.CYAN_PRIMARY);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(380, 36));
        field.setBorder(Theme.createFieldBorder());
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Theme.CYAN_PRIMARY);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(380, 36));
        field.setBorder(Theme.createFieldBorder());
        return field;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BODY);
        btn.setForeground(Theme.BLUE_ACCENT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }
}
