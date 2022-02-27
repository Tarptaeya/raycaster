package com.anmolgautam.raycaster;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MainPanel extends JPanel {
    private final Engine engine;

    public MainPanel(Engine engine) {
        this.engine = engine;

        addKeyListener(this.engine);

        App.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(App.width, App.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        engine.paint(g);
    }
}
