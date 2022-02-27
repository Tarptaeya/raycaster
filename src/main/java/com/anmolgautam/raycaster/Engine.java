package com.anmolgautam.raycaster;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Engine implements KeyListener {
    private static class HitRecord {
        private double nextX;
        private double nextY;
        private double distance;
        private int wall;

        public HitRecord(double nextX, double nextY, double distance, int wall) {
            this.nextX = nextX;
            this.nextY = nextY;
            this.distance = distance;
            this.wall = wall;
        }
    }

    private final Player player;
    private final BufferedImage bufferedImage;
    private final BufferedImage texture;

    private static final int cellSize = App.cellSize;
    private static final int screenWidth = App.width;
    private static final int screenHeight = App.height;

    private static final int sky = new Color(204, 235, 255).getRGB();
    private static final int grass = new Color(86, 125, 70).getRGB();

    public Engine(Player player) throws IOException {
        this.player = player;
        bufferedImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        texture = ImageIO.read(getClass().getResourceAsStream("/wolftextures.png"));
    }

    public void paint(Graphics g) {
        double fov = Math.PI / 3;
        double alpha = player.angle - fov / 2;
        double step = fov / screenWidth;
        double distanceToProjectionPlane = screenWidth / (2 * Math.tan(fov / 2));

        for (int i = 0; i < screenWidth; ++i) {
            for (int j = 0; j < screenHeight / 2; ++j) {
                bufferedImage.setRGB(i, j, sky);
            }
            for (int j = screenHeight / 2; j < screenHeight; ++j) {
                bufferedImage.setRGB(i, j, grass);
            }
        }

        for (int col = 0; col < screenWidth; ++col) {
            HitRecord vRec = getVRecord(alpha);
            HitRecord hRec = getHRecord(alpha);
            HitRecord rec = vRec.distance <= hRec.distance ? vRec : hRec;

            double distance = rec.distance * Math.cos(alpha - player.angle);
            int height = (int) (cellSize / distance * distanceToProjectionPlane);

            int startY = (screenHeight - height) / 2;
            int endY = (screenHeight + height) / 2;
            for (int j = Math.max(0, startY); j < Math.min(screenHeight, endY); ++j) {
                int textureOffset = (vRec.distance <= hRec.distance ? (int) vRec.nextY % cellSize : (int) hRec.nextX % cellSize) + cellSize * rec.wall - cellSize;
                int texel = getTexel(textureOffset, j - startY, height);
                bufferedImage.setRGB(col, j, texel);
            }

            alpha += step;
        }

        g.drawImage(bufferedImage, 0, 0, screenWidth, screenHeight, null);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int dx = 0;
        int dy = 0;
        final int speed = cellSize / 4;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                dx += speed * Math.cos(player.angle);
                dy += speed * Math.sin(player.angle);
                break;
            case KeyEvent.VK_DOWN:
                dx -= speed * Math.cos(player.angle);
                dy -= speed * Math.sin(player.angle);
                break;
            case KeyEvent.VK_LEFT:
                player.angle -= Math.PI / 90;
                break;
            case KeyEvent.VK_RIGHT:
                player.angle += Math.PI / 90;
                break;
            case KeyEvent.VK_R:
                player.x = cellSize + cellSize / 2;
                player.y = cellSize + cellSize / 2;
                player.angle = 0;
                return;
        }

        int x = player.x + dx;
        int y = player.y + dy;

        if (getWallAt(x / cellSize, y / cellSize) > 0)
            return;
        if (getWallAt((x - 1) / cellSize, y / cellSize) > 0)
            return;
        if (getWallAt(x / cellSize, (y - 1) / cellSize) > 0)
            return;
        if (getWallAt((x - 1) / cellSize, (y - 1) / cellSize) > 0)
            return;
        if (getWallAt((x + 1) / cellSize, y / cellSize) > 0)
            return;
        if (getWallAt(x / cellSize, (y + 1) / cellSize) > 0)
            return;
        if (getWallAt((x + 1) / cellSize, (y + 1) / cellSize) > 0)
            return;

        player.x = x;
        player.y = y;
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private HitRecord getVRecord(double alpha) {
        boolean right = Math.cos(alpha) >= 0;
        double nextX = right ? (double) (player.x / cellSize) * cellSize + cellSize : (double) (player.x / cellSize) * cellSize;
        double nextY = player.y + (nextX - player.x) * Math.tan(alpha);
        double stepX = right ? cellSize : -cellSize;
        double stepY = stepX * Math.tan(alpha);
        int wall = 0;
        while (true) {
            int cellX = right ? (int) (nextX / cellSize) : (int) (nextX / cellSize) - 1;
            int cellY = (int) (nextY / cellSize);
            if ((wall = getWallAt(cellX, cellY)) > 0)
                break;
            nextX += stepX;
            nextY += stepY;
        }

        double distance = Math.sqrt(Math.pow(player.x - nextX, 2) + Math.pow(player.y - nextY, 2));
        return new HitRecord(nextX, nextY, distance, wall);
    }

    private HitRecord getHRecord(double alpha) {
        boolean down = Math.sin(alpha) >= 0;
        double nextY = down ? (double) (player.y / cellSize) * cellSize + cellSize : (double) (player.y / cellSize) * cellSize;
        double nextX = player.x + (nextY - player.y) / Math.tan(alpha);
        double stepY = down ? cellSize : -cellSize;
        double stepX = stepY / Math.tan(alpha);
        int wall = 0;
        while (true) {
            int cellY = down ? (int) (nextY / cellSize) : (int) (nextY / cellSize) - 1;
            int cellX = (int) (nextX / cellSize);
            if ((wall = getWallAt(cellX, cellY)) > 0)
                break;
            nextX += stepX;
            nextY += stepY;
        }

        double distance = Math.sqrt(Math.pow(player.x - nextX, 2) + Math.pow(player.y - nextY, 2));
        return new HitRecord(nextX, nextY, distance, wall);
    }

    private static int getWallAt(int x, int y) {
        if (x < 0 || x >= App.map[0].length || y < 0 || y >= App.map.length)
            return 1;
        return App.map[y][x];
    }

    private int getTexel(int offset, int col, double height) {
        return texture.getRGB(offset, (int) (col / height * cellSize));
    }
}
