import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class second extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, treeImage, brevnoImage, foodIcon, heartIcon, waterIcon;
    BufferedImage buffer;

    public static final int mapWidth = 1376;
    public static final int mapHeight = 768;

    private boolean isChopping = false;
    private long chopStartTime = 0;
    private static final long CHOP_DURATION = 2000;
    private int choppingTreeIndex = -1;
    private Timer gameTimer;

    private static final int PLAYER_WIDTH = 110;
    private static final int PLAYER_HEIGHT = 147;
    private static final int TREE_WIDTH = 240;
    private static final int TREE_HEIGHT = 272;

    double[][] collisionZones = {
            {0.000, 0.000, 0.235, 0.220},
            {0.765, 0.000, 1.000, 0.051},
            {0.815, 0.000, 1.000, 0.220},
            {0.916, 0.000, 1.000, 0.310}
    };

    public static int[][] trees = {
            {300, 400},
            {500, 300},
            {700, 500},
            {200, 200}
    };

    double[][] cavePortals = {{0.865, 0.188, 0.01, 0.05}}; // x%, y%, w%, h%

    public second(Player existingPlayer) {
        this.player = existingPlayer;
        init();
    }

    public second() {
        init();
    }

    public void init() {
        setFocusable(true);
        addKeyListener(this);

        fon = ImageLoader.loadImage("/assets/location1.jpg");
        rect = ImageLoader.loadImage("/assets/rect1.png");
        treeImage = ImageLoader.loadImage("/assets/derevo.png");
        brevnoImage = ImageLoader.loadImage("/assets/brevno.png");
        heartIcon = ImageLoader.loadImage("/assets/heart_icon.png");
        foodIcon = ImageLoader.loadImage("/assets/food_icon.png");
        waterIcon = ImageLoader.loadImage("/assets/water_icon.png");

        if (this.player == null) {
            this.player = new Player(0, 0);
            this.player.image = ImageLoader.loadImage("/assets/pers1.png");
        }

        gameTimer = new Timer(50, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();

        if (player.x == 0 && player.y == 0) {
            player.x = mapWidth / 2;
            player.y = mapHeight / 2;
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

        double targetAspect = (double) mapWidth / mapHeight;
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

        // Деревья
        int drawTreeW = (int)((TREE_WIDTH / (double)mapWidth) * drawWidth);
        int drawTreeH = (int)((TREE_HEIGHT / (double)mapHeight) * drawHeight);
        for (int i = 0; i < trees.length; i++) {
            if (trees[i] != null) {
                int screenX = offsetX + (int)((trees[i][0] / (double)mapWidth) * drawWidth);
                int screenY = offsetY + (int)((trees[i][1] / (double)mapHeight) * drawHeight);
                g2d.drawImage(treeImage, screenX, screenY, drawTreeW, drawTreeH, null);
            }
        }


        for (double[] p : cavePortals) {
            int px = offsetX + (int)(p[0] * drawWidth);
            int py = offsetY + (int)(p[1] * drawHeight);
            int pw = (int)(p[2] * drawWidth);
            int ph = (int)(p[3] * drawHeight);
            g2d.setColor(new Color(100, 100, 255, 100));
            g2d.fillRect(px, py, pw, ph);
        }


        // Игрок
        int drawPlayerW = (int)((PLAYER_WIDTH / (double)mapWidth) * drawWidth);
        int drawPlayerH = (int)((PLAYER_HEIGHT / (double)mapHeight) * drawHeight);
        int playerScreenX = offsetX + (int)((player.x / (double)mapWidth) * drawWidth);
        int playerScreenY = offsetY + (int)((player.y / (double)mapHeight) * drawHeight);
        g2d.drawImage(player.image, playerScreenX, playerScreenY, drawPlayerW, drawPlayerH, null);;

        // Ночь
        if (player.isNight) {
            g2d.setColor(new Color(0, 0, 20, 150));
            g2d.fillRect(offsetX, offsetY, drawWidth, drawHeight);
        }

        // Прогресс-бар рубки
        if (isChopping && choppingTreeIndex != -1 && trees[choppingTreeIndex] != null) {
            int tx = offsetX + (int)((trees[choppingTreeIndex][0] / (double)mapWidth) * drawWidth);
            int ty = offsetY + (int)((trees[choppingTreeIndex][1] / (double)mapHeight) * drawHeight);
            int tw = drawTreeW;

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
        int spacing = 30; // ← добавь это
        int startY = getHeight() - slotSize - 30;
        int startX = (getWidth() - (6 * slotSize + 5 * spacing)) / 2; // ← обнови формулу

        for (int i = 0; i < 6; i++) {
            int x = startX + i * (slotSize + spacing);
            int y = startY;
            g2d.drawImage(rect, x, y, null);
        }

        int selX = startX + player.selectedHotbarSlot * (slotSize + spacing);
        int selY = startY;
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(selX - 2, selY - 2, slotSize + 4, slotSize + 4);

        int healthIcons = (player.health + 19) / 20;
        int iconSize = 38;
        spacing = 5;
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
        if (key == KeyEvent.VK_S && player.y < mapHeight - PLAYER_HEIGHT) {
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
        if (key == KeyEvent.VK_D && player.x < mapWidth - PLAYER_WIDTH) {
            int newX = player.x + speed;
            int newY = player.y;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers1.png");
                player.x += speed;
            }
        }

        if (key == KeyEvent.VK_R) {
            if (!isChopping) {
                int treeIndex = findNearbyTree();
                if (treeIndex != -1) {
                    isChopping = true;
                    chopStartTime = System.currentTimeMillis();
                    choppingTreeIndex = treeIndex;
                }
            }
        }

        if (key == KeyEvent.VK_ENTER) {
            Item found = null;
            int rowF = -1, colF = -1;
            int hotbarF = -1;

            for (int row = 0; row < 3 && found == null; row++) {
                for (int col = 0; col < 6; col++) {
                    if (player.inventory[row][col] != null && "wood".equals(player.inventory[row][col].type)) {
                        found = player.inventory[row][col];
                        rowF = row; colF = col;
                        break;
                    }
                }
            }
            if (found == null) {
                for (int i = 0; i < 6; i++) {
                    if (player.hotbar[i] != null && "wood".equals(player.hotbar[i].type)) {
                        found = player.hotbar[i];
                        hotbarF = i;
                        break;
                    }
                }
            }

            if (found != null) {
                found.count -= 1;
                if (found.count <= 0) {
                    if (hotbarF != -1) {
                        player.hotbar[hotbarF] = null;
                    } else {
                        player.inventory[rowF][colF] = null;
                    }
                }
                player.hunger += 20;
                if (player.hunger > 100) player.hunger = 100;
                System.out.println("Голод " + player.hunger);
            }
        }

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
            choppingTreeIndex = -1;
        }
    }

    boolean isPlayerInPortal(double[][] portals) {
        int pw = 142;
        int ph = 190;

        for (double[] p : portals) {
            int px = (int)(p[0] * mapWidth);
            int py = (int)(p[1] * mapHeight);
            int pwid = (int)(p[2] * mapWidth);
            int phgt = (int)(p[3] * mapHeight);

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
        int pw = 142;
        int ph = 190;
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

    int findNearbyTree() {
        int pw = 142, ph = 190;
        for (int i = 0; i < trees.length; i++) {
            if (trees[i] != null) {
                int tx = trees[i][0];
                int ty = trees[i][1];
                if (player.x + pw > tx && player.x < tx + TREE_WIDTH &&
                        player.y + ph > ty && player.y < ty + TREE_HEIGHT) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateGame() {
        if (isPlayerInPortal(cavePortals)) {
                player.x = 220;
                player.y = 210;
                switchToLocation(new LocKamni(player));
                return;
            }

        if (isChopping && choppingTreeIndex != -1 && trees[choppingTreeIndex] != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - chopStartTime;

            if (elapsed >= CHOP_DURATION) {
                trees[choppingTreeIndex] = null;
                player.addItem(brevnoImage, "wood", 2);
                isChopping = false;
                choppingTreeIndex = -1;
                System.out.println("Дерево срублено!");
            }
        }
    }

    public static void startGlobalTimers(Player p) {
        new Timer(1000, e -> {
            p.hunger = Math.max(0, p.hunger - 1);
        }).start();

        new Timer(1200, e -> {
            p.thirst = Math.max(0, p.thirst - 1);
        }).start();

        new Timer(2000, e -> {
            if (p.hunger == 0 || p.thirst == 0) {
                p.health = Math.max(0, p.health - 1);
            }
        }).start();

        new Timer(100, e -> {
            long now = System.currentTimeMillis();
            long elapsed = now - p.dayStartTime;
            if (p.isNight) {
                if (elapsed >= Player.NIGHT_DURATION) {
                    p.isNight = false;
                    p.dayStartTime = now;
                }
            } else {
                if (elapsed >= Player.DAY_DURATION) {
                    p.isNight = true;
                    p.dayStartTime = now;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Survival Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Player globalPlayer = new Player(0, 0);
        globalPlayer.image = ImageLoader.loadImage("/assets/pers1.png");
        second.startGlobalTimers(globalPlayer);

        second game = new second(globalPlayer);
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}