package ui;

import database.SessionManager;
import database.UserSession;
import model.MatchRecord;
import service.MatchService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog displaying an authenticated pilot's mission history from Supabase (public.matches).
 * Styled with the Phase 9 Design System.
 */
public class MatchHistoryDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final MainWindow mainWindow;
    private final MatchService matchService;

    private JLabel statusLabel;
    private JLabel pilotInfoLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private CyberButton refreshBtn;
    private JPanel guestNoticePanel;

    public MatchHistoryDialog(JFrame parent, MainWindow mainWindow, MatchService matchService) {
        super(parent, "STARFLEET FLIGHT ARCHIVES – MATCH HISTORY", true);
        this.mainWindow = mainWindow;
        this.matchService = matchService;

        setSize(600, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        buildUI();
        loadHistoryData();
    }

    private void buildUI() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_PANEL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("STARFLEET FLIGHT LOG ARCHIVE", SwingConstants.CENTER);
        titleLbl.setFont(Theme.HEADER_1);
        titleLbl.setForeground(Theme.CYAN_PRIMARY);
        headerPanel.add(titleLbl, BorderLayout.NORTH);

        pilotInfoLabel = new JLabel("PILOT: GUEST / OFFLINE", SwingConstants.CENTER);
        pilotInfoLabel.setFont(Theme.BODY_BOLD);
        pilotInfoLabel.setForeground(Theme.TEXT_MUTED);
        pilotInfoLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        headerPanel.add(pilotInfoLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Table & Guest Warning Container
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Theme.BG_DEEP);

        // Guest advisory panel
        guestNoticePanel = new JPanel(new GridBagLayout());
        guestNoticePanel.setBackground(Theme.BG_PANEL);
        guestNoticePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CYAN),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel guestWarning = new JLabel("GUEST FLIGHT LOGS ARE STORED LOCALLY ONLY");
        guestWarning.setFont(Theme.BODY_BOLD);
        guestWarning.setForeground(Theme.GOLD_ACCENT);
        guestNoticePanel.add(guestWarning, gbc);

        gbc.gridy = 1;
        CyberButton loginPromptBtn = CyberButton.hero("AUTHENTICATE PILOT TO SYNC LOGS");
        loginPromptBtn.setPreferredSize(new Dimension(280, 32));
        loginPromptBtn.addActionListener(e -> {
            dispose();
            if (mainWindow != null) mainWindow.showAuthDialog();
        });
        guestNoticePanel.add(loginPromptBtn, gbc);

        centerPanel.add(guestNoticePanel, BorderLayout.NORTH);

        // History Table
        String[] columnNames = {"DATE & TIME", "FINAL SCORE", "SECTOR", "DURATION"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setBackground(Theme.BG_INPUT);
        historyTable.setForeground(Color.WHITE);
        historyTable.setFont(Theme.BODY);
        historyTable.setRowHeight(30);
        historyTable.setGridColor(new Color(30, 42, 65));
        historyTable.getTableHeader().setBackground(new Color(25, 34, 58));
        historyTable.getTableHeader().setForeground(Theme.CYAN_PRIMARY);
        historyTable.getTableHeader().setFont(Theme.BODY_BOLD);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(Theme.BG_DEEP);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Theme.BG_PANEL);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        statusLabel = new JLabel("Status: Ready", SwingConstants.LEFT);
        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        footerPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        refreshBtn = CyberButton.pill("REFRESH", Theme.CYAN_PRIMARY, Theme.BG_INPUT, 90);
        refreshBtn.addActionListener(e -> loadHistoryData());
        btnPanel.add(refreshBtn);

        CyberButton closeBtn = CyberButton.danger("CLOSE");
        closeBtn.setPreferredSize(new Dimension(80, 26));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footerPanel.add(btnPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadHistoryData() {
        UserSession session = SessionManager.getInstance().getCurrentSession();
        if (!session.isAuthenticated()) {
            pilotInfoLabel.setText("PILOT: GUEST / OFFLINE");
            pilotInfoLabel.setForeground(Theme.TEXT_MUTED);
            guestNoticePanel.setVisible(true);
            tableModel.setRowCount(0);
            statusLabel.setText("Offline: Cloud logs require authenticated pilot clearance.");
            statusLabel.setForeground(Theme.GOLD_ACCENT);
            return;
        }

        pilotInfoLabel.setText("PILOT: " + session.getUsername().toUpperCase() + " (CLEARANCE: ACTIVE)");
        pilotInfoLabel.setForeground(Theme.EMERALD_STATUS);
        guestNoticePanel.setVisible(false);

        statusLabel.setText("Retrieving mission records from Starfleet Archives...");
        statusLabel.setForeground(Theme.CYAN_PRIMARY);
        refreshBtn.setEnabled(false);

        matchService.fetchMyMatchHistory().thenAccept(records -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                tableModel.setRowCount(0);

                if (records == null || records.isEmpty()) {
                    statusLabel.setText("No flight logs on record. Launch missions to build your record.");
                    statusLabel.setForeground(Theme.TEXT_MUTED);
                } else {
                    for (MatchRecord r : records) {
                        tableModel.addRow(new Object[]{
                                r.getFormattedDate(),
                                String.format("%,d", r.getScore()),
                                "Sector " + r.getLevelReached(),
                                r.getFormattedDuration()
                        });
                    }
                    statusLabel.setText("Retrieved " + records.size() + " flight mission log(s) successfully.");
                    statusLabel.setForeground(Theme.EMERALD_STATUS);
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                statusLabel.setText("Comm link failure: Unable to reach Starfleet Archives.");
                statusLabel.setForeground(Theme.CORAL_DANGER);
            });
            return null;
        });
    }
}
