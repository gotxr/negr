import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class second extends JPanel implements KeyListener {

    Player player;
    BufferedImage rect, fon, treeImage, brevnoImage, foodIcon, heartIcon, waterIcon;
    BufferedImage buffer;

    double[][] collisionZones= {
            {0.000, 0.000, 0.238, 0.215},
            {0.736, 0.000, 0.781, 0.046},
            {0.798, 0.000, 0.781, 0.202},
            {0.890, 0.000, 0.781, 0.292}
    };

    int[][] trees = {
            {300, 400},
            {500, 300},
            {700, 500},
            {200, 200}
    };

    second() {
        setFocusable(true);
        addKeyListener(this);

        fon = ImageLoader.loadImage("/assets/location1.jpg");
        rect = ImageLoader.loadImage("/assets/rect1.png");
        treeImage = ImageLoader.loadImage("/assets/derevo.png");
        brevnoImage = ImageLoader.loadImage("/assets/brevno.png");
        heartIcon = ImageLoader.loadImage("/assets/heart_icon.png");
        foodIcon = ImageLoader.loadImage("/assets/food_icon.png");
        waterIcon = ImageLoader.loadImage("/assets/water_icon.png");

        player = new Player(0, 0);

        new Timer(1000, e -> {
            player.hunger -= 10;
            if (player.hunger < 0) player.hunger = 0;
            repaint();
        }).start();

        new Timer(1200, e -> {
            player.thirst -= 10;
            if (player.thirst < 0) player.thirst = 0;
            repaint();
        }).start();

        new Timer(2000, e -> {
            if (player.hunger == 0 || player.thirst == 0) {
                player.health -= 10;
                if (player.health < 0) player.health = 0;
            }
            repaint();
        }).start();
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

        g2d.drawImage(fon, 0, 0, getWidth(), getHeight(), null);

        for (int i = 0; i < trees.length; i++) {
            if (trees[i] != null) {
                g2d.drawImage(treeImage, trees[i][0], trees[i][1], null);
            }
        }

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
            g2d.drawImage(heartIcon, 20 + i * (iconSize + spacing), y, iconSize, iconSize, null);
        }

        int hungerIcons = (player.hunger + 19) / 20;
        for (int i = 0; i < hungerIcons; i++) {
            g2d.drawImage(foodIcon, 20 + i * (iconSize + spacing), y + iconSize + 10, iconSize, iconSize, null);
        }

        int thirstIcons = (player.thirst + 19) / 20;
        for (int i = 0; i < thirstIcons; i++) {
            g2d.drawImage(waterIcon, 20 + i * (iconSize + spacing), y + 2 * (iconSize + 10), iconSize, iconSize, null);
        }

        player.setStartPos(getWidth(), getHeight());
        g2d.drawImage(player.image, player.x, player.y, null);

        g2d.dispose();
        g.drawImage(buffer, 0, 0, null);
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
        if (key == KeyEvent.VK_S && player.y < getHeight() - 200) {
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
        if (key == KeyEvent.VK_D && player.x < getWidth() - 130) {
            int newX = player.x + speed;
            int newY = player.y;
            if (!checkCollision(newX, newY)) {
                player.image = ImageLoader.loadImage("/assets/pers1.png");
                player.x += speed;
            }
        }

        if (key == KeyEvent.VK_R) {
            int treeIndex = findNearbyTree();
            if (treeIndex != -1) {
                trees[treeIndex] = null;
                if (player.inventory[0][0] == null) {
                    player.inventory[0][0] = new Item(brevnoImage, 1);
                } else {
                    player.inventory[0][0].count += 1;
                }
                System.out.println("Собрано бревно!");
            }
        }

        if (key == KeyEvent.VK_ENTER) {
            if (player.inventory[0][0] != null && player.inventory[0][0].count > 0) {
                player.inventory[0][0].count -= 1;
                player.hunger += 20;
                if (player.hunger > 100) player.hunger = 100;
                System.out.println("Использована еда! Голод: " + player.hunger);
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

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    boolean checkCollision(int x, int y) {
        int pw = player.image.getWidth();
        int ph = player.image.getHeight();
        int w = getWidth();
        int h = getHeight();

        for (double[] zone : collisionZones) {
            int zx = (int)(zone[0] * w);
            int zy = (int)(zone[1] * h);
            int zw = (int)(zone[2] * w);
            int zh = (int)(zone[3] * h);

            if (x + pw > zx && x < zx + zw &&
                    y + ph > zy && y < zy + zh) {
                return true;
            }
        }
        return false;
    }

    int findNearbyTree() {
        int pw = player.image.getWidth();
        int ph = player.image.getHeight();
        for (int i = 0; i < trees.length; i++) {
            if (trees[i] != null) {
                int tx = trees[i][0];
                int ty = trees[i][1];
                int tw = treeImage.getWidth();
                int th = treeImage.getHeight();
                if (player.x + pw > tx && player.x < tx + tw &&
                        player.y + ph > ty && player.y < ty + th) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Survival Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        second game = new second();
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}