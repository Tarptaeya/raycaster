package com.anmolgautam.raycaster;

public class Player {
    protected int x;
    protected int y;
    protected double angle = 0;

    protected static final int size = App.cellSize / 5;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
