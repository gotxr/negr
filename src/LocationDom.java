import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class LocationDom extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, foodIcon, heartIcon, waterIcon,
            stakanEmptyImage, stakanFullImage, brevnoImage, appleIcon, doorImage;
    BufferedImage buffer;

    private static final int PLAYER_WIDTH = 220;   // 110 * 2
    private static final int PLAYER_HEIGHT = 294;  // 147 * 2

    private Timer gameTimer;

    public static final int mapWidth = 1376;
    public static final int mapHeight = 768;

    double[][] collisionZones = {
            {0.000, 0.000, 1.000, 0.130},
            {0.000, 0.000, 0.100, 1.000},
            {0.000, 0.000, 0.290, 0.500},
    };

    double[] exitPortal = {0.830, 0.050, 0.160, 0.875};

    //конструктор
    public LocationDom(Player existingPlayer) {
        this.player = existingPlayer;
        init();
    }

    private void init() {
        setFocusable(true);
        addKeyListener(this);

        fon = ImageLoader.loadImage("/assets/location_dom.png");
        rect = ImageLoader.loadImage("/assets/rect1.png");
        heartIcon = ImageLoader.loadImage("/assets/heart_icon.png");
        foodIcon = ImageLoader.loadImage("/assets/food_icon.png");
        waterIcon = ImageLoader.loadImage("/assets/water_icon.png");
        stakanEmptyImage = ImageLoader.loadImage("/assets/stakan_empty.png");
        stakanFullImage = ImageLoader.loadImage("/assets/stakan_full.png");
        brevnoImage = ImageLoader.loadImage("/assets/brevno.png");
        appleIcon = ImageLoader.loadImage("/assets/apple_icon.png");
        doorImage = ImageLoader.loadImage("/assets/dver.png"); // ДВЕРЬ

        gameTimer = new Timer(50, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();
        player.gameTimer = gameTimer;

        // Спавн в доме
        if (player.x == 0 && player.y == 0) {
            player.x = 400;
            player.y = 300;
        }

        // В доме ВСЕГДА день — безопасная зона
        player.isNight = false;

        setVisible(true);
        restoreItemIcons();
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

        // Фон дома
        g2d.drawImage(fon, offsetX, offsetY, drawWidth, drawHeight, null);

        // Дверь (портал)
        int doorScreenX = offsetX + (int)(exitPortal[0] * drawWidth);
        int doorScreenY = offsetY + (int)(exitPortal[1] * drawHeight);
        int doorW = (int)(exitPortal[2] * drawWidth);
        int doorH = (int)(exitPortal[3] * drawHeight);
        if (doorImage != null) {
            g2d.drawImage(doorImage, doorScreenX, doorScreenY, doorW, doorH, null);
        }

        // Игрок (×2)
        int drawPlayerW = (int)((PLAYER_WIDTH / (double)mapWidth) * drawWidth);
        int drawPlayerH = (int)((PLAYER_HEIGHT / (double)mapHeight) * drawHeight);
        int playerScreenX = offsetX + (int)((player.x / (double)mapWidth) * drawWidth);
        int playerScreenY = offsetY + (int)((player.y / (double)mapHeight) * drawHeight);
        g2d.drawImage(player.image, playerScreenX, playerScreenY, drawPlayerW, drawPlayerH, null);

        // UI
        drawUI(g2d);

        g2d.dispose();
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawUI(Graphics2D g2d) {
        int slotSize = rect.getWidth();
        int slotSpacing = 30;
        int startY = getHeight() - slotSize - 30;
        int startX = (getWidth() - (6 * slotSize + 5 * slotSpacing)) / 2;

        for (int i = 0; i < 6; i++) {
            int x = startX + i * (slotSize + slotSpacing);
            int y = startY;
            g2d.drawImage(rect, x, y, null);

            Item item = player.hotbar[i];
            if (item != null && !item.isEmpty()) {
                g2d.drawImage(item.icon, x + 18, y + 18, slotSize - 36, slotSize - 36, null);
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString(String.valueOf(item.count), x + slotSize - 21, y + slotSize - 8);
            }
        }

        int selX = startX + player.selectedCol * (slotSize + slotSpacing);
        int selY = startY;
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(selX - 2, selY - 2, slotSize + 4, slotSize + 4);

        // Иконки статуса
        int healthIcons = (player.health + 19) / 20;
        int iconSize = 38;
        int uiSpacing = 5;
        int y = 20;

        for (int i = 0; i < healthIcons; i++) {
            g2d.drawImage(heartIcon, 60 + i * (iconSize + uiSpacing), y, iconSize, iconSize, null);
        }

        int hungerIcons = (player.hunger + 19) / 20;
        for (int i = 0; i < hungerIcons; i++) {
            g2d.drawImage(foodIcon, 60 + i * (iconSize + uiSpacing), y + iconSize + 10, iconSize, iconSize, null);
        }

        int thirstIcons = (player.thirst + 19) / 20;
        for (int i = 0; i < thirstIcons; i++) {
            g2d.drawImage(waterIcon, 60 + i * (iconSize + uiSpacing), y + 2 * (iconSize + 10), iconSize, iconSize, null);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 10;
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
            player.selectedCol = e.getKeyCode() - KeyEvent.VK_1;
            player.isSelectingHotbar = true;
        }

        if (key == KeyEvent.VK_ENTER) {
            Item selectedItem = player.hotbar[player.selectedCol];
            if (selectedItem != null && !selectedItem.isEmpty()) {
                useItem(selectedItem);
            }
        }

        if (key == KeyEvent.VK_ESCAPE) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.remove(this);
            PauseMenu pauseMenu = new PauseMenu(frame, this, player);
            frame.add(pauseMenu);
            frame.revalidate();
            frame.repaint();
            pauseMenu.requestFocusInWindow();
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    boolean isPlayerInPortal(double[][] portals) {
        // Проверяем ТОЛЬКО ЦЕНТР игрока
        int centerX = player.x + PLAYER_WIDTH / 2;
        int centerY = player.y + PLAYER_HEIGHT / 2;

        for (double[] p : portals) {
            int px = (int)(p[0] * mapWidth);
            int py = (int)(p[1] * mapHeight);
            int pwid = (int)(p[2] * mapWidth);
            int phgt = (int)(p[3] * mapHeight);

            if (centerX > px && centerX < px + pwid &&
                    centerY > py && centerY < py + phgt) {
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
        player.lastEnemySpawnTime = System.currentTimeMillis();
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

    private void useItem(Item item) {
        if ("apple".equals(item.type)) {
            player.hunger += 10;
            if (player.hunger > 100) player.hunger = 100;
            item.count -= 1;
            if (item.count <= 0) {
                player.hotbar[player.selectedCol] = null;
            }
        }
        if ("full_cup".equals(item.type)) {
            player.thirst += 10;
            if (player.thirst > 100) player.thirst = 100;
            item.type = "empty_cup";
            item.icon = stakanEmptyImage;
        }
        if ("healing_potion".equals(item.type)) {
            player.health += 20;
            if (player.health > 100) player.health = 100;
            item.count -= 1;
            if (item.count <= 0) {
                player.hotbar[player.selectedCol] = null;
            }
            System.out.println("Вылечились! Здоровье: " + player.health);
        }
    }

    private void updateGame() {
        if (isPlayerInPortal(new double[][]{exitPortal})) {
            player.x = 220;
            player.y = 210;
            switchToLocation(new LocPolana(player));
            return;
        }

        // В доме НЕТ ночи и НЕТ врагов — всегда безопасно
        player.isNight = false;
    }

    private void saveGame() {
        GameSave.save(player, 3); // 3 = дом
    }

    private void restoreItemIcons() {
        for (int i = 0; i < 6; i++) {
            Item item = player.hotbar[i];
            if (item != null && item.icon == null) {
                if ("wood".equals(item.type)) item.icon = brevnoImage;
                else if ("apple".equals(item.type)) item.icon = appleIcon;
                else if ("empty_cup".equals(item.type)) item.icon = stakanEmptyImage;
                else if ("full_cup".equals(item.type)) item.icon = stakanFullImage;
                else if ("sword".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/mech.png");
                else if ("axe".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/topor.png");
                else if ("pickaxe".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/kirka.png");
                else if ("acorn".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/acorn.png");
                else if ("torch".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/fakel.png");
                else if ("stone".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/ruda_ugol.png");
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                Item item = player.inventory[row][col];
                if (item != null && item.icon == null) {
                    if ("wood".equals(item.type)) item.icon = brevnoImage;
                    else if ("apple".equals(item.type)) item.icon = appleIcon;
                    else if ("empty_cup".equals(item.type)) item.icon = stakanEmptyImage;
                    else if ("full_cup".equals(item.type)) item.icon = stakanFullImage;
                    else if ("sword".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/mech.png");
                    else if ("axe".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/topor.png");
                    else if ("pickaxe".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/kirka.png");
                    else if ("acorn".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/acorn.png");
                    else if ("torch".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/fakel.png");
                    else if ("stone".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/ruda_ugol.png");
                }
            }
        }
    }
}