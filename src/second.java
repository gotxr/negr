import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class second extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, treeImage, brevnoImage, foodIcon, heartIcon,
            waterIcon, appleIcon, stakanEmptyImage, stakanFullImage,
            appleTreeFull, appleTreeHalf, appleTreeEmpty, acornImage, sproutImage;
    BufferedImage enemyRight, enemyRightBack, enemyLeft, enemyLeftBack, swordIcon;
    BufferedImage buffer;

    public static final int mapWidth = 1376;
    public static final int mapHeight = 768;

    private boolean isChopping = false;
    private long chopStartTime = 0;
    private static final long CHOP_DURATION_WOOD = 5000;
    private static final long CHOP_DURATION_AXE = 2000;
    private long currentChopDuration = CHOP_DURATION_WOOD;
    private int choppingTreeIndex = -1;
    private Timer gameTimer;

    private static final int PLAYER_WIDTH = 110;
    private static final int PLAYER_HEIGHT = 147;
    private static final int TREE_WIDTH = 240;
    private static final int TREE_HEIGHT = 272;
    private static final int SPROUT_WIDTH = 100;
    private static final int SPROUT_HEIGHT = 100;

    private static final long ENEMY_SPAWN_INTERVAL = 12000;
    private static final int SWORD_RANGE = PLAYER_WIDTH * 2;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 600;
    private long hitFlashTime = 0;
    private static final long HIT_FLASH_DURATION = 100;
    private long swordFlashTime = 0;
    private static final long SWORD_FLASH_DURATION = 200;

    int[][] appleTreePositions = {
            {1000, 300},
            {600, 50},
    };

    double[][] collisionZones = {
            {0.000, 0.000, 0.235, 0.220},
            {0.765, 0.000, 1.000, 0.051},
            {0.815, 0.000, 1.000, 0.220},
            {0.900, 0.000, 1.000, 0.310}
    };

    double[] cavePortals = {0.865, 0.188, 0.01, 0.05};
    double[] polanaPortals = {0.15, 0.178, 0.01, 0.05};

    List<Enemy> enemies = new ArrayList<>();

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
        appleTreeFull = ImageLoader.loadImage("/assets/apple_tree_full.png");
        appleTreeHalf = ImageLoader.loadImage("/assets/apple_tree_half.png");
        appleTreeEmpty = ImageLoader.loadImage("/assets/apple_tree_empty.png");
        appleIcon = ImageLoader.loadImage("/assets/apple_icon.png");
        stakanEmptyImage = ImageLoader.loadImage("/assets/stakan_empty.png");
        stakanFullImage = ImageLoader.loadImage("/assets/stakan_full.png");
        acornImage = ImageLoader.loadImage("/assets/acorn.png");
        sproutImage = ImageLoader.loadImage("/assets/sprout.png");
        enemyRight = ImageLoader.loadImage("/assets/enemyz.png");
        enemyRightBack = ImageLoader.loadImage("/assets/enemy2z.png");
        enemyLeft = ImageLoader.loadImage("/assets/enemy.png");
        enemyLeftBack = ImageLoader.loadImage("/assets/enemy2.png");
        swordIcon = ImageLoader.loadImage("/assets/mech.png");

        if (this.player == null) {
            this.player = new Player(0, 0);
            this.player.image = ImageLoader.loadImage("/assets/pers1.png");
        }

        gameTimer = new Timer(50, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();
        player.gameTimer = gameTimer;

        if (player.x == 0 && player.y == 0) {
            player.x = mapWidth / 2;
            player.y = mapHeight / 2;
        }

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

        // Фон
        g2d.drawImage(fon, offsetX, offsetY, drawWidth, drawHeight, null);

        // Мигание при уроне
        long now = System.currentTimeMillis();
        if (now - hitFlashTime < HIT_FLASH_DURATION) {
            g2d.setColor(new Color(255, 0, 0, 80));
            g2d.fillRect(offsetX, offsetY, drawWidth, drawHeight);
        }

        // Деревья
        int drawTreeW = (int)((TREE_WIDTH / (double)mapWidth) * drawWidth);
        int drawTreeH = (int)((TREE_HEIGHT / (double)mapHeight) * drawHeight);
        for (int i = 0; i < player.trees.length; i++) {
            if (player.trees[i] != null) {
                int screenX = offsetX + (int)((player.trees[i][0] / (double)mapWidth) * drawWidth);
                int screenY = offsetY + (int)((player.trees[i][1] / (double)mapHeight) * drawHeight);
                g2d.drawImage(treeImage, screenX, screenY, drawTreeW, drawTreeH, null);
            }
        }

        //Ростки
        int drawSproutW = (int)((SPROUT_WIDTH / (double)mapWidth) * drawWidth);
        int drawSproutH = (int)((SPROUT_HEIGHT / (double)mapHeight) * drawHeight);
        for (long[] sprout : player.sprouts) {
            int screenX = offsetX + (int)((sprout[0] / (double)mapWidth) * drawWidth);
            int screenY = offsetY + (int)((sprout[1] / (double)mapHeight) * drawHeight);
            g2d.drawImage(sproutImage, screenX, screenY, drawSproutW, drawSproutH, null);
        }

        // Яблони
        int drawAppleW = (int)((TREE_WIDTH / (double)mapWidth) * drawWidth);
        int drawAppleH = (int)((TREE_HEIGHT / (double)mapHeight) * drawHeight);
        for (int i = 0; i < appleTreePositions.length; i++) {
            if (appleTreePositions[i] != null) {
                int screenX = offsetX + (int)((appleTreePositions[i][0] / (double)mapWidth) * drawWidth);
                int screenY = offsetY + (int)((appleTreePositions[i][1] / (double)mapHeight) * drawHeight);

                BufferedImage img;
                if (player.appleTreeStages[i] == 0) {
                    img = appleTreeFull;
                } else if (player.appleTreeStages[i] == 1) {
                    img = appleTreeHalf;
                } else {
                    img = appleTreeEmpty;
                }

                g2d.drawImage(img, screenX, screenY, drawAppleW, drawAppleH, null);
            }
        }

/*
        // Визуализация порталов (временно для отладки)
        int px1 = offsetX + (int)(cavePortals[0] * drawWidth);
        int py1 = offsetY + (int)(cavePortals[1] * drawHeight);
        int pw1 = (int)(cavePortals[2] * drawWidth);
        int ph1 = (int)(cavePortals[3] * drawHeight);
        g2d.setColor(new Color(0, 255, 0, 100)); // зелёный — пещера
        g2d.fillRect(px1, py1, pw1, ph1);

        int px2 = offsetX + (int)(polanaPortals[0] * drawWidth);
        int py2 = offsetY + (int)(polanaPortals[1] * drawHeight);
        int pw2 = (int)(polanaPortals[2] * drawWidth);
        int ph2 = (int)(polanaPortals[3] * drawHeight);
        g2d.setColor(new Color(0, 100, 255, 100)); // синий — поляна
        g2d.fillRect(px2, py2, pw2, ph2);*/

        // Враги
        for (Enemy e : enemies) {
            int ex = e.x;
            int ey = e.y;

            // Вектор к игроку
            int dx = player.x - ex;
            int dy = player.y - ey;

            BufferedImage enemySprite;

            if (Math.abs(dx) > Math.abs(dy)) {
                // Смотрит по горизонтали
                if (dx > 0) {
                    // Игрок справа - враг вправо
                    // Определяем: лицо или спина?
                    if (dy > 0) {
                        // Игрок ниже - враг вперёд
                        enemySprite = enemyRight;
                    } else {
                        // Игрок выше - враг назад
                        enemySprite = enemyRightBack;
                    }
                } else {
                    // Игрок слева - враг влево
                    if (dy > 0) {
                        enemySprite = enemyLeft;
                    } else {
                        enemySprite = enemyLeftBack;
                    }
                }
            } else {
                // Смотрит по вертикали - решаем по X
                if (dx > 0) {
                    // Игрок справа - враг вправо
                    if (dy > 0) {
                        enemySprite = enemyRight;
                    } else {
                        enemySprite = enemyRightBack;
                    }
                } else {
                    // Игрок слева - враг влево
                    if (dy > 0) {
                        enemySprite = enemyLeft;
                    } else {
                        enemySprite = enemyLeftBack;
                    }
                }
            }
            int screenX = offsetX + (int)((ex / (double)mapWidth) * drawWidth);
            int screenY = offsetY + (int)((ey / (double)mapHeight) * drawHeight);
            int drawW = (int)((PLAYER_WIDTH / (double)mapWidth) * drawWidth);
            int drawH = (int)((PLAYER_HEIGHT / (double)mapHeight) * drawHeight);
            g2d.drawImage(enemySprite, screenX, screenY, drawW, drawH, null);

            // Индикатор здоровья врага
            int barWidth = drawW;
            int barHeight = 5;
            int barX = screenX;
            int barY = screenY - 10;

            g2d.setColor(Color.RED);
            g2d.fillRect(barX, barY, barWidth, barHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(barX, barY, barWidth, barHeight);

            // Зелёная часть (здоровье)
            int currentHealthWidth = (int)((e.health / 2.0) * barWidth); // 2 — максимум
            g2d.setColor(Color.GREEN);
            g2d.fillRect(barX, barY, currentHealthWidth, barHeight);
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
        if (isChopping && choppingTreeIndex != -1 && player.trees[choppingTreeIndex] != null) {
            int tx = offsetX + (int)((player.trees[choppingTreeIndex][0] / (double)mapWidth) * drawWidth);
            int ty = offsetY + (int)((player.trees[choppingTreeIndex][1] / (double)mapHeight) * drawHeight);
            int tw = drawTreeW;

            long chopElapsed = System.currentTimeMillis() - chopStartTime;
            float progress = Math.min(1.0f, (float) chopElapsed / currentChopDuration);
            int barWidth = (int)(tw * progress);

            g2d.setColor(Color.YELLOW);
            g2d.fillRect(tx, ty - 10, barWidth, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(tx, ty - 10, tw, 5);
        }

        // Радиус атаки мечом
        Item selectedItem = player.hotbar[player.selectedCol];
        if (selectedItem != null && "sword".equals(selectedItem.type)) {
            // Центр игрока
            int centerX = offsetX + (int)((player.x + PLAYER_WIDTH / 2.0) / mapWidth * drawWidth);
            int centerY = offsetY + (int)((player.y + PLAYER_HEIGHT / 2.0) / mapHeight * drawHeight);

            int screenRange = (int)((SWORD_RANGE / (double)mapWidth) * drawWidth);

            // Проверяем, идёт ли анимация удара
            now = System.currentTimeMillis();
            boolean isFlashing = (now - swordFlashTime < SWORD_FLASH_DURATION);

            if (isFlashing) {
                // Анимация удара
                g2d.setColor(new Color(255, 255, 0, 180)); // почти непрозрачный
                g2d.setStroke(new BasicStroke(4));
            } else {
                // Обычный радиус
                g2d.setColor(new Color(255, 255, 0, 80)); // полупрозрачный
                g2d.setStroke(new BasicStroke(2));
            }

            g2d.drawOval(centerX - screenRange, centerY - screenRange, screenRange * 2, screenRange * 2);
        }

        drawUI(g2d);

        g2d.dispose();
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawUI(Graphics2D g2d) {
        int slotSize = rect.getWidth();
        int slotSpacing = 30;
        int startY = getHeight() - slotSize - 30;
        int startX = (getWidth() - (6 * slotSize + 5 * slotSpacing)) / 2;

        // слоты хотбара и предметы
        for (int i = 0; i < 6; i++) {
            int x = startX + i * (slotSize + slotSpacing);
            int y = startY;
            g2d.drawImage(rect, x, y, null);

            Item item = player.hotbar[i];
            if (item != null && !item.isEmpty()) {
                // Иконка по центру слота
                g2d.drawImage(item.icon, x + 18, y + 18, slotSize - 36, slotSize - 36, null);

                // Количество в правом нижнем углу
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString(String.valueOf(item.count), x + slotSize - 21, y + slotSize - 8);
            }
        }

        // Рамка вокруг выбранного слота
        int selX = startX + player.selectedCol * (slotSize + slotSpacing);
        int selY = startY;
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(selX - 2, selY - 2, slotSize + 4, slotSize + 4);

        // Здоровье, голод, жажда
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

                    // Определяем длительность
                    Item selectedItem = player.hotbar[player.selectedCol];
                    if (selectedItem != null && "axe".equals(selectedItem.type)) {
                        currentChopDuration = CHOP_DURATION_AXE;
                    } else {
                        currentChopDuration = CHOP_DURATION_WOOD;
                    }
                }}

            int appleTreeIndex = findNearbyAppleTree();
            if (appleTreeIndex != -1) {
                if (player.appleTreeStages[appleTreeIndex] == 0) {
                    player.addItem(appleIcon, "apple", 1);
                    player.appleTreeStages[appleTreeIndex] = 1;
                    player.appleTreeLastUpdateTime[appleTreeIndex] = System.currentTimeMillis();
                    System.out.println("Собрали 2 яблока!");
                } else if (player.appleTreeStages[appleTreeIndex] == 1) {
                    player.addItem(appleIcon, "apple", 1);
                    player.appleTreeStages[appleTreeIndex] = 2;
                    player.appleTreeLastUpdateTime[appleTreeIndex] = System.currentTimeMillis();
                }
            }

            Item selectedItem = player.hotbar[player.selectedCol];
            if (selectedItem != null && "acorn".equals(selectedItem.type)) {
                int sproutX = player.x + PLAYER_WIDTH / 2;
                int sproutY = player.y + PLAYER_HEIGHT / 2;
                player.sprouts.add(new long[]{sproutX, sproutY, System.currentTimeMillis()});

                selectedItem.count--;
                if (selectedItem.count <= 0) {
                    player.hotbar[player.selectedCol] = null;
                }
                System.out.println("Посажен росток!");
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

        if (key == KeyEvent.VK_F) {
            Item selectedItem = player.hotbar[player.selectedCol];
            if (selectedItem != null && "sword".equals(selectedItem.type)) {
                attackWithSword();
            }
        }

        if (key == KeyEvent.VK_Y) {
            saveGame();
        }

        if (key == KeyEvent.VK_ESCAPE) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.remove(this); // ← УДАЛЯЕМ текущую панель
            PauseMenu pauseMenu = new PauseMenu(frame, this, player);
            frame.add(pauseMenu); // ← ДОБАВЛЯЕМ паузу
            frame.revalidate();
            frame.repaint();
            pauseMenu.requestFocusInWindow();
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

    private void useItem(Item item) {
        if ("apple".equals(item.type)) {
            player.hunger += 10;
            if (player.hunger > 100) player.hunger = 100;
            item.count -= 1;
            if (item.count <= 0) {
                player.hotbar[player.selectedCol] = null;
            }
            System.out.println("Съели яблоко! Голод: " + player.hunger);
        }

        if ("full_cup".equals(item.type)) {
            player.thirst += 10;
            if (player.thirst > 100) player.thirst = 100;
            // Делаем стакан пустым
            item.type = "empty_cup";
            item.icon = stakanEmptyImage;
            System.out.println("Выпили воды! Жажда: " + player.thirst);
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

    boolean isPlayerInPortal(double[][] portals) {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
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
        player.lastEnemySpawnTime = System.currentTimeMillis();
        // Спавн врагов сразу при входе ночью
        if (player.isNight) {
            player.lastEnemySpawnTime = System.currentTimeMillis() - ENEMY_SPAWN_INTERVAL;
        }
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

    int findNearbyTree() {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
        for (int i = 0; i < player.trees.length; i++) {
            if (player.trees[i] != null) {
                int tx = player.trees[i][0];
                int ty = player.trees[i][1];
                if (player.x + pw > tx && player.x < tx + TREE_WIDTH &&
                        player.y + ph > ty && player.y < ty + TREE_HEIGHT) {
                    return i;
                }
            }
        }
        return -1;
    }

    int findNearbyAppleTree() {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
        for (int i = 0; i < appleTreePositions.length; i++) {
            if (appleTreePositions[i] != null) {
                int tx = appleTreePositions[i][0];
                int ty = appleTreePositions[i][1];
                if (player.x + pw > tx && player.x < tx + TREE_WIDTH &&
                        player.y + ph > ty && player.y < ty + TREE_HEIGHT) {
                    // Проверяем, не пустая ли яблоня
                    if (player.appleTreeStages[i] < 2) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void updateGame() {
        if (isPlayerInPortal(new double[][]{cavePortals})) {
            player.x = 220;
            player.y = 210;
            switchToLocation(new LocKamni(player));
            return;
        }

        if (isPlayerInPortal(new double[][]{polanaPortals})) {
            player.x = 1000;
            player.y = 210;
            switchToLocation(new LocPolana(player));
            return;
        }

        if (isChopping && choppingTreeIndex != -1 && player.trees[choppingTreeIndex] != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - chopStartTime;

            if (elapsed >= currentChopDuration) {
                player.trees[choppingTreeIndex] = null;
                player.addItem(brevnoImage, "wood", 2);
                player.addItem(acornImage, "acorn", 1);
                isChopping = false;
                choppingTreeIndex = -1;
                System.out.println("Дерево срублено!");
            }
        }

        long now = System.currentTimeMillis();
        for (int i = player.sprouts.size() - 1; i >= 0; i--) {
            long[] sprout = player.sprouts.get(i);
            if (now - sprout[2] >= 4 * Player.DAY_DURATION) {
                for (int j = 0; j < player.trees.length; j++) {
                    if (player.trees[j] == null) {
                        player.trees[j] = new int[]{(int)sprout[0], (int)sprout[1]};
                        player.sprouts.remove(i);
                        System.out.println("Росток вырос в дерево!");
                        break;
                    }
                }
            }
        }

        // Спавн врагов
        if (player.isNight) {
            now = System.currentTimeMillis();
            if (now - player.lastEnemySpawnTime >= ENEMY_SPAWN_INTERVAL) {
                spawnEnemy();
                player.lastEnemySpawnTime = now;
            }
        } else {
            if (!enemies.isEmpty()) {
                enemies.clear();
                System.out.println("Враги исчезли с рассветом.");
            }
        }

        // Движение врагов
        if (player.isNight) {
            for (Enemy e : enemies) {
                int ex = e.x;
                int ey = e.y;

                // Вектор к игроку
                int dx = player.x - ex;
                int dy = player.y - ey;
                int speed = 3;

                // Нормализация
                if (Math.abs(dx) > Math.abs(dy)) {
                    // Движение по X
                    if (dx > 0) ex += speed; // вправо
                    else ex -= speed;        // влево
                } else {
                    // Движение по Y
                    if (dy > 0) ey += speed; // вниз
                    else ey -= speed;        // вверх
                }

                // Проверка коллизии со стенами
                if (!checkCollision(ex, ey)) {
                    e.x = ex;
                    e.y = ey;
                }
            }
        }

        // Проверка урона от врагов
        if (player.isNight) {
            for (Enemy e : enemies) {
                // Проверяем касание хитбоксов
                if (Math.abs(player.x - e.x) < PLAYER_WIDTH &&
                        Math.abs(player.y - e.y) < PLAYER_HEIGHT) {

                    now = System.currentTimeMillis();
                    if (now - player.lastHitTime >= Player.INVINCIBILITY_DURATION) {
                        player.health -= 10;
                        if (player.health < 0) player.health = 0;
                        player.lastHitTime = now;
                        hitFlashTime = now; // запускаем мигание
                        System.out.println("Получен урон! Здоровье: " + player.health);
                    }
                }
            }
        }
    }

    private void spawnEnemy() {
        // Спавним 2 врагов
        for (int i = 0; i < 2; i++) {
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;

            while (attempts < MAX_ATTEMPTS) {
                int x = (int)(Math.random() * (mapWidth - PLAYER_WIDTH));
                int y = (int)(Math.random() * (mapHeight - PLAYER_HEIGHT));

                if (!checkCollision(x, y)) {
                    enemies.add(new Enemy(x, y));
                    System.out.println("Враг " + (i+1) + " появился на (" + x + ", " + y + ")");
                    break; // выходим из while, переходим к следующему врагу
                }
                attempts++;
            }
            // Если не нашли место - скип
        }
    }

    private void attackWithSword() {
        long now = System.currentTimeMillis();
        if (now - lastAttackTime < ATTACK_COOLDOWN) {
            return; // ещё нельзя атаковать
        }
        lastAttackTime = now;
        swordFlashTime = now;

        // Центр игрока
        int playerCenterX = player.x + PLAYER_WIDTH / 2;
        int playerCenterY = player.y + PLAYER_HEIGHT / 2;

        for (Enemy e : enemies) {
            // Центр врага
            int enemyCenterX = e.x + PLAYER_WIDTH / 2;
            int enemyCenterY = e.y + PLAYER_HEIGHT / 2;

            // Расстояние между центрами
            double dx = playerCenterX - enemyCenterX;
            double dy = playerCenterY - enemyCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= SWORD_RANGE) {
                e.health -= 1;
                System.out.println("Враг ранен! Осталось HP: " + e.health);
                if (e.health <= 0) {
                    enemies.remove(e);
                    System.out.println("Враг убит!");
                }
                break; // один удар - один враг
            }
        }
    }

    private void saveGame() {
        GameSave.save(player, 0);
    }

    private void restoreItemIcons() {
        // Хотбар
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
                else if ("acorn".equals(item.type)) item.icon = acornImage;
                else if ("torch".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/fakel.png");
                else if ("stone".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/ruda_ugol.png");
            }
        }

        // Инвентарь
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
                    else if ("acorn".equals(item.type)) item.icon = acornImage;
                    else if ("torch".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/fakel.png");
                    else if ("stone".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/ruda_ugol.png");
                }
            }
        }
    }

    public static void startGlobalTimers(Player p) {
        // Голод
        p.hungerTimer = new Timer(1000, e -> {
            p.hunger = Math.max(0, p.hunger - 1);
        });
        p.hungerTimer.start();

        // Жажда
        p.thirstTimer = new Timer(1200, e -> {
            p.thirst = Math.max(0, p.thirst - 1);
        });
        p.thirstTimer.start();

        // Здоровье
        p.healthTimer = new Timer(2000, e -> {
            if (p.hunger == 0 || p.thirst == 0) {
                p.health = Math.max(0, p.health - 1);
            }
        });
        p.healthTimer.start();

        // День/ночь
        p.dayNightTimer = new Timer(100, e -> {
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
        });
        p.dayNightTimer.start();

        // Яблони
        p.appleTreeTimer = new Timer(1000, e -> {
            long now = System.currentTimeMillis();
            for (int i = 0; i < p.appleTreeStages.length; i++) {
                if (p.appleTreeStages[i] > 0) {
                    if (now - p.appleTreeLastUpdateTime[i] >= Player.DAY_DURATION) {
                        p.appleTreeStages[i]--;
                        p.appleTreeLastUpdateTime[i] = now;
                    }
                }
            }
        });
        p.appleTreeTimer.start();
    }

    public static void pauseTimers(Player p) {
        p.pauseStartTime = System.currentTimeMillis();
        if (p.hungerTimer != null) p.hungerTimer.stop();
        if (p.thirstTimer != null) p.thirstTimer.stop();
        if (p.healthTimer != null) p.healthTimer.stop();
        if (p.dayNightTimer != null) p.dayNightTimer.stop();
        if (p.appleTreeTimer != null) p.appleTreeTimer.stop();
        if (p.gameTimer != null) p.gameTimer.stop();
    }

    public static void resumeTimers(Player p) {
        long pauseDuration = System.currentTimeMillis() - p.pauseStartTime;

        // Корректируем время начала дня/ночи
        p.dayStartTime += pauseDuration;

        // Корректируем время обновления яблонь
        for (int i = 0; i < p.appleTreeLastUpdateTime.length; i++) {
            p.appleTreeLastUpdateTime[i] += pauseDuration;
        }

        // Корректируем спавн врагов
        p.lastEnemySpawnTime += pauseDuration;

        // Возобновляем таймеры
        if (p.hungerTimer != null) p.hungerTimer.start();
        if (p.thirstTimer != null) p.thirstTimer.start();
        if (p.healthTimer != null) p.healthTimer.start();
        if (p.dayNightTimer != null) p.dayNightTimer.start();
        if (p.appleTreeTimer != null) p.appleTreeTimer.start();
        if (p.gameTimer != null) p.gameTimer.start();
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