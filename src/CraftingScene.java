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
        second.pauseTimers(player);

        slotBg = ImageLoader.loadImage("/assets/rect1.png");

        BufferedImage torchIcon = ImageLoader.loadImage("/assets/fakel.png");
        BufferedImage pickaxeIcon = ImageLoader.loadImage("/assets/kirka.png");
        BufferedImage cupIcon = ImageLoader.loadImage("/assets/stakan_empty.png");
        BufferedImage swordIcon = ImageLoader.loadImage("/assets/mech.png");
        BufferedImage axeIcon = ImageLoader.loadImage("/assets/topor.png");
        BufferedImage hilkaIcon = ImageLoader.loadImage("/assets/hilka.png");
        BufferedImage keyIcon = ImageLoader.loadImage("/assets/key.png");

        recipes = new Recipe[] {
                new Recipe("Torch", torchIcon, 1, "wood", 2),
                new Recipe("Pickaxe", pickaxeIcon, 1, "wood", 3),
                new Recipe("Cup", cupIcon, 1, "wood", 2),
                new Recipe("Sword", swordIcon, 1, "wood", 2),
                new Recipe("Axe", axeIcon, 1, "wood", 2),
                new Recipe("Healing Potion", hilkaIcon, 1,
                        new Ingredient("apple", 4),
                        new Ingredient("empty_cup", 1)
                ),
                new Recipe("Key", keyIcon, 1,
                        new Ingredient("wood", 30),
                        new Ingredient("stone", 30),
                        new Ingredient("apple", 20),
                        new Ingredient("empty_cup", 10),
                        new Ingredient("acorn", 15),
                        new Ingredient("torch", 5),
                        new Ingredient("sword", 1),
                        new Ingredient("axe", 1),
                        new Ingredient("pickaxe", 1)
                )
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
        second.resumeTimers(player);
        parentFrame.remove(this);
        parentFrame.add(gamePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        gamePanel.requestFocusInWindow();
    }

    private void craftItem(int recipeIndex) {
        Recipe r = recipes[recipeIndex];
        if (canCraft(r)) {
            consumeItems(r.ingredients);

            // Один общий метод для всех предметов
            player.addItem(r.outputIcon, getItemType(r.name), r.outputAmount);
            System.out.println("Создан " + r.name);
            repaint();
        } else {
            System.out.println("Не хватает ресурсов для " + r.name);
        }
    }

    // Вспомогательный метод
    private String getItemType(String name) {
        switch (name) {
            case "Torch": return "torch";
            case "Pickaxe": return "pickaxe";
            case "Cup": return "empty_cup";
            case "Sword": return "sword";
            case "Axe": return "axe";
            case "Healing Potion": return "healing_potion";
            case "Key": return "key";
            default: return "unknown";
        }
    }

    private boolean canCraft(Recipe r) {
        for (Ingredient ing : r.ingredients) {
            if (getItemCount(ing.type) < ing.amount) {
                return false;
            }
        }
        return true;
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

    private void consumeItems(Ingredient[] ingredients) {
        for (Ingredient ing : ingredients) {
            int remaining = ing.amount;
            // Инвентарь
            for (int row = 0; row < 3 && remaining > 0; row++) {
                for (int col = 0; col < 6 && remaining > 0; col++) {
                    if (player.inventory[row][col] != null && ing.type.equals(player.inventory[row][col].type)) {
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
            // Хотбар
            if (remaining > 0) {
                for (int i = 0; i < 6 && remaining > 0; i++) {
                    if (player.hotbar[i] != null && ing.type.equals(player.hotbar[i].type)) {
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
        int spacing = 25;
        int cols = 3;
        int rows = (int) Math.ceil((double) recipes.length / cols);
        int startX = (getWidth() - (cols * slotSize + (cols - 1) * spacing)) / 2;
        int startY = 180;

        for (int i = 0; i < recipes.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (slotSize + spacing);
            int y = startY + row * (slotSize + 70);

            btnX[i] = x;
            btnY[i] = y;
            btnW[i] = slotSize;
            btnH[i] = slotSize + 60;

            g.setColor(canCraft(recipes[i]) ? Color.GREEN : Color.RED);
            g.drawRect(x - 2, y - 2, slotSize + 4, slotSize + 4);

            g.drawImage(recipes[i].outputIcon, x + 10, y + 10, slotSize - 20, slotSize - 20, null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(recipes[i].name, x, y + slotSize + 20);

            g.setFont(new Font("Arial", Font.PLAIN, 11));
            for (int j = 0; j < recipes[i].ingredients.length; j++) {
                Ingredient ing = recipes[i].ingredients[j];
                g.drawString(ing.amount + "× " + ing.type, x, y + slotSize + 35 + j * 14);
            }
        }
    }
}