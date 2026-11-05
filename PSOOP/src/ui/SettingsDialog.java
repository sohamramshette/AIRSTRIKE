package ui;

import game.GameEngine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Settings configuration modal for mission difficulty and simulation parameters.
 */
public class SettingsDialog extends JDialog {

    private final JComboBox<String> difficultyBox;
    private final GameEngine engine;

    public SettingsDialog(JFrame parent, GameEngine engine) {
        super(parent, "AIRSTRIKE – Mission Settings", true);
        this.engine = engine;

        setSize(450, 320);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(15, 18, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("TACTICAL MISSION CONFIGURATION");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(0, 210, 255));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(22, 28, 45));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 60, 90)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Difficulty Setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel diffLbl = new JLabel("Simulation Threat Level:");
        diffLbl.setForeground(Color.WHITE);
        diffLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(diffLbl, gbc);

        gbc.gridx = 1;
        String[] options = {"Cadet (Standard Shield & Hull)", "Veteran (Faster Waves)", "Ace Commander (Elite Squadrons)"};
        difficultyBox = new JComboBox<>(options);
        difficultyBox.setBackground(new Color(30, 40, 65));
        difficultyBox.setForeground(Color.WHITE);
        form.add(difficultyBox, gbc);

        // Graphics Hint
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel renderLbl = new JLabel("Renderer Pipeline:");
        renderLbl.setForeground(Color.WHITE);
        renderLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(renderLbl, gbc);

        gbc.gridx = 1;
        JLabel renderVal = new JLabel("Hardware 2D Double-Buffered EDT");
        renderVal.setForeground(new Color(100, 220, 160));
        renderVal.setFont(new Font("SansSerif", Font.BOLD, 12));
        form.add(renderVal, gbc);

        panel.add(form, BorderLayout.CENTER);

        JButton saveBtn = new JButton("SAVE & CONFIRM");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setBackground(new Color(0, 180, 255));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(160, 36));
        saveBtn.addActionListener(e -> applyAndClose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(15, 18, 30));
        btnPanel.add(saveBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(panel);
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
        dispose();
    }
}

