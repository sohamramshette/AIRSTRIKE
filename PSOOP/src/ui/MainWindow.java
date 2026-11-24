// ==============================================================================
// File      : PSOOP/src/ui/MainWindow.java
// Subject   : PSOOP-CO2 / CO4 | Primary Application Window & CardLayout Router
// Topics    : [TOPIC: Threading/Swing EDT] [TOPIC: CardLayout Navigation] [TOPIC: Game Loop]
// Game Role : Owns top-level JFrame, routes between MenuPanel and GameCanvas, and launches dialogs.
// ==============================================================================
package ui;

import database.SessionManager;
import game.GameCanvas;
import game.GameEngine;
import game.GameLoop;
import service.AuthService;
import service.MatchService;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * [TOPIC: CardLayout Navigation]
 * Main application frame orchestrating menu/gameplay switching and subsystem dialogs.
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final GameEngine engine;
    private final GameCanvas gameCanvas;
    private final MenuPanel menuPanel;
    private final GameLoop gameLoop;
    private final AuthService authService;
    private final MatchService matchService;

    public MainWindow() {
        super("AIRSTRIKE – 2D Space Shooter & Academic Demonstration System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 650);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        // Subsystems
        this.authService = new AuthService();
        this.matchService = new MatchService(authService.getHttpClient(), SessionManager.getInstance());
        this.engine = new GameEngine(800, 650, matchService);
        this.gameCanvas = new GameCanvas(engine, this);
        this.menuPanel = new MenuPanel(this, authService);
        this.gameLoop = new GameLoop(engine, gameCanvas);

        // CardLayout container
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);

        rootPanel.add(menuPanel, "MENU");
        rootPanel.add(gameCanvas, "GAME");

        add(rootPanel);

        // WHY: Start the Swing Timer loop immediately so the starfield renders continuously in MENU state.
        gameLoop.start();
        showMainMenu();
    }

    /**
     * [TOPIC: CardLayout Navigation] Switches view to MenuPanel and restores keyboard focus.
     */
    public void showMainMenu() {
        cardLayout.show(rootPanel, "MENU");
        menuPanel.requestFocusInWindow();
    }

    /**
     * [TOPIC: CardLayout Navigation] Switches to GameCanvas, starts solo run, and focuses canvas for WASD/Space.
     */
    public void startGame() {
        cardLayout.show(rootPanel, "GAME");
        engine.startNewGame();
        // WHY: CardLayout.show does not auto-focus the newly revealed panel; explicit request enables KeyListener input.
        gameCanvas.requestFocusInWindow();
    }

    /**
     * [TOPIC: Multiplayer Telemetry] Launches a seeded 1v1 match on GameCanvas.
     */
    public void startMultiplayerGame(long matchSeed, String startedAt, database.multiplayer.MultiplayerModels.MultiplayerRoomState room) {
        cardLayout.show(rootPanel, "GAME");
        engine.startMultiplayerGame(matchSeed, startedAt, room);
        gameCanvas.requestFocusInWindow();
    }

    /**
     * [TOPIC: Subject CO Matrix] Opens the interactive multi-subject enemy demo arena.
     */
    public void startEnemyDemo() {
        cardLayout.show(rootPanel, "GAME");
        engine.startEnemyDemo();
        gameCanvas.requestFocusInWindow();
    }

    public void showInstructions() {
        InstructionsDialog dialog = new InstructionsDialog(this);
        dialog.setVisible(true);
    }

    public void showSettings() {
        showProfileSettingsDialog();
    }

    public void showProfileSettingsDialog() {
        ProfileSettingsDialog dialog = new ProfileSettingsDialog(this, this, engine, authService);
        dialog.setVisible(true);
    }

    public void showSubjectCODialog() {
        SubjectCODialog dialog = new SubjectCODialog(this, this);
        dialog.setVisible(true);
    }

    public void showNativeBridgeDialog() {
        NativeBridgeDialog dialog = new NativeBridgeDialog(this);
        dialog.setVisible(true);
    }

    public void showAuthDialog() {
        showAuthDialog("LOGIN");
    }

    public void showAuthDialog(String defaultTab) {
        AuthDialog dialog = new AuthDialog(this, authService, defaultTab);
        dialog.setVisible(true);
    }

    public void showLeaderboardDialog() {
        LeaderboardDialog dialog = new LeaderboardDialog(this, this, matchService);
        dialog.setVisible(true);
    }

    public void showMultiplayerDialog() {
        MultiplayerDialog dialog = new MultiplayerDialog(this, this);
        dialog.setVisible(true);
    }

    public void showMatchHistoryDialog() {
        MatchHistoryDialog dialog = new MatchHistoryDialog(this, this, matchService);
        dialog.setVisible(true);
    }

    public GameEngine getEngine() { return engine; }
    public GameCanvas getGameCanvas() { return gameCanvas; }
    public AuthService getAuthService() { return authService; }
    public MatchService getMatchService() { return matchService; }

    public static void main(String[] args) {
        main.Main.main(args);
    }
}
