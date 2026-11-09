package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * Pilot Flight Manual dialog displaying flight controls, mission shortcuts, and combat mechanics.
 * Styled with the Phase 9 Design System.
 */
public class InstructionsDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public InstructionsDialog(JFrame parent) {
        super(parent, "STARFLEET COMMAND – PILOT FLIGHT MANUAL", true);
        setSize(600, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_PANEL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("PILOT FLIGHT MANUAL & COMBAT CONTROLS", SwingConstants.CENTER);
        titleLbl.setFont(Theme.HEADER_1);
        titleLbl.setForeground(Theme.CYAN_PRIMARY);
        headerPanel.add(titleLbl, BorderLayout.NORTH);

        JLabel subLbl = new JLabel("Standard Spacecraft Navigation, Ordinance Operations & Survival Guidelines", SwingConstants.CENTER);
        subLbl.setFont(Theme.BODY);
        subLbl.setForeground(Theme.TEXT_MUTED);
        subLbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        headerPanel.add(subLbl, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Body with HTML text pane
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(Theme.BG_INPUT);
        textPane.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String html = """
            <html>
            <body style='font-family: sans-serif; color: #F8FAFC; background-color: #161F38;'>
                <h3 style='color: #00E5FF; margin-top: 4px; margin-bottom: 6px;'>1. FLIGHT NAVIGATION</h3>
                <ul style='color: #CBD5E1; line-height: 1.5;'>
                    <li><b style='color: #FFFFFF;'>Move Forward / Climb:</b> [W] or [UP ARROW]</li>
                    <li><b style='color: #FFFFFF;'>Move Aft / Dive:</b> [S] or [DOWN ARROW]</li>
                    <li><b style='color: #FFFFFF;'>Bank Port (Left):</b> [A] or [LEFT ARROW]</li>
                    <li><b style='color: #FFFFFF;'>Bank Starboard (Right):</b> [D] or [RIGHT ARROW]</li>
                    <li><b style='color: #FFFFFF;'>Fire Primary Plasma Cannons:</b> [SPACEBAR] (Hold for sustained fire)</li>
                </ul>

                <h3 style='color: #FBBF24; margin-top: 10px; margin-bottom: 6px;'>2. TACTICAL MISSION SHORTCUTS</h3>
                <ul style='color: #CBD5E1; line-height: 1.5;'>
                    <li><b style='color: #FFFFFF;'>Tactical Pause:</b> [P] or [ESCAPE]</li>
                    <li><b style='color: #FFFFFF;'>Quick Redeploy (Restart):</b> [R]</li>
                    <li><b style='color: #FFFFFF;'>Abort to Main Menu:</b> [M] (From Pause or Game Over)</li>
                    <li><b style='color: #FFFFFF;'>Audio Mute:</b> [MUTE TOGGLE in Settings]</li>
                </ul>

                <h3 style='color: #34D399; margin-top: 10px; margin-bottom: 6px;'>3. COMBAT SYSTEMS & ORDNANCE</h3>
                <ul style='color: #CBD5E1; line-height: 1.5;'>
                    <li><b style='color: #00E5FF;'>Shield Capacitors:</b> Absorbs incoming plasma energy before hull breach.</li>
                    <li><b style='color: #34D399;'>[+] Repair Kit:</b> Instantly restores +40 Hull Integrity.</li>
                    <li><b style='color: #38BDF8;'>[S] Shield Booster:</b> Rapidly recharges depleted shield reserves.</li>
                    <li><b style='color: #FBBF24;'>[W] Weapon Upgrade:</b> Dual Cannons &rarr; Triple Spread Cannons.</li>
                    <li><b style='color: #A855F7;'>[^] Hyper Drive:</b> Permanent propulsion acceleration increase.</li>
                    <li><b style='color: #EF4444;'>Kill Streaks:</b> Rapid kills grant up to 4x score multipliers!</li>
                </ul>
            </body>
            </html>
            """;
        textPane.setText(html);
        textPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        scroll.getViewport().setBackground(Theme.BG_DEEP);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Theme.BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE));

        CyberButton closeBtn = CyberButton.hero("RETURN TO COMMAND");
        closeBtn.setPreferredSize(new Dimension(190, 34));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }
}
