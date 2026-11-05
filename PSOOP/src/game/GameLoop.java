package game;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * 60 FPS / UPS game loop orchestrated via Swing Timer.
 * Guarantees that physics updates and rendering repaints are synchronized safely
 * on the Swing Event Dispatch Thread (EDT).
 */
public class GameLoop implements ActionListener {

    private final GameEngine engine;
    private final Component renderTarget;
    private final Timer timer;
    private long lastTime;
    private boolean running;

    /**
     * Constructs a game loop targeting 60 updates/sec (~16 ms tick interval).
     */
    public GameLoop(GameEngine engine, Component renderTarget) {
        this.engine = engine;
        this.renderTarget = renderTarget;
        this.timer = new Timer(16, this);
        this.timer.setCoalesce(true);
        this.running = false;
        this.lastTime = 0;
    }

    public void start() {
        if (!running) {
            this.lastTime = System.nanoTime();
            this.running = true;
            this.timer.start();
        }
    }

    public void stop() {
        if (running) {
            this.running = false;
            this.timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        if (lastTime == 0) lastTime = now;

        double delta = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        // Clamp delta to avoid large spiral jumps if paused/lagged
        if (delta > 0.05) delta = 0.05;

        // 1. Advance simulation
        engine.update(delta);

        // 2. Request surface repaint on EDT
        if (renderTarget != null) {
            renderTarget.repaint();
        }
    }

    public boolean isRunning() { return running; }
}
