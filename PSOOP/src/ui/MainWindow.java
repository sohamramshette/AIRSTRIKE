package ui;

import game.GameCanvas;
import game.GameEngine;
import game.GameLoop;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main application window for AIRSTRIKE.
 * Houses CardLayout navigation between the Main Menu and active Combat Canvas.
 */
public class MainWindow extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final GameEngine engine;
    private final GameCanvas gameCanvas;
    private final MenuPanel menuPanel;
    private final GameLoop gameLoop;

    public MainWindow() {
        super("AIRSTRIKE – 2D Space Shooter & Academic Demonstration System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 650);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        // Subsystems
        this.engine = new GameEngine(800, 650);
        this.gameCanvas = new GameCanvas(engine, this);
        this.menuPanel = new MenuPanel(this);
        this.gameLoop = new GameLoop(engine, gameCanvas);

        // CardLayout container
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);

        rootPanel.add(menuPanel, "MENU");
        rootPanel.add(gameCanvas, "GAME");

        add(rootPanel);

        // Start background game loop for starfield animation
        gameLoop.start();
        showMainMenu();
    }

    public void showMainMenu() {
        cardLayout.show(rootPanel, "MENU");
        menuPanel.requestFocusInWindow();
    }

    public void startGame() {
        cardLayout.show(rootPanel, "GAME");
        engine.startNewGame();
        gameCanvas.requestFocusInWindow();
    }

    public void showInstructions() {
        InstructionsDialog dialog = new InstructionsDialog(this);
        dialog.setVisible(true);
    }

    public void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this, engine);
        dialog.setVisible(true);
    }

    public GameEngine getEngine() { return engine; }
    public GameCanvas getGameCanvas() { return gameCanvas; }
}
