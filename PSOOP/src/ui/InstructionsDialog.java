package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Dialog displaying player flight manual, key bindings, and combat mechanics.
 */
public class InstructionsDialog extends JDialog {

    public InstructionsDialog(JFrame parent) {
        super(parent, "AIRSTRIKE – Pilot Flight Manual", true);
        setSize(560, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(15, 18, 30));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(new Color(22, 28, 45));
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String html = """
            <html>
            <body style='font-family: sans-serif; color: #E0E8F5;'>
                <h2 style='color: #00D2FF; margin-bottom: 2px;'>FLIGHT MANUAL & CONTROLS</h2>
                <hr style='border: 1px solid #285082;'/>
                
                <h3 style='color: #FFB428; margin-top: 8px;'>1. NAVIGATION CONTROLS</h3>
                <ul>
                    <li><b>Move Up / Forward:</b> [W] or [UP ARROW]</li>
                    <li><b>Move Down / Aft:</b> [S] or [DOWN ARROW]</li>
                    <li><b>Move Left:</b> [A] or [LEFT ARROW]</li>
                    <li><b>Move Right:</b> [D] or [RIGHT ARROW]</li>
                    <li><b>Fire Plasma Laser:</b> [SPACEBAR] (Hold for sustained fire)</li>
                </ul>

                <h3 style='color: #FFB428; margin-top: 8px;'>2. MISSION SYSTEM KEYS</h3>
                <ul>
                    <li><b>Pause / Resume Mission:</b> [P] or [ESCAPE]</li>
                    <li><b>Quick Redeploy (Restart):</b> [R]</li>
                    <li><b>Return to Main Menu:</b> [M] (From Pause or Game Over)</li>
                </ul>

                <h3 style='color: #FFB428; margin-top: 8px;'>3. COMBAT & POWER-UPS</h3>
                <ul>
                    <li><b>Shields:</b> Absorbs incoming laser damage before hull is breached.</li>
                    <li><b>[+] Health Pack:</b> Repairs hull integrity by +40 points.</li>
                    <li><b>[S] Shield Boost:</b> Recharges protective shield capacitors.</li>
                    <li><b>[W] Plasma Cannon:</b> Upgrades primary weapons (Single -> Dual -> Triple Spread).</li>
                    <li><b>[^] Hyper Engine:</b> Grants permanent propulsion speed boost.</li>
                    <li><b>Combos:</b> Rapid kills grant up to 4x score multipliers!</li>
                </ul>
            </body>
            </html>
            """;
        textPane.setText(html);
        textPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 60, 90)));
        content.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("RETURN TO COMMAND");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        closeBtn.setBackground(new Color(0, 150, 220));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(200, 36));
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(15, 18, 30));
        btnPanel.add(closeBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }
}

