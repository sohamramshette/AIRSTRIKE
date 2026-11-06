package ui;

import database.SessionManager;
import database.UserSession;
import model.MatchRecord;
import service.MatchService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
 *
 * Implements:
 * - Asynchronous PostgREST data retrieval off the Swing EDT.
 * - Loading, empty, and error feedback states.
 * - Clear guest / offline advisory prompting pilot authentication.
 */
public class MatchHistoryDialog extends JDialog {

    private final MainWindow mainWindow;
    private final MatchService matchService;

    private JLabel statusLabel;
    private JLabel pilotInfoLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn;
    private JPanel guestNoticePanel;

    public MatchHistoryDialog(JFrame parent, MainWindow mainWindow, MatchService matchService) {
        super(parent, "STARFLEET FLIGHT ARCHIVES – MATCH HISTORY", true);
        this.mainWindow = mainWindow;
        this.matchService = matchService;

        setSize(580, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(13, 17, 30));
        setLayout(new BorderLayout());

        buildUI();
        loadHistoryData();
    }

    private void buildUI() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(10, 13, 24));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 45, 75)),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("STARFLEET FLIGHT LOG ARCHIVE", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLbl.setForeground(new Color(0, 210, 255));
        headerPanel.add(titleLbl, BorderLayout.NORTH);

        pilotInfoLabel = new JLabel("PILOT: GUEST / OFFLINE", SwingConstants.CENTER);
        pilotInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        pilotInfoLabel.setForeground(new Color(150, 180, 220));
        pilotInfoLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        headerPanel.add(pilotInfoLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Table & Guest Warning Container
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(13, 17, 30));

        // Guest advisory panel
        guestNoticePanel = new JPanel(new GridBagLayout());
        guestNoticePanel.setBackground(new Color(18, 24, 42));
        guestNoticePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 60, 100)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel guestWarning = new JLabel("GUEST FLIGHT LOGS ARE STORED LOCALLY ONLY");
        guestWarning.setFont(new Font("SansSerif", Font.BOLD, 12));
        guestWarning.setForeground(new Color(255, 190, 60));
        guestNoticePanel.add(guestWarning, gbc);

        gbc.gridy = 1;
        JButton loginPromptBtn = new JButton("AUTHENTICATE PILOT TO SYNC LOGS");
        loginPromptBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        loginPromptBtn.setForeground(Color.WHITE);
        loginPromptBtn.setBackground(new Color(0, 150, 220));
        loginPromptBtn.setFocusPainted(false);
        loginPromptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginPromptBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
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
        historyTable.setBackground(new Color(18, 24, 40));
        historyTable.setForeground(Color.WHITE);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        historyTable.setGridColor(new Color(30, 42, 65));
        historyTable.getTableHeader().setBackground(new Color(25, 34, 58));
        historyTable.getTableHeader().setForeground(new Color(0, 210, 255));
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(new Color(13, 17, 30));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(10, 13, 24));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 45, 75)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        statusLabel = new JLabel("Status: Ready", SwingConstants.LEFT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 180, 220));
        footerPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        refreshBtn = createButton("REFRESH", new Color(35, 60, 105));
        refreshBtn.addActionListener(e -> loadHistoryData());
        btnPanel.add(refreshBtn);

        JButton closeBtn = createButton("CLOSE", new Color(60, 35, 45));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footerPanel.add(btnPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadHistoryData() {
        UserSession session = SessionManager.getInstance().getCurrentSession();
        if (!session.isAuthenticated()) {
            pilotInfoLabel.setText("PILOT: GUEST / OFFLINE");
            pilotInfoLabel.setForeground(new Color(160, 190, 230));
            guestNoticePanel.setVisible(true);
            tableModel.setRowCount(0);
            statusLabel.setText("Offline: Mission logs require authenticated pilot clearance.");
            statusLabel.setForeground(new Color(255, 190, 60));
            return;
        }

        pilotInfoLabel.setText("PILOT: " + session.getUsername().toUpperCase() + " (CLEARANCE: ACTIVE)");
        pilotInfoLabel.setForeground(new Color(80, 250, 120));
        guestNoticePanel.setVisible(false);

        statusLabel.setText("Retrieving mission records from Starfleet Archives...");
        statusLabel.setForeground(new Color(0, 210, 255));
        refreshBtn.setEnabled(false);

        matchService.fetchMyMatchHistory().thenAccept(records -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                tableModel.setRowCount(0);

                if (records.isEmpty()) {
                    statusLabel.setText("No flight logs on record. Launch missions to build your service record.");
                    statusLabel.setForeground(new Color(160, 190, 220));
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
                    statusLabel.setForeground(new Color(80, 250, 120));
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                statusLabel.setText("Comm link failure: Unable to reach Starfleet Archives.");
                statusLabel.setForeground(new Color(255, 90, 90));
            });
            return null;
        });
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }
}

