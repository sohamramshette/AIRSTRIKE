package ui;

import database.SessionManager;
import database.UserSession;
import game.GameEngine;
import service.AuthService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Unified Pilot Profile & Mission Settings Center for AIRSTRIKE.
 * Combines authenticated pilot credentials, flight records, and tactical simulation parameters.
 */
public class ProfileSettingsDialog extends JDialog {

    private final MainWindow mainWindow;
    private final GameEngine engine;
    private final AuthService authService;

    private JComboBox<String> difficultyBox;
    private JCheckBox sfxCheckBox;
    private JCheckBox musicCheckBox;
    private JCheckBox announcerCheckBox;
    private JComboBox<String> packBox;
    private JComboBox<String> frequencyBox;
    private JCheckBox voiceCheckBox;

    public ProfileSettingsDialog(JFrame parent, MainWindow mainWindow, GameEngine engine, AuthService authService) {
        super(parent, "STARFLEET COMMAND – PILOT PROFILE & SYSTEM SETTINGS", true);
        this.mainWindow = mainWindow;
        this.engine = engine;
        this.authService = authService;

        setSize(620, 660);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildBodyPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel title = new JLabel("PILOT DOSSIER & MISSION SETTINGS", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_1);
        title.setForeground(Theme.CYAN_PRIMARY);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Tactical Security Clearance & Combat Simulator Preferences", SwingConstants.CENTER);
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        header.add(sub, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG_DEEP);
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // 1. Pilot Profile Card
        body.add(buildProfileCard());
        body.add(Box.createVerticalStrut(14));

        // 2. Mission Settings Card
        body.add(buildSettingsCard());

        return body;
    }

    private JPanel buildProfileCard() {
        CyberCard card = new CyberCard(new BorderLayout(10, 10), Theme.BLUE_ACCENT, Theme.RADIUS_CARD, true);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("STARFLEET PILOT DOSSIER");
        title.setFont(Theme.HEADER_2);
        title.setForeground(Theme.BLUE_ACCENT);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(4, 2, 8, 6));
        details.setOpaque(false);

        UserSession session = SessionManager.getInstance().getCurrentSession();
        boolean isAuth = session.isAuthenticated();

        // Callsign
        details.add(createLabel("PILOT CALLSIGN:", Theme.TEXT_MUTED));
        String callsign = isAuth ? session.getUsername().toUpperCase() : "GUEST PILOT (OFFLINE)";
        Color callsignColor = isAuth ? Theme.EMERALD_STATUS : Theme.BLUE_ACCENT;
        details.add(createLabel(callsign, callsignColor));

        // Email / Comm Link
        details.add(createLabel("COMM CHANNEL / EMAIL:", Theme.TEXT_MUTED));
        String email = isAuth && session.getEmail() != null ? session.getEmail() : "LOCAL ENCLAVE ONLY";
        details.add(createLabel(email, Theme.TEXT_WHITE));

        // Clearance Status
        details.add(createLabel("SECURITY CLEARANCE:", Theme.TEXT_MUTED));
        String status = isAuth ? "COMMANDER (SUPABASE VERIFIED)" : "UNREGISTERED CADET";
        Color statusColor = isAuth ? Theme.EMERALD_STATUS : Theme.GOLD_ACCENT;
        details.add(createLabel(status, statusColor));

        // All-Time Record
        details.add(createLabel("ALL-TIME COMBAT RECORD:", Theme.TEXT_MUTED));
        int high = engine != null ? engine.getScoreCalculator().getHighScore() : 0;
        details.add(createLabel(String.format("%,d PTS", high), Theme.GOLD_ACCENT));

        card.add(details, BorderLayout.CENTER);

        // Auth action button (Logout or Login)
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.setOpaque(false);

        if (isAuth) {
            CyberButton logoutBtn = CyberButton.danger("LOGOUT PILOT");
            logoutBtn.setPreferredSize(new Dimension(130, 26));
            logoutBtn.addActionListener(e -> {
                if (authService != null) authService.logout();
                dispose();
                if (mainWindow != null) mainWindow.showAuthDialog("LOGIN");
            });
            btnBar.add(logoutBtn);
        } else {
            CyberButton loginBtn = CyberButton.pill("AUTHENTICATE PILOT", Theme.CYAN_PRIMARY, Theme.BG_PANEL, 140);
            loginBtn.addActionListener(e -> {
                dispose();
                if (mainWindow != null) mainWindow.showAuthDialog("LOGIN");
            });
            btnBar.add(loginBtn);
        }
        card.add(btnBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildSettingsCard() {
        CyberCard card = new CyberCard(new BorderLayout(10, 10), Theme.CYAN_PRIMARY, Theme.RADIUS_CARD, true);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("SIMULATION & HARDWARE PREFERENCES");
        title.setFont(Theme.HEADER_2);
        title.setForeground(Theme.CYAN_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Difficulty Setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        form.add(createLabel("Threat Level:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        String[] options = {
                "Cadet (Standard Shield & 100 HP)",
                "Veteran (Faster Waves & 85 HP)",
                "Ace Commander (Elite Squadrons & 70 HP)"
        };
        difficultyBox = new JComboBox<>(options);
        difficultyBox.setBackground(Theme.BG_INPUT);
        difficultyBox.setForeground(Theme.TEXT_WHITE);
        difficultyBox.setFont(Theme.BODY);
        form.add(difficultyBox, gbc);

        // Audio & FX Toggles
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.4;
        form.add(createLabel("Audio Systems:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        audioPanel.setOpaque(false);
        sfxCheckBox = new JCheckBox("Tactical SFX", true);
        sfxCheckBox.setOpaque(false);
        sfxCheckBox.setForeground(Theme.TEXT_WHITE);
        sfxCheckBox.setFont(Theme.BODY);
        audioPanel.add(sfxCheckBox);

        musicCheckBox = new JCheckBox("Starfleet Comm", true);
        musicCheckBox.setOpaque(false);
        musicCheckBox.setForeground(Theme.TEXT_WHITE);
        musicCheckBox.setFont(Theme.BODY);
        audioPanel.add(musicCheckBox);
        form.add(audioPanel, gbc);

        // Graphics Pipeline Info
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.4;
        form.add(createLabel("Renderer Pipeline:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JLabel renderVal = new JLabel("Hardware 2D Double-Buffered EDT");
        renderVal.setForeground(Theme.EMERALD_STATUS);
        renderVal.setFont(Theme.BODY_BOLD);
        form.add(renderVal, gbc);

        // --- Phase 10: Sarcastic Desi Toxic Announcer AI ---
        announcer.AnnouncerSettings annSettings = announcer.AnnouncerSettings.getInstance();

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.4;
        form.add(createLabel("Desi Toxic Announcer:", Theme.GOLD_ACCENT), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        announcerCheckBox = new JCheckBox("Active Hinglish Commentary", annSettings.isEnabled());
        announcerCheckBox.setOpaque(false);
        announcerCheckBox.setForeground(Theme.TEXT_WHITE);
        announcerCheckBox.setFont(Theme.BODY_BOLD);
        form.add(announcerCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.4;
        form.add(createLabel("Dialogue Meme Pack:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        String[] packOptions = {
            "All Packs (Ultimate Hinglish Chaos)",
            "Desi Viral Memes",
            "Instagram Reels & Gen-Z Slang",
            "Bollywood Classics & Comedy",
            "Toxic Pilot Roasts & Sarcasm",
            "Exaggerated Victory & Praise",
            "Bizarre Space Interruptions"
        };
        packBox = new JComboBox<>(packOptions);
        packBox.setBackground(Theme.BG_INPUT);
        packBox.setForeground(Theme.TEXT_WHITE);
        packBox.setFont(Theme.BODY);
        packBox.setSelectedIndex(switch (annSettings.getActiveCategory()) {
            case DESI_MEME -> 1;
            case INSTAGRAM_GENZ -> 2;
            case BOLLYWOOD -> 3;
            case GENERAL_ROAST -> 4;
            case VICTORY_PRAISE -> 5;
            case RANDOM_INTERRUPT -> 6;
            default -> 0;
        });
        form.add(packBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.4;
        form.add(createLabel("Commentary Frequency:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        String[] freqOptions = {
            "Normal (Balanced ~10-18s)",
            "High (Non-stop Desi Banter ~5-10s)",
            "Low (Relaxed ~20-30s)"
        };
        frequencyBox = new JComboBox<>(freqOptions);
        frequencyBox.setBackground(Theme.BG_INPUT);
        frequencyBox.setForeground(Theme.TEXT_WHITE);
        frequencyBox.setFont(Theme.BODY);
        frequencyBox.setSelectedIndex(switch (annSettings.getFrequency()) {
            case HIGH -> 1;
            case LOW -> 2;
            default -> 0;
        });
        form.add(frequencyBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.4;
        form.add(createLabel("Voiceover & Preview:", Theme.TEXT_WHITE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JPanel voicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        voicePanel.setOpaque(false);
        voiceCheckBox = new JCheckBox("Speech Voiceover (TTS)", annSettings.isVoiceEnabled());
        voiceCheckBox.setOpaque(false);
        voiceCheckBox.setForeground(Theme.TEXT_WHITE);
        voiceCheckBox.setFont(Theme.BODY);
        voicePanel.add(voiceCheckBox);

        CyberButton testBtn = CyberButton.tactical("TEST BANTER", Theme.CYAN_PRIMARY, Theme.BG_CARD);
        testBtn.setPreferredSize(new Dimension(110, 26));
        testBtn.setFont(Theme.MONO_SMALL);
        testBtn.addActionListener(e -> {
            announcer.AnnouncerManager.getInstance().testAnnounce();
        });
        voicePanel.add(testBtn);
        form.add(voicePanel, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JLabel createLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.BODY_BOLD);
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Theme.BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE));

        CyberButton saveBtn = CyberButton.hero("SAVE & CONFIRM");
        saveBtn.setPreferredSize(new Dimension(170, 36));
        saveBtn.addActionListener(e -> applyAndClose());
        footer.add(saveBtn);

        CyberButton closeBtn = CyberButton.danger("CLOSE");
        closeBtn.setPreferredSize(new Dimension(90, 36));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        return footer;
    }

    private void applyAndClose() {
        int idx = difficultyBox.getSelectedIndex();
        if (engine != null && engine.getPlayer() != null) {
            switch (idx) {
                case 0 -> engine.getPlayer().setHealth(100);
                case 1 -> engine.getPlayer().setHealth(85);
                case 2 -> engine.getPlayer().setHealth(70);
            }
        }

        // Apply Announcer Settings
        announcer.AnnouncerSettings annSettings = announcer.AnnouncerSettings.getInstance();
        annSettings.setEnabled(announcerCheckBox.isSelected());

        int packIdx = packBox.getSelectedIndex();
        announcer.DialogueCategory cat = switch (packIdx) {
            case 1 -> announcer.DialogueCategory.DESI_MEME;
            case 2 -> announcer.DialogueCategory.INSTAGRAM_GENZ;
            case 3 -> announcer.DialogueCategory.BOLLYWOOD;
            case 4 -> announcer.DialogueCategory.GENERAL_ROAST;
            case 5 -> announcer.DialogueCategory.VICTORY_PRAISE;
            case 6 -> announcer.DialogueCategory.RANDOM_INTERRUPT;
            default -> announcer.DialogueCategory.ALL_PACKS;
        };
        annSettings.setActiveCategory(cat);

        int freqIdx = frequencyBox.getSelectedIndex();
        announcer.CommentaryFrequency freq = switch (freqIdx) {
            case 1 -> announcer.CommentaryFrequency.HIGH;
            case 2 -> announcer.CommentaryFrequency.LOW;
            default -> announcer.CommentaryFrequency.NORMAL;
        };
        annSettings.setFrequency(freq);

        annSettings.setVoiceEnabled(voiceCheckBox.isSelected());

        dispose();
    }
}
