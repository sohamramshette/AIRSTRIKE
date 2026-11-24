// ==============================================================================
// File      : PSOOP/src/game/GameCanvas.java
// Subject   : PSOOP-CO4 | Double-Buffered Swing Gameplay Surface
// Topics    : [TOPIC: Custom Swing Rendering] [TOPIC: Threading/Swing EDT] [TOPIC: Input Handling]
// Game Role : Captures keyboard/mouse input and delegates Graphics2D rendering to GameEngine.
// ==============================================================================
package game;

import exception.InvalidGameStateException;
import model.Player;
import ui.MainWindow;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

/**
 * [TOPIC: Custom Swing Rendering] [TOPIC: Input Handling]
 * Double-buffered Swing rendering canvas for active space combat gameplay.
 */
public class GameCanvas extends JPanel {
    private static final long serialVersionUID = 1L;

    private final GameEngine engine;
    private final MainWindow mainWindow;

    public GameCanvas(GameEngine engine, MainWindow mainWindow) {
        this.engine = engine;
        this.mainWindow = mainWindow;

        setPreferredSize(new Dimension(800, 650));
        setBackground(java.awt.Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        setupInputBindings();
        setupFocusHandlers();
    }

    /**
     * [TOPIC: Input Handling] Registers mouse hit-testing and focus-loss key resets.
     */
    private void setupFocusHandlers() {
        // Regain keyboard focus and handle interactive Pause & Game Over button clicks
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();

                if (engine.getState() == GameState.PAUSED) {
                    GameEngine.PauseButton pb = engine.getClickedPauseButton(e.getX(), e.getY());
                    switch (pb) {
                        case RESUME -> engine.resumeGame();
                        case RESTART -> engine.startNewGame();
                        case SETTINGS -> {
                            if (mainWindow != null) mainWindow.showProfileSettingsDialog();
                        }
                        case MENU -> {
                            try {
                                engine.setState(GameState.MENU);
                                if (mainWindow != null) mainWindow.showMainMenu();
                            } catch (InvalidGameStateException ignored) {}
                        }
                        case EXIT -> System.exit(0);
                        case NONE -> {}
                    }
                } else if (engine.getState() == GameState.GAME_OVER) {
                    GameEngine.GameOverButton gb = engine.getClickedGameOverButton(e.getX(), e.getY());
                    switch (gb) {
                        case REDEPLOY -> engine.startNewGame();
                        case MENU -> {
                            try {
                                engine.setState(GameState.MENU);
                                if (mainWindow != null) mainWindow.showMainMenu();
                            } catch (InvalidGameStateException ignored) {}
                        }
                        case NONE -> {}
                    }
                }
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (engine.getState() == GameState.PAUSED || engine.getState() == GameState.GAME_OVER) {
                    engine.setMousePos(e.getX(), e.getY());
                    repaint();
                }
            }
        });

        // WHY: Clearing movement and fire flags on focus loss prevents stuck ship motion when Alt-Tabbing.
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                Player player = engine.getPlayer();
                if (player != null) {
                    player.setMoveLeft(false);
                    player.setMoveRight(false);
                    player.setMoveUp(false);
                    player.setMoveDown(false);
                }
                engine.setShooting(false);
            }
        });
    }

    /**
     * [TOPIC: Input Handling] Binds flight, firing, demo spawn, and pause hotkeys.
     */
    private void setupInputBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Player player = engine.getPlayer();
                int code = e.getKeyCode();

                // Movement controls: WASD or Arrow Keys
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    player.setMoveLeft(true);
                } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    player.setMoveRight(true);
                } else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    player.setMoveUp(true);
                } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                    player.setMoveDown(true);
                } else if (code == KeyEvent.VK_SPACE) {
                    engine.setShooting(true);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_1 || code == KeyEvent.VK_NUMPAD1)) {
                    engine.spawnDemoEnemy(1);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_2 || code == KeyEvent.VK_NUMPAD2)) {
                    engine.spawnDemoEnemy(2);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_3 || code == KeyEvent.VK_NUMPAD3)) {
                    engine.spawnDemoEnemy(3);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_4 || code == KeyEvent.VK_NUMPAD4)) {
                    engine.spawnDemoEnemy(4);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_5 || code == KeyEvent.VK_NUMPAD5)) {
                    engine.spawnDemoEnemy(5);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_6 || code == KeyEvent.VK_NUMPAD6)) {
                    engine.spawnDemoEnemy(6);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_7 || code == KeyEvent.VK_NUMPAD7)) {
                    engine.spawnDemoEnemy(7);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_8 || code == KeyEvent.VK_NUMPAD8)) {
                    engine.spawnDemoEnemy(8);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_9 || code == KeyEvent.VK_NUMPAD9)) {
                    engine.spawnDemoEnemy(9);
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_0 || code == KeyEvent.VK_NUMPAD0)) {
                    engine.spawnDemoEnemy(10);
                } else if (code == KeyEvent.VK_B && mainWindow != null) {
                    mainWindow.showNativeBridgeDialog();
                } else if (engine.isEnemyDemoMode() && (code == KeyEvent.VK_C)) {
                    engine.clearDemoEnemies();
                } else if (code == KeyEvent.VK_P) {
                    // Pause toggle
                    if (engine.getState() == GameState.PLAYING) {
                        engine.pauseGame();
                    } else if (engine.getState() == GameState.PAUSED) {
                        engine.resumeGame();
                    }
                } else if (code == KeyEvent.VK_R) {
                    if (engine.isEnemyDemoMode()) {
                        engine.resetDemoFleet();
                    } else if (engine.isMultiplayer()) {
                        try {
                            engine.setState(GameState.MENU);
                            if (mainWindow != null) {
                                mainWindow.showMainMenu();
                                mainWindow.showMultiplayerDialog();
                            }
                        } catch (InvalidGameStateException ignored) {}
                    } else if (engine.getState() == GameState.GAME_OVER || engine.getState() == GameState.PLAYING) {
                        engine.startNewGame();
                    }
                } else if (code == KeyEvent.VK_M || (engine.isEnemyDemoMode() && code == KeyEvent.VK_ESCAPE)) {
                    // Return to Main Menu
                    if (engine.isEnemyDemoMode() || engine.getState() == GameState.PAUSED || engine.getState() == GameState.GAME_OVER) {
                        try {
                            engine.setState(GameState.MENU);
                            if (mainWindow != null) {
                                mainWindow.showMainMenu();
                            }
                        } catch (InvalidGameStateException ignored) {}
                    }
                } else if (code == KeyEvent.VK_ESCAPE) {
                    if (engine.getState() == GameState.PLAYING) {
                        engine.pauseGame();
                    } else if (engine.getState() == GameState.PAUSED) {
                        engine.resumeGame();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Player player = engine.getPlayer();
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    player.setMoveLeft(false);
                } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    player.setMoveRight(false);
                } else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    player.setMoveUp(false);
                } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                    player.setMoveDown(false);
                } else if (code == KeyEvent.VK_SPACE) {
                    engine.setShooting(false);
                }
            }
        });
    }

    /**
     * [TOPIC: Custom Swing Rendering] [TOPIC: Threading/Swing EDT]
     * Paints the combat scene on the EDT with antialiasing enabled.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // WHY: Smooth vector ship hulls and HUD text at high-DPI/scaled resolutions.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Delegate to GameEngine
        engine.render(g2d, getWidth(), getHeight());
    }
}

