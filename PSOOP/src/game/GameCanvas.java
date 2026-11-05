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
                } else if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    // Pause toggle
                    if (engine.getState() == GameState.PLAYING) {
                        engine.pauseGame();
                    } else if (engine.getState() == GameState.PAUSED) {
                        engine.resumeGame();
                    }
                } else if (code == KeyEvent.VK_R) {
                    // Quick restart from Game Over or active session
                    if (engine.getState() == GameState.GAME_OVER || engine.getState() == GameState.PLAYING) {
                        engine.startNewGame();
                    }
                } else if (code == KeyEvent.VK_M) {
                    // Return to Main Menu
                    if (engine.getState() == GameState.PAUSED || engine.getState() == GameState.GAME_OVER) {
                        try {
                            engine.setState(GameState.MENU);
                            if (mainWindow != null) {
                                mainWindow.showMainMenu();
                            }
                        } catch (InvalidGameStateException ignored) {}
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
