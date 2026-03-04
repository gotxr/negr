import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class BossLoc extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, foodIcon, heartIcon, waterIcon,
            stakanEmptyImage, stakanFullImage, brevnoImage, appleIcon;
    BufferedImage enemyRight, enemyRightBack, enemyLeft, enemyLeftBack;
    BufferedImage buffer;

    private static final int PLAYER_WIDTH = 110;
    private static final int PLAYER_HEIGHT = 147;

    private static final long ENEMY_SPAWN_INTERVAL = 12000;
    private static final int SWORD_RANGE = PLAYER_WIDTH * 2;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 600;
    private long hitFlashTime = 0;
    private static final long HIT_FLASH_DURATION = 100;
    private long swordFlashTime = 0;
    private static final long SWORD_FLASH_DURATION = 200;

    private Timer gameTimer;

    public static final int mapWidth = 1376;
    public static final int mapHeight = 768;

    double[][] collisionZones = {
            {0.000, 0.000, 0.235, 0.220},
            {0.765, 0.000, 1.000, 0.051},
            {0.815, 0.000, 1.000, 0.220},
            {0.916, 0.000, 1.000, 0.310}
    };

    double[] pPortals = {0.15, 0.178, 0.01, 0.05};

    List<Enemy> enemies = new ArrayList<>();

    //конструктор
    public BossLoc(Player existingPlayer) {
        this.player = existingPlayer;
        init();
    }

    private void init() {
        setFocusable(true);
        addKeyListener(this);

        fon = ImageLoader.loadImage("/assets/boss_location.png");
        rect = ImageLoader.loadImage("/assets/rect1.png");
        heartIcon = ImageLoader.loadImage("/assets/heart_icon.png");
        foodIcon = ImageLoader.loadImage("/assets/food_icon.png");
        waterIcon = ImageLoader.loadImage("/assets/water_icon.png");
        stakanEmptyImage = ImageLoader.loadImage("/assets/stakan_empty.png");
        stakanFullImage = ImageLoader.loadImage("/assets/stakan_full.png");
        enemyRight = ImageLoader.loadImage("/assets/enemyz.png");
        enemyRightBack = ImageLoader.loadImage("/assets/enemy2z.png");
        enemyLeft = ImageLoader.loadImage("/assets/enemy.png");
        enemyLeftBack = ImageLoader.loadImage("/assets/enemy2.png");
        brevnoImage = ImageLoader.loadImage("/assets/brevno.png");
        appleIcon = ImageLoader.loadImage("/assets/apple_icon.png");


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
        player.gameTimer = gameTimer;

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

        // Мигание при уроне
        long now = System.currentTimeMillis();
        if (now - hitFlashTime < HIT_FLASH_DURATION) {
            g2d.setColor(new Color(255, 0, 0, 80));
            g2d.fillRect(offsetX, offsetY, drawWidth, drawHeight);
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

        // Радиус атаки мечом
        Item selectedItem = player.hotbar[player.selectedCol];
        if (selectedItem != null && "sword".equals(selectedItem.type)) {
            // Центр игрока
            int centerX = offsetX + (int)((player.x + PLAYER_WIDTH / 2.0) / mapWidth * drawWidth);
            int centerY = offsetY + (int)((player.y + PLAYER_HEIGHT / 2.0) / mapHeight * drawHeight);

            // Радиус в экранных координатах
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
    }

    boolean isPlayerInPortal(double[][] portals) {
        int pw = PLAYER_WIDTH;
        int ph = PLAYER_HEIGHT;
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

    private void updateGame() {

        if (isPlayerInPortal(new double[][]{pPortals})) {
            player.x = 1000;
            player.y = 210;
            switchToLocation(new LocKamni(player));
            return;
        }

        // Спавн врагов
        if (player.isNight) {
            long now = System.currentTimeMillis();
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
                // Проверяем касание (хитбоксы)
                if (Math.abs(player.x - e.x) < PLAYER_WIDTH &&
                        Math.abs(player.y - e.y) < PLAYER_HEIGHT) {

                    long now = System.currentTimeMillis();
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

    private void spawnEnemy() {
        // Спавним 2 врагов
        for (int i = 0; i < 2; i++) {
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;

            while (attempts < MAX_ATTEMPTS) {
                // размер врага
                int x = (int)(Math.random() * (mapWidth - PLAYER_WIDTH));
                int y = (int)(Math.random() * (mapHeight - PLAYER_HEIGHT));

                if (!checkCollision(x, y)) {
                    enemies.add(new Enemy(x, y));
                    System.out.println("Враг " + (i+1) + " появился на (" + x + ", " + y + ")");
                    break; // выходим из while, переходим к следующему врагу
                }
                attempts++;
            }
            // Если не нашли место - пропускаем этого врага
        }
    }

    private void saveGame() {
        GameSave.save(player, 4); // 4 = босс
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
                else if ("acorn".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/acorn.png");
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
                    else if ("acorn".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/acorn.png");
                    else if ("torch".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/fakel.png");
                    else if ("stone".equals(item.type)) item.icon = ImageLoader.loadImage("/assets/ruda_ugol.png");
                }
            }
        }
    }

/*
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cave");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Player testPlayer = new Player(0, 0);
        testPlayer.image = ImageLoader.loadImage("/assets/pers1.png");
        LocPolana cave = new LocPolana(testPlayer);
        frame.add(cave);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }*/
}



