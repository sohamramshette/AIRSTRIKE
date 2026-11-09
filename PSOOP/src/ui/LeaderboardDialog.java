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
import java.awt.FlowLayout;
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
 * Styled with the Phase 9 Design System.
 */
public class LeaderboardDialog extends JDialog {

    private final MainWindow mainWindow;
    private final MatchService matchService;

    private JLabel statusLabel;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private CyberButton refreshBtn;

    public LeaderboardDialog(JFrame parent, MainWindow mainWindow, MatchService matchService) {
        super(parent, "STARFLEET GLOBAL LEADERBOARD – TOP PILOTS", true);
        this.mainWindow = mainWindow;
        this.matchService = matchService;

        setSize(600, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        buildUI();
        loadLeaderboardData();
    }

    private void buildUI() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_PANEL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("STARFLEET GLOBAL HALL OF FAME", SwingConstants.CENTER);
        titleLbl.setFont(Theme.HEADER_1);
        titleLbl.setForeground(Theme.GOLD_ACCENT);
        headerPanel.add(titleLbl, BorderLayout.NORTH);

        JLabel subLbl = new JLabel("Top 10 Ace Pilots Across the Cosmos – Synchronized via Supabase", SwingConstants.CENTER);
        subLbl.setFont(Theme.BODY);
        subLbl.setForeground(Theme.TEXT_MUTED);
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
        leaderboardTable.setBackground(Theme.BG_INPUT);
        leaderboardTable.setForeground(Color.WHITE);
        leaderboardTable.setFont(Theme.BODY);
        leaderboardTable.setRowHeight(34);
        leaderboardTable.setGridColor(new Color(30, 42, 65));
        leaderboardTable.getTableHeader().setBackground(new Color(25, 34, 58));
        leaderboardTable.getTableHeader().setForeground(Theme.CYAN_PRIMARY);
        leaderboardTable.getTableHeader().setFont(Theme.BODY_BOLD);

        // Column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(270);
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(170);

        LeaderboardCellRenderer customRenderer = new LeaderboardCellRenderer();
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.getViewport().setBackground(Theme.BG_DEEP);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        add(scrollPane, BorderLayout.CENTER);

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
        refreshBtn.addActionListener(e -> loadLeaderboardData());
        btnPanel.add(refreshBtn);

        CyberButton closeBtn = CyberButton.danger("CLOSE");
        closeBtn.setPreferredSize(new Dimension(80, 26));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footerPanel.add(btnPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadLeaderboardData() {
        statusLabel.setText("Connecting to Starfleet High Command...");
        statusLabel.setForeground(Theme.CYAN_PRIMARY);
        refreshBtn.setEnabled(false);

        matchService.fetchGlobalLeaderboard().thenAccept(entries -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                tableModel.setRowCount(0);

                if (entries == null || entries.isEmpty()) {
                    statusLabel.setText("No active flight combat records found in global leaderboard.");
                    statusLabel.setForeground(Theme.TEXT_MUTED);
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
                    statusLabel.setForeground(Theme.EMERALD_STATUS);
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                refreshBtn.setEnabled(true);
                statusLabel.setText("Comm link failure: Unable to retrieve global leaderboard.");
                statusLabel.setForeground(Theme.CORAL_DANGER);
            });
            return null;
        });
    }

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
                    c.setForeground(Theme.GOLD_ACCENT);
                    c.setFont(Theme.BODY_BOLD);
                } else if (row == 1) {
                    c.setForeground(new Color(215, 225, 240));
                    c.setFont(Theme.BODY_BOLD);
                } else if (row == 2) {
                    c.setForeground(new Color(230, 160, 95));
                    c.setFont(Theme.BODY_BOLD);
                } else {
                    c.setForeground(column == 2 ? Theme.CYAN_PRIMARY : Color.WHITE);
                    c.setFont(Theme.BODY);
                }
            }
            return c;
        }
    }
}
