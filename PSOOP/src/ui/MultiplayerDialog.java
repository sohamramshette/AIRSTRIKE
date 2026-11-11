package ui;

import database.SessionManager;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.PilotPresenceState;
import database.multiplayer.MultiplayerModels.RoomOperationResult;
import database.multiplayer.MultiplayerModels.RoomStatus;
import database.multiplayer.MultiplayerPresenceService;
import database.multiplayer.MultiplayerRoomService;
import database.multiplayer.MultiplayerValidation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * ==============================================================================
 * AIRSTRIKE Phase 12 (Part 4): Java Swing 1v1 Multiplayer Lobby Dialog
 * PACKAGE: ui
 *
 * Implements:
 * - Phase 9 Cyber-Military Dark Glassmorphic Design System.
 * - Sub-view navigation: Main Menu -> Join Code Entry -> Dual-Pilot Waiting Lobby.
 * - Non-blocking asynchronous network operations outside the Swing EDT.
 * - Thread-safe EDT UI updates via SwingUtilities.invokeLater.
 * ==============================================================================
 */
public class MultiplayerDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final MainWindow mainWindow;
    private final MultiplayerRoomService roomService;
    private final MultiplayerPresenceService presenceService;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    // Sub-view Card IDs
    private static final String VIEW_MENU = "VIEW_MENU";
    private static final String VIEW_JOIN = "VIEW_JOIN";
    private static final String VIEW_LOBBY = "VIEW_LOBBY";

    // Join View Components
    private JTextField joinCodeField;
    private JLabel joinErrorLabel;
    private CyberButton joinSubmitBtn;

    // Lobby View Components
    private JLabel lobbyRoomCodeLabel;
    private JButton copyCodeBtn;
    private JLabel hostNameLabel;
    private JLabel hostPresenceLabel;
    private JLabel hostReadyBadge;
    private JLabel guestNameLabel;
    private JLabel guestPresenceLabel;
    private JLabel guestReadyBadge;
    private JLabel lobbyStatusText;
    private CyberButton toggleReadyBtn;
    private CyberButton leaveLobbyBtn;

    // Local state tracking
    private MultiplayerRoomState activeRoom = null;

    public MultiplayerDialog(JFrame parent, MainWindow mainWindow) {
        super(parent, "STARFLEET 1v1 COMBAT LOBBY – MULTIPLAYER", true);
        this.mainWindow = mainWindow;
        this.roomService = MultiplayerRoomService.getInstance();
        this.presenceService = MultiplayerPresenceService.getInstance();

        setSize(600, 540);
        setMinimumSize(new Dimension(540, 480));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.contentPanel.setOpaque(false);

        buildHeader();
        buildSubviews();
        add(contentPanel, BorderLayout.CENTER);

        // Register room state and presence observers
        setupListeners();

        // Handle window closing gracefully
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleDialogClose();
            }
        });

        // Start at Menu sub-view
        cardLayout.show(contentPanel, VIEW_MENU);
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)
        ));

        JLabel title = new JLabel("ONLINE 1v1 COMBAT LOBBY", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_1);
        title.setForeground(Theme.PURPLE_ACCENT);
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Synchronized Realtime Score Attack • Private Starfleet Rooms", SwingConstants.CENTER);
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
    }

    private void buildSubviews() {
        contentPanel.add(buildMenuView(), VIEW_MENU);
        contentPanel.add(buildJoinView(), VIEW_JOIN);
        contentPanel.add(buildLobbyView(), VIEW_LOBBY);
    }

    // =========================================================================
    // Sub-View 1: Multiplayer Menu
    // =========================================================================
    private JPanel buildMenuView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Pilot identity capsule
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 24, 16, 24);
        JPanel pilotCapsule = createCapsuleCard();
        panel.add(pilotCapsule, gbc);

        // Button 1: Create Room
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 24, 8, 24);
        CyberButton createBtn = CyberButton.tactical("▶   CREATE PRIVATE ROOM", Theme.PURPLE_ACCENT, Theme.PURPLE_DARK);
        createBtn.addActionListener(e -> executeCreateRoom());
        panel.add(createBtn, gbc);

        // Button 2: Join Room
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 24, 8, 24);
        CyberButton joinBtn = CyberButton.tactical("◈   JOIN MISSION VIA ROOM CODE", Theme.BLUE_ACCENT, new Color(16, 32, 54));
        joinBtn.addActionListener(e -> {
            joinCodeField.setText("");
            joinErrorLabel.setVisible(false);
            cardLayout.show(contentPanel, VIEW_JOIN);
            joinCodeField.requestFocusInWindow();
        });
        panel.add(joinBtn, gbc);

        // Button 3: Return
        gbc.gridy = 3;
        gbc.insets = new Insets(14, 24, 10, 24);
        CyberButton returnBtn = CyberButton.tactical("✕   RETURN TO MAIN MENU", Theme.TEXT_MUTED, new Color(20, 28, 44));
        returnBtn.addActionListener(e -> dispose());
        panel.add(returnBtn, gbc);

        return panel;
    }

    private JPanel createCapsuleCard() {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQuality(g2);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.BORDER_CYAN);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        String username = SessionManager.getInstance().getCurrentUsername();
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();

        JLabel pilotLbl = new JLabel("PILOT IDENTITY: " + username.toUpperCase());
        pilotLbl.setFont(Theme.HEADER_2);
        pilotLbl.setForeground(loggedIn ? Theme.CYAN_PRIMARY : Theme.GOLD_ACCENT);

        JLabel statusLbl = new JLabel(loggedIn ? "● VERIFIED STARFLEET CADET" : "⚠ GUEST PILOT (LOGIN REQUIRED)");
        statusLbl.setFont(Theme.MONO_SMALL);
        statusLbl.setForeground(loggedIn ? Theme.EMERALD_STATUS : Theme.CORAL_DANGER);

        card.add(pilotLbl, BorderLayout.WEST);
        card.add(statusLbl, BorderLayout.EAST);
        return card;
    }

    // =========================================================================
    // Sub-View 2: Join Mission Screen
    // =========================================================================
    private JPanel buildJoinView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Card Container
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD_OPAQUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_CYAN, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel title = new JLabel("ENTER 6-CHARACTER ROOM CODE");
        title.setFont(Theme.HEADER_2);
        title.setForeground(Theme.BLUE_ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel hint = new JLabel("Obtain invitation code from hosting pilot and enter below:");
        hint.setFont(Theme.BODY);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));
        card.add(hint);

        // Room Code Input Field
        joinCodeField = new JTextField(8);
        joinCodeField.setMaximumSize(new Dimension(280, 44));
        joinCodeField.setPreferredSize(new Dimension(280, 44));
        joinCodeField.setBackground(Theme.BG_INPUT);
        joinCodeField.setForeground(Theme.GOLD_ACCENT);
        joinCodeField.setCaretColor(Theme.CYAN_PRIMARY);
        joinCodeField.setFont(new Font("JetBrains Mono", Font.BOLD, 22));
        joinCodeField.setHorizontalAlignment(JTextField.CENTER);
        joinCodeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CYAN_PRIMARY, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        joinCodeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(joinCodeField);

        // Error message label
        joinErrorLabel = new JLabel("Invalid Room Code");
        joinErrorLabel.setFont(Theme.BODY);
        joinErrorLabel.setForeground(Theme.CORAL_DANGER);
        joinErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinErrorLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        joinErrorLabel.setVisible(false);
        card.add(joinErrorLabel);

        gbc.gridy = 0;
        gbc.insets = new Insets(10, 24, 16, 24);
        panel.add(card, gbc);

        // Action Buttons Row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        CyberButton backBtn = CyberButton.tactical("⟵   BACK", Theme.TEXT_MUTED, new Color(20, 28, 44));
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, VIEW_MENU));
        btnRow.add(backBtn);

        joinSubmitBtn = CyberButton.hero("▶   JOIN ROOM");
        joinSubmitBtn.addActionListener(e -> executeJoinRoom());
        btnRow.add(joinSubmitBtn);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 24, 10, 24);
        panel.add(btnRow, gbc);

        return panel;
    }

    // =========================================================================
    // Sub-View 3: Dual-Player Lobby Screen
    // =========================================================================
    private JPanel buildLobbyView() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Room Code Banner
        JPanel codeBanner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQuality(g2);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.BORDER_CYAN);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        codeBanner.setOpaque(false);
        codeBanner.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel leftCode = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftCode.setOpaque(false);
        JLabel codeLbl = new JLabel("ROOM CODE:");
        codeLbl.setFont(Theme.BODY);
        codeLbl.setForeground(Theme.TEXT_MUTED);
        leftCode.add(codeLbl);

        lobbyRoomCodeLabel = new JLabel("------");
        lobbyRoomCodeLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 22));
        lobbyRoomCodeLabel.setForeground(Theme.GOLD_ACCENT);
        leftCode.add(lobbyRoomCodeLabel);
        codeBanner.add(leftCode, BorderLayout.WEST);

        copyCodeBtn = new JButton("COPY INVITATION");
        copyCodeBtn.setFont(Theme.MONO_SMALL);
        copyCodeBtn.setBackground(Theme.BG_INPUT);
        copyCodeBtn.setForeground(Theme.CYAN_PRIMARY);
        copyCodeBtn.setBorder(BorderFactory.createLineBorder(Theme.CYAN_PRIMARY, 1));
        copyCodeBtn.setFocusPainted(false);
        copyCodeBtn.addActionListener(e -> {
            String code = lobbyRoomCodeLabel.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
            copyCodeBtn.setText("COPIED!");
            SwingUtilities.invokeLater(() -> {
                javax.swing.Timer t = new javax.swing.Timer(1500, ev -> copyCodeBtn.setText("COPY INVITATION"));
                t.setRepeats(false);
                t.start();
            });
        });
        codeBanner.add(copyCodeBtn, BorderLayout.EAST);
        panel.add(codeBanner, BorderLayout.NORTH);

        // 2. Dual Pilot Cards (Center)
        JPanel centerCards = new JPanel(new GridLayout(1, 2, 14, 0));
        centerCards.setOpaque(false);

        // Host Slot Card
        JPanel hostCard = createSlotCard("HOST PILOT", Theme.CYAN_PRIMARY);
        hostNameLabel = new JLabel("Pilot 1");
        hostNameLabel.setFont(Theme.HEADER_2);
        hostNameLabel.setForeground(Theme.TEXT_WHITE);

        hostPresenceLabel = new JLabel("● ONLINE");
        hostPresenceLabel.setFont(Theme.MONO_SMALL);
        hostPresenceLabel.setForeground(Theme.EMERALD_STATUS);

        hostReadyBadge = createBadge("NOT READY", false);

        populateSlotCard(hostCard, hostNameLabel, hostPresenceLabel, hostReadyBadge);
        centerCards.add(hostCard);

        // Challenger Slot Card
        JPanel guestCard = createSlotCard("CHALLENGER PILOT", Theme.PURPLE_ACCENT);
        guestNameLabel = new JLabel("Waiting for pilot...");
        guestNameLabel.setFont(Theme.HEADER_2);
        guestNameLabel.setForeground(Theme.TEXT_MUTED);

        guestPresenceLabel = new JLabel("○ DISCONNECTED");
        guestPresenceLabel.setFont(Theme.MONO_SMALL);
        guestPresenceLabel.setForeground(Theme.TEXT_MUTED);

        guestReadyBadge = createBadge("NOT READY", false);

        populateSlotCard(guestCard, guestNameLabel, guestPresenceLabel, guestReadyBadge);
        centerCards.add(guestCard);

        panel.add(centerCards, BorderLayout.CENTER);

        // 3. Status Notice Banner & Action Buttons (South)
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        lobbyStatusText = new JLabel("Waiting for second cadet to enter room code...", SwingConstants.CENTER);
        lobbyStatusText.setFont(Theme.BODY);
        lobbyStatusText.setForeground(Theme.TEXT_MUTED);
        lobbyStatusText.setAlignmentX(Component.CENTER_ALIGNMENT);
        lobbyStatusText.setBorder(BorderFactory.createEmptyBorder(6, 0, 12, 0));
        southPanel.add(lobbyStatusText);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        leaveLobbyBtn = CyberButton.danger("✕   LEAVE ROOM");
        leaveLobbyBtn.addActionListener(e -> executeLeaveRoom());
        btnRow.add(leaveLobbyBtn);

        toggleReadyBtn = CyberButton.hero("✓   MARK READY");
        toggleReadyBtn.addActionListener(e -> executeToggleReady());
        btnRow.add(toggleReadyBtn);

        southPanel.add(btnRow);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSlotCard(String roleTitle, Color roleColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQuality(g2);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel(roleTitle);
        title.setFont(Theme.MONO_SMALL);
        title.setForeground(roleColor);
        card.add(title, BorderLayout.NORTH);
        return card;
    }

    private void populateSlotCard(JPanel card, JLabel nameLabel, JLabel presenceLabel, JLabel readyBadge) {
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(nameLabel, BorderLayout.NORTH);

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.add(presenceLabel, BorderLayout.WEST);
        statusRow.add(readyBadge, BorderLayout.EAST);
        center.add(statusRow, BorderLayout.SOUTH);

        card.add(center, BorderLayout.CENTER);
    }

    private JLabel createBadge(String text, boolean ready) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(Theme.MONO_SMALL);
        badge.setOpaque(true);
        updateBadgeState(badge, text, ready);
        return badge;
    }

    private void updateBadgeState(JLabel badge, String text, boolean ready) {
        badge.setText(" " + text + " ");
        if (ready) {
            badge.setBackground(Theme.EMERALD_DARK);
            badge.setForeground(Theme.EMERALD_STATUS);
            badge.setBorder(BorderFactory.createLineBorder(Theme.EMERALD_STATUS, 1));
        } else {
            badge.setBackground(Theme.BG_INPUT);
            badge.setForeground(Theme.TEXT_MUTED);
            badge.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        }
    }

    // =========================================================================
    // Asynchronous Actions & State Handlers
    // =========================================================================

    private void executeCreateRoom() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            mainWindow.showAuthDialog();
            return;
        }

        roomService.createRoom().thenAccept(res -> {
            SwingUtilities.invokeLater(() -> {
                if (res.isSuccess()) {
                    this.activeRoom = res.getData();
                    renderLobbyState(activeRoom);
                    cardLayout.show(contentPanel, VIEW_LOBBY);
                } else {
                    mainWindow.showAuthDialog();
                }
            });
        });
    }

    private void executeJoinRoom() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            mainWindow.showAuthDialog();
            return;
        }

        String rawCode = joinCodeField.getText();
        String sanitized = MultiplayerValidation.sanitizeRoomCode(rawCode);

        if (!MultiplayerValidation.isValidRoomCode(sanitized)) {
            joinErrorLabel.setText("Invalid code format. Must be 6 characters (e.g. A3B9X2)");
            joinErrorLabel.setVisible(true);
            return;
        }

        joinSubmitBtn.setEnabled(false);
        joinErrorLabel.setVisible(false);

        roomService.joinRoom(sanitized).thenAccept(res -> {
            SwingUtilities.invokeLater(() -> {
                joinSubmitBtn.setEnabled(true);
                if (res.isSuccess()) {
                    this.activeRoom = res.getData();
                    renderLobbyState(activeRoom);
                    cardLayout.show(contentPanel, VIEW_LOBBY);
                } else {
                    joinErrorLabel.setText(res.getError() != null ? res.getError() : "Failed to join room");
                    joinErrorLabel.setVisible(true);
                }
            });
        });
    }

    private void executeToggleReady() {
        if (activeRoom == null) return;
        toggleReadyBtn.setEnabled(false);

        roomService.setReady(activeRoom.getRoomId()).thenAccept(res -> {
            SwingUtilities.invokeLater(() -> {
                toggleReadyBtn.setEnabled(true);
                if (res.isSuccess()) {
                    this.activeRoom = res.getData();
                    renderLobbyState(activeRoom);
                }
            });
        });
    }

    private void executeLeaveRoom() {
        if (activeRoom != null) {
            roomService.leaveRoom(activeRoom.getRoomId());
        }
        activeRoom = null;
        cardLayout.show(contentPanel, VIEW_MENU);
    }

    private void handleDialogClose() {
        if (activeRoom != null) {
            roomService.leaveRoom(activeRoom.getRoomId());
            activeRoom = null;
        }
        roomService.disconnectPresence();
        dispose();
    }

    // =========================================================================
    // Reactive Room & Presence State Observers
    // =========================================================================

    private void setupListeners() {
        roomService.addRoomListener(room -> {
            SwingUtilities.invokeLater(() -> {
                this.activeRoom = room;
                if (room != null) {
                    renderLobbyState(room);
                } else {
                    cardLayout.show(contentPanel, VIEW_MENU);
                }
            });
        });

        presenceService.addPresenceListener(presences -> {
            SwingUtilities.invokeLater(() -> renderPresenceState(presences));
        });
    }

    private void renderLobbyState(MultiplayerRoomState room) {
        if (room == null) return;

        lobbyRoomCodeLabel.setText(room.getRoomCode());
        hostNameLabel.setText(room.getHostUsername());
        updateBadgeState(hostReadyBadge, room.isHostReady() ? "READY" : "NOT READY", room.isHostReady());

        if (room.hasGuest()) {
            guestNameLabel.setText(room.getGuestUsername());
            guestNameLabel.setForeground(Theme.TEXT_WHITE);
            updateBadgeState(guestReadyBadge, room.isGuestReady() ? "READY" : "NOT READY", room.isGuestReady());
        } else {
            guestNameLabel.setText("Waiting for pilot...");
            guestNameLabel.setForeground(Theme.TEXT_MUTED);
            updateBadgeState(guestReadyBadge, "NOT READY", false);
        }

        // Status banner messaging
        if (room.getStatus() == RoomStatus.COUNTDOWN) {
            lobbyStatusText.setText("⚔ COMBAT IMMINENT! BOTH PILOTS READY — COUNTDOWN ENGAGED!");
            lobbyStatusText.setForeground(Theme.GOLD_ACCENT);
            toggleReadyBtn.setEnabled(false);
            toggleReadyBtn.setText("✓   COUNTDOWN ACTIVE");
        } else if (room.isBothReady()) {
            lobbyStatusText.setText("⚔ BOTH PILOTS READY! PREPARING COMBAT...");
            lobbyStatusText.setForeground(Theme.EMERALD_STATUS);
        } else if (room.hasGuest()) {
            lobbyStatusText.setText("Both cadets connected. Mark READY when prepared for combat.");
            lobbyStatusText.setForeground(Theme.CYAN_PRIMARY);
            boolean myReady = room.isHost() ? room.isHostReady() : room.isGuestReady();
            toggleReadyBtn.setText(myReady ? "✓   READY CONFIRMED" : "✓   MARK READY");
            toggleReadyBtn.setEnabled(!myReady);
        } else {
            lobbyStatusText.setText("Waiting for second cadet to enter room code...");
            lobbyStatusText.setForeground(Theme.TEXT_MUTED);
            toggleReadyBtn.setText("✓   MARK READY");
            toggleReadyBtn.setEnabled(false);
        }
    }

    private void renderPresenceState(Map<String, PilotPresenceState> presences) {
        if (activeRoom == null || presences == null) return;

        // Host presence
        PilotPresenceState hostPres = presences.get(activeRoom.getHostId());
        if (hostPres != null && hostPres.isOnline()) {
            hostPresenceLabel.setText("● ONLINE");
            hostPresenceLabel.setForeground(Theme.EMERALD_STATUS);
        } else {
            hostPresenceLabel.setText("○ DISCONNECTED");
            hostPresenceLabel.setForeground(Theme.TEXT_MUTED);
        }

        // Guest presence
        if (activeRoom.getGuestId() != null) {
            PilotPresenceState guestPres = presences.get(activeRoom.getGuestId());
            if (guestPres != null && guestPres.isOnline()) {
                guestPresenceLabel.setText("● ONLINE");
                guestPresenceLabel.setForeground(Theme.EMERALD_STATUS);
            } else {
                guestPresenceLabel.setText("○ DISCONNECTED");
                guestPresenceLabel.setForeground(Theme.TEXT_MUTED);
            }
        } else {
            guestPresenceLabel.setText("○ AWAITING GUEST");
            guestPresenceLabel.setForeground(Theme.TEXT_MUTED);
        }
    }
}
