// ==============================================================================
// File      : PSOOP/src/game/GameLoop.java
// Subject   : PSOOP-CO4 | 60 FPS Swing Timer Game Loop
// Topics    : [TOPIC: Game Loop] [TOPIC: 60 FPS Delta-Time] [TOPIC: Threading/Swing EDT]
// Game Role : Drives fixed-cadence simulation ticks and EDT repaints in AIRSTRIKE.
// ==============================================================================
package game;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * [TOPIC: Game Loop] [TOPIC: Threading/Swing EDT]
 * 60 FPS / UPS game loop orchestrated via Swing Timer on the Event Dispatch Thread (EDT).
 */
public class GameLoop implements ActionListener {

    private final GameEngine engine;
    private final Component renderTarget;
    private final Timer timer;
    private long lastTime;
    private boolean running;

    /**
     * [TOPIC: 60 FPS Delta-Time] [TOPIC: Threading/Swing EDT]
     * Constructs a game loop targeting 60 updates/sec (~16 ms tick interval).
     */
    public GameLoop(GameEngine engine, Component renderTarget) {
        this.engine = engine;
        this.renderTarget = renderTarget;
        // WHY: javax.swing.Timer executes actionPerformed on the EDT and coalesces backlog ticks, avoiding Swing race conditions.
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

    /**
     * [TOPIC: 60 FPS Delta-Time] Computes elapsed seconds and advances one tick.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        if (lastTime == 0) lastTime = now;

        double delta = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        // WHY: Clamp delta to 50 ms max so window drags or GC pauses do not cause physics tunneling across hitboxes.
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

