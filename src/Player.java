import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class Player {
    public int x, y;
    public int health = 100, hunger = 100, thirst = 100;
    public BufferedImage image;

    public Item[][] inventory = new Item[3][6];
    public Item[] hotbar = new Item[6];

    public boolean isNight = false;
    public long dayStartTime = System.currentTimeMillis();
    public static final long DAY_DURATION = 30_000;
    public static final long NIGHT_DURATION = 30_000;
    public long lastHitTime = 0;
    public static final long INVINCIBILITY_DURATION = 1000; // 1 секунда

    public boolean isSelectingHotbar = false;
    public int selectedRow = 0;
    public int selectedCol = 0;

    public int[] appleTreeStages = {0, 0};
    public long[] appleTreeLastUpdateTime = {
            System.currentTimeMillis(),
            System.currentTimeMillis()
    };

    public List<long[]> sprouts = new ArrayList<>();

    public int[][] trees = {
            {300, 400},
            {500, 300},
            {700, 500},
            {200, 200},
            null, null, null
    };

    public int[][] stones = {
            {400, 300},
            {600, 200},
            {800, 400},
            {300, 500},
            null, null, null
    };

    // Таймеры (для паузы)
    public Timer hungerTimer;
    public Timer thirstTimer;
    public Timer healthTimer;
    public Timer dayNightTimer;
    public Timer appleTreeTimer;
    public long pauseStartTime = 0; // время начала паузы
    public long lastEnemySpawnTime = 0;
    public Timer gameTimer = null; // таймер локации

    public boolean addItem(BufferedImage icon, String type, int count) {
        //1 Ищем существующий слот с таким же типом
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                if (inventory[row][col] != null && type.equals(inventory[row][col].type)) {
                    inventory[row][col].count += count;
                    return true;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (hotbar[i] != null && type.equals(hotbar[i].type)) {
                hotbar[i].count += count;
                return true;
            }
        }

        //2 Если не нашли - ищем пустой слот
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                if (inventory[row][col] == null) {
                    inventory[row][col] = new Item(icon, type, count);
                    return true;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (hotbar[i] == null) {
                hotbar[i] = new Item(icon, type, count);
                return true;
            }
        }

        return false; // инвентарь полон
    }

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void setImage(String path) {
        if (image == null) {
            image = ImageLoader.loadImage(path);
        }
    }

    public boolean isReady() {
        return image != null;
    }
    public void setStartPos(int width, int height) {
        // Спавним ТОЛЬКО если размеры уже известны и позиция не задана
        if (width > 0 && height > 0 && x == 0 && y == 0) {
            x = width / 2 - 175;
            y = height / 2 - 300;
        }
    }
}