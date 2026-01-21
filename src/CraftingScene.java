import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class CraftingScene extends JPanel {

    JFrame parentFrame;
    JPanel gamePanel;
    Player player;

    Recipe[] recipes;
    BufferedImage slotBg;

    int[] btnX, btnY, btnW, btnH;

    public CraftingScene(JFrame parent, JPanel game, Player p) {
        this.parentFrame = parent;
        this.gamePanel = game;
        this.player = p;

        slotBg = ImageLoader.loadImage("/assets/rect1.png");

        BufferedImage torchIcon = ImageLoader.loadImage("/assets/fakel.png");
        BufferedImage pickaxeIcon = ImageLoader.loadImage("/assets/kirka.png");

        recipes = new Recipe[] {
                new Recipe("Torch", torchIcon, 1, "wood", 2),
                new Recipe("Pickaxe", pickaxeIcon, 1, "wood", 3)
        };

        btnX = new int[recipes.length];
        btnY = new int[recipes.length];
        btnW = new int[recipes.length];
        btnH = new int[recipes.length];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                for (int i = 0; i < recipes.length; i++) {
                    if (mx >= btnX[i] && mx <= btnX[i] + btnW[i] &&
                            my >= btnY[i] && my <= btnY[i] + btnH[i]) {
                        craftItem(i);
                        break;
                    }
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_C) {
                    closeCrafting();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    private void closeCrafting() {
        parentFrame.remove(this);
        parentFrame.add(gamePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        gamePanel.requestFocusInWindow();
    }

    private void craftItem(int recipeIndex) {
        Recipe r = recipes[recipeIndex];
        if (canCraft(r)) {
            consumeItems(r.ingredient, r.amount);

            if ("Torch".equals(r.name)) {
                player.addItem(r.outputIcon, "torch", r.outputAmount);
            } else if ("Pickaxe".equals(r.name)) {
                player.addItem(r.outputIcon, "pickaxe", r.outputAmount);
            }

            System.out.println("Создан " + r.name);
            repaint();
        } else {
            System.out.println("Не достаточно ресурсов для " + r.name);
        }
    }

    private boolean canCraft(Recipe r) {
        return getItemCount(r.ingredient) >= r.amount;
    }

    private int getItemCount(String type) {
        int count = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                if (player.inventory[row][col] != null && type.equals(player.inventory[row][col].type)) {
                    count += player.inventory[row][col].count;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (player.hotbar[i] != null && type.equals(player.hotbar[i].type)) {
                count += player.hotbar[i].count;
            }
        }
        return count;
    }

    private void consumeItems(String type, int count) {
        int remaining = count;

        // основной инвент
        for (int row = 0; row < 3 && remaining > 0; row++) {
            for (int col = 0; col < 6 && remaining > 0; col++) {
                if (player.inventory[row][col] != null && type.equals(player.inventory[row][col].type)) {
                    if (player.inventory[row][col].count <= remaining) {
                        remaining -= player.inventory[row][col].count;
                        player.inventory[row][col] = null;
                    } else {
                        player.inventory[row][col].count -= remaining;
                        remaining = 0;
                    }
                }
            }
        }

        // дотрата ресурсов из хотбара
        if (remaining > 0) {
            for (int i = 0; i < 6 && remaining > 0; i++) {
                if (player.hotbar[i] != null && type.equals(player.hotbar[i].type)) {
                    if (player.hotbar[i].count <= remaining) {
                        remaining -= player.hotbar[i].count;
                        player.hotbar[i] = null;
                    } else {
                        player.hotbar[i].count -= remaining;
                        remaining = 0;
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(40, 40, 40, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Berlin Sans FB", Font.PLAIN, 60));
        g.drawString("Crafting", getWidth() / 2 - 110, 110);

        int slotSize = 100;
        int spacing = 20;
        int startX = (getWidth() - (recipes.length * slotSize + (recipes.length - 1) * spacing)) / 2;
        int startY = 180;

        for (int i = 0; i < recipes.length; i++) {
            int x = startX + i * (slotSize + spacing);
            int y = startY;
            btnX[i] = x;
            btnY[i] = y;
            btnW[i] = slotSize;
            btnH[i] = slotSize + 40;

            if (canCraft(recipes[i])) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            g.drawRect(x, y, slotSize, slotSize);

            g.drawImage(recipes[i].outputIcon, x + 10, y + 10, slotSize - 20, slotSize - 20, null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString(recipes[i].name, x, y + slotSize + 20);
            g.drawString(recipes[i].amount + " x wood", x, y + slotSize + 35);
        }
    }
}