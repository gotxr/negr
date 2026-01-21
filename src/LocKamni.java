import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class LocKamni extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, stoneImage, stoneItemImage, foodIcon, heartIcon, waterIcon;
    BufferedImage buffer;

    private static final int PLAYER_WIDTH = 110;
    private static final int PLAYER_HEIGHT = 147;
    private static final int STONE_WIDTH = 75;
    private static final int STONE_HEIGHT = 80;

    private boolean isChopping = false;
    private long chopStartTime = 0;
    private static final long CHOP_DURATION = 2000;
    private int choppingStoneIndex = -1;
    private Timer gameTimer;

    public static final int mapWidth = 1376;
    public static final int mapHeight = 768;

    double[][] collisionZones = {
            {0.000, 0.000, 0.235, 0.220},
            {0.765, 0.000, 1.000, 0.051},
            {0.815, 0.000, 1.000, 0.220},
            {0.916, 0.000, 1.000, 0.310}
    };

    public static int[][] stones = {
            {400, 300},
            {600, 200},
            {800, 400},
            {300, 500}
    };

    double[][] exitPortals = {{0.15, 0.178, 0.01, 0.05}};

    //конструктор
    public LocKamni(Player existingPlayer) {
        this.player = existingPlayer;
        init();
    }

    private void init() {
        setFocusable(true);
        addKeyListener(this);

        fon = ImageLoader.loadImage("/assets/lockamni.png");
        rect = ImageLoader.loadImage("/assets/rect1.png");
        stoneImage = ImageLoader.loadImage("/assets/ruda_ugol.png");
        stoneItemImage = ImageLoader.loadImage("/assets/ruda_ugol.png");
        heartIcon = ImageLoader.loadImage("/assets/heart_icon.png");
        foodIcon = ImageLoader.loadImage("/assets/food_icon.png");
        waterIcon = ImageLoader.loadImage("/assets/water_icon.png");

        /*
        if (this.player == null) {
            this.player = new Player(0, 0);
            this.player.image = ImageLoader.loadImage("/assets/pers1.png");
        }*/

        gameTimer = new Timer(50, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();

        // Спавн - только если игрок новый
        if (player.x == 0 && player.y == 0) {
            // Спавн в координатах карты
            player.x = 300;
            player.y = 420;
        }

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight()) {
            buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // === ПРОПОРЦИОНАЛЬНОЕ МАСШТАБИРОВАНИЕ ===
        double targetAspect = (double) second.mapWidth / second.mapHeight;
        double currentAspect = (double) getWidth() / getHeight();

        int drawWidth, drawHeight, offsetX, offsetY;

        if (currentAspect > targetAspect) {
            drawHeight = getHeight();
            drawWidth = (int)(drawHeight * targetAspect);
            offsetX = (getWidth() - drawWidth) / 2;
            offsetY = 0;
        } else {
            drawWidth = getWidth();
            drawHeight = (int)(drawWidth / targetAspect);
            offsetX = 0;
            offsetY = (getHeight() - drawHeight) / 2;
        }

        // Фон
        g2d.drawImage(fon, offsetX, offsetY, drawWidth, drawHeight, null);

        // Камни
        int drawStoneW = (int)((STONE_WIDTH / (double)second.mapWidth) * drawWidth);
        int drawStoneH = (int)((STONE_HEIGHT / (double)second.mapHeight) * drawHeight);
        for (int i = 0; i < stones.length; i++) {
            if (stones[i] != null) {
                int screenX = offsetX + (int)((stones[i][0] / (double)second.mapWidth) * drawWidth);
                int screenY = offsetY + (int)((stones[i][1] / (double)second.mapHeight) * drawHeight);
                g2d.drawImage(stoneImage, screenX, screenY, drawStoneW, drawStoneH, null);
                g2d.drawImage(stoneImage, screenX, screenY, null);
            }
        }

        /*
        for (double[] p : exitPortals) {
            int px = offsetX + (int)(p[0] * drawWidth);
            int py = offsetY + (int)(p[1] * drawHeight);
            int pw = (int)(p[2] * drawWidth);
            int ph = (int)(p[3] * drawHeight);
            g2d.setColor(new Color(100, 255, 100, 100));
            g2d.fillRect(px, py, pw, ph);
        }*/

        // Игрок
        int playerScreenX = offsetX + (int)((player.x / (double)second.mapWidth) * drawWidth);
        int playerScreenY = offsetY + (int)((player.y / (double)second.mapHeight) * drawHeight);
        int drawPlayerW = (int)((PLAYER_WIDTH / (double)second.mapWidth) * drawWidth);
        int drawPlayerH = (int)((PLAYER_HEIGHT / (double)second.mapHeight) * drawHeight);
        g2d.drawImage(player.image, playerScreenX, playerScreenY, drawPlayerW, drawPlayerH, null);

        // Ночь
        if (player.isNight) {
            g2d.setColor(new Color(0, 0, 20, 150));
            g2d.fillRect(offsetX, offsetY, drawWidth, drawHeight);
        }

        // Прогресс-бар добычи
        if (isChopping && choppingStoneIndex != -1 && stones[choppingStoneIndex] != null) {
            int tx = offsetX + (int)((stones[choppingStoneIndex][0] / (double)second.mapWidth) * drawWidth);
            int ty = offsetY + (int)((stones[choppingStoneIndex][1] / (double)second.mapHeight) * drawHeight);
            int tw = (int)((stoneImage.getWidth() / (double)second.mapWidth) * drawWidth);

            long chopElapsed = System.currentTimeMillis() - chopStartTime;
            float progress = Math.min(1.0f, (float) chopElapsed / CHOP_DURATION);
            int barWidth = (int)(tw * progress);

            g2d.setColor(Color.YELLOW);
            g2d.fillRect(tx, ty - 10, barWidth, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(tx, ty - 10, tw, 5);
        }

        drawUI(g2d);

        g2d.dispose();
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawUI(Graphics2D g2d) {
        int slotSize = rect.getWidth();
        int startY = getHeight() - slotSize - 30;
        int startX = (getWidth() - 6 * slotSize) / 2;
        for (int i = 0; i < 6; i++) {
            int x = startX + i * (slotSize + 30);
            int y = startY;
            g2d.drawImage(rect, x, y, null);
        }

        int healthIcons = (player.health + 19) / 20;
        int iconSize = 38;
        int spacing = 5;
        int y = 20;

        for (int i = 0; i < healthIcons; i++) {
            g2d.drawImage(heartIcon, 60 + i * (iconSize + spacing), y, iconSize, iconSize, null);
        }

        int hungerIcons = (player.hunger + 19) / 20;
        for (int i = 0; i < hungerIcons; i++) {
            g2d.drawImage(foodIcon, 60 + i * (iconSize + spacing), y + iconSize + 10, iconSize, iconSize, null);
        }

        int thirstIcons = (player.thirst + 19) / 20;
        for (int i = 0; i < thirstIcons; i++) {
            g2d.drawImage(waterIcon, 60 + i * (iconSize + spacing), y + 2 * (iconSize + 10), iconSize, iconSize, null);
        }


    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 7;
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W && player.y > 0) {
            int newX = player.x;
            int newY = player.y - speed;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers3z.png");
                player.y -= speed;
            }
        }
        if (key == KeyEvent.VK_S && player.y < second.mapHeight - PLAYER_HEIGHT) {
            int newX = player.x;
            int newY = player.y + speed;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers1.png");
                player.y += speed;
            }
        }
        if (key == KeyEvent.VK_A && player.x > 0) {
            int newX = player.x - speed;
            int newY = player.y;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers1z.png");
                player.x -= speed;
            }
        }
        if (key == KeyEvent.VK_D && player.x < second.mapWidth - PLAYER_WIDTH) {
            int newX = player.x + speed;
            int newY = player.y;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers1.png");
                player.x += speed;
            }
        }

        if (key == KeyEvent.VK_R) {
            if (!isChopping) {
                int stoneIndex = findNearbyStone();
                if (stoneIndex != -1) {
                    isChopping = true;
                    chopStartTime = System.currentTimeMillis();
                    choppingStoneIndex = stoneIndex;
                }
            }
        }

        if (key == KeyEvent.VK_ENTER) {}

        if (key == KeyEvent.VK_E) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            Inventory invScene = new Inventory(frame, this, player);
            frame.remove(this);
            frame.add(invScene);
            frame.revalidate();
            frame.repaint();
            invScene.requestFocusInWindow();
        }

        if (key == KeyEvent.VK_C) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            CraftingScene craftScene = new CraftingScene(frame, this, player);
            frame.remove(this);
            frame.add(craftScene);
            frame.revalidate();
            frame.repaint();
            craftScene.requestFocusInWindow();
        }

        if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_6) {
            player.selectedHotbarSlot = e.getKeyCode() - KeyEvent.VK_1;
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            isChopping = false;
            choppingStoneIndex = -1;
        }
    }

    boolean isPlayerInPortal(double[][] portals) {
        // Размер игрока в координатах карты
        int pw = 142;
        int ph = 190;

        for (double[] p : portals) {
            int px = (int)(p[0] * second.mapWidth);
            int py = (int)(p[1] * second.mapHeight);
            int pwid = (int)(p[2] * second.mapWidth);
            int phgt = (int)(p[3] * second.mapHeight);

            if (player.x + pw > px && player.x < px + pwid &&
                    player.y + ph > py && player.y < py + phgt) {
                return true;
            }
        }
        return false;
    }

    void switchToLocation(JPanel newLocation) {
        gameTimer.stop();
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.remove(this);
        frame.add(newLocation);
        frame.revalidate();
        frame.repaint();
        newLocation.requestFocusInWindow();
    }

    boolean checkCollision(int x, int y) {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
        for (double[] zone : collisionZones) {
            int zx1 = (int)(zone[0] * mapWidth);
            int zy1 = (int)(zone[1] * mapHeight);
            int zx2 = (int)(zone[2] * mapWidth);
            int zy2 = (int)(zone[3] * mapHeight);
            int zw = zx2 - zx1;
            int zh = zy2 - zy1;

            if (x + pw > zx1 && x < zx1 + zw &&
                    y + ph > zy1 && y < zy1 + zh) {
                return true;
            }
        }
        return false;
    }

    int findNearbyStone() {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
        for (int i = 0; i < stones.length; i++) {
            if (stones[i] != null) {
                int tx = stones[i][0];
                int ty = stones[i][1];
                if (player.x + pw > tx && player.x < tx + STONE_WIDTH &&
                        player.y + ph > ty && player.y < ty + STONE_HEIGHT) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateGame() {
        if (isPlayerInPortal(exitPortals)) {
                player.x = 1000;
                player.y = 210;
                switchToLocation(new second(player));
                return;
            }

        if (isChopping && choppingStoneIndex != -1 && stones[choppingStoneIndex] != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - chopStartTime;

            if (elapsed >= CHOP_DURATION) {
                stones[choppingStoneIndex] = null;
                player.addItem(stoneItemImage, "stone", 1);
                isChopping = false;
                choppingStoneIndex = -1;
                System.out.println("Камень добыт!");
            }
        }
    }

    /*
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cave");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Player testPlayer = new Player(0, 0);
        testPlayer.image = ImageLoader.loadImage("/assets/pers1.png");
        LocKamni cave = new LocKamni(testPlayer);
        frame.add(cave);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }*/
}