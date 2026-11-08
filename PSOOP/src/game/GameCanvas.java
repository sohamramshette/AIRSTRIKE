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
 * Double-buffered Swing rendering canvas for active space combat gameplay.
 * Handles keyboard dispatching and antialiased rendering calls.
 */
public class GameCanvas extends JPanel {

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

    private void setupFocusHandlers() {
        // Regain keyboard focus if user clicks anywhere on canvas
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // Clear active input flags if canvas loses focus
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // High quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Delegate to GameEngine
        engine.render(g2d, getWidth(), getHeight());
    }
}

