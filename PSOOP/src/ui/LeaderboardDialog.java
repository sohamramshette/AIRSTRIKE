package ui;

import database.SessionManager;
import database.UserSession;
import model.LeaderboardEntry;
import service.MatchService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
 * Modal dialog presenting the Starfleet Global Leaderboard (Top 10 Pilots).
 * Queries Supabase PostgREST endpoint `GET /rest/v1/leaderboard` asynchronously.
 *
 * Implements:
 * - Gold/Silver/Bronze distinctive styling for top-ranking aces.
 * - Non-blocking asynchronous data fetching off the Swing EDT.
 * - Loading, empty, and error feedback states with manual refresh capability.
 */
public class LeaderboardDialog extends JDialog {

    private final MainWindow mainWindow;
    private final MatchService matchService;

    private JLabel statusLabel;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn;

    public LeaderboardDialog(JFrame parent, MainWindow mainWindow, MatchService matchService) {
        super(parent, "STARFLEET GLOBAL LEADERBOARD – TOP PILOTS", true);
        this.mainWindow = mainWindow;
        this.matchService = matchService;

        setSize(580, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(13, 17, 30));
        setLayout(new BorderLayout());

        buildUI();
        loadLeaderboardData();
    }

    private void buildUI() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(10, 13, 24));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 45, 75)),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("STARFLEET GLOBAL HALL OF FAME", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLbl.setForeground(new Color(255, 215, 0)); // Golden radiance
        headerPanel.add(titleLbl, BorderLayout.NORTH);

        JLabel subLbl = new JLabel("Top 10 Ace Pilots Across the Cosmos – Synchronized via Supabase", SwingConstants.CENTER);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLbl.setForeground(new Color(150, 180, 220));
        subLbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        headerPanel.add(subLbl, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Table in Center
        String[] columns = {"RANK", "PILOT CALLSIGN", "ALL-TIME BEST SCORE"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setBackground(new Color(18, 24, 40));
        leaderboardTable.setForeground(Color.WHITE);
        leaderboardTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        leaderboardTable.setRowHeight(32);
        leaderboardTable.setGridColor(new Color(30, 42, 65));
        leaderboardTable.getTableHeader().setBackground(new Color(25, 34, 58));
        leaderboardTable.getTableHeader().setForeground(new Color(0, 210, 255));
        leaderboardTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(170);

        // Custom styled cell renderer for Ace medals and scores
        LeaderboardCellRenderer customRenderer = new LeaderboardCellRenderer();
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.getViewport().setBackground(new Color(13, 17, 30));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        add(scrollPane, BorderLayout.CENTER);

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
        refreshBtn.addActionListener(e -> loadLeaderboardData());
        btnPanel.add(refreshBtn);

        JButton closeBtn = createButton("CLOSE", new Color(60, 35, 45));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footerPanel.add(btnPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadLeaderboardData() {
        statusLabel.setText("Connecting to Starfleet High Command...");
        statusLabel.setForeground(new Color(0, 210, 255));
        refreshBtn.setEnabled(false);

        matchService.fetchGlobalLeaderboard().thenAccept(entries -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                tableModel.setRowCount(0);

                if (entries.isEmpty()) {
                    statusLabel.setText("No active flight combat records found in global leaderboard.");
                    statusLabel.setForeground(new Color(160, 190, 220));
                } else {
                    for (LeaderboardEntry entry : entries) {
                        String rankDisplay = "#" + entry.getRank();
                        if (entry.getRank() == 1) rankDisplay = "★ #1 ACE";
                        else if (entry.getRank() == 2) rankDisplay = "♦ #2";
                        else if (entry.getRank() == 3) rankDisplay = "▲ #3";

                        tableModel.addRow(new Object[]{
                                rankDisplay,
                                entry.getUsername(),
                                String.format("%,d PTS", entry.getBestScore())
                        });
                    }
                    statusLabel.setText("Retrieved " + entries.size() + " top pilot ranking(s) successfully.");
                    statusLabel.setForeground(new Color(80, 250, 120));
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                statusLabel.setText("Comm link failure: Unable to retrieve global leaderboard.");
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

    /**
     * Custom renderer for stylish high-contrast medals and rank styling.
     */
    private static class LeaderboardCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? new Color(18, 24, 40) : new Color(22, 30, 48));
                if (row == 0) {
                    c.setForeground(new Color(255, 215, 0)); // Gold
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (row == 1) {
                    c.setForeground(new Color(215, 225, 240)); // Silver
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (row == 2) {
                    c.setForeground(new Color(230, 160, 95)); // Bronze
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(column == 2 ? new Color(0, 210, 255) : Color.WHITE);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }
}

