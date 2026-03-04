import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class Inventory extends JPanel implements KeyListener {

    Player player;                  // Ссылка на игрока (для чтения инвентаря)
    BufferedImage slotImage;
    JFrame parentFrame;             // Основное окно
    JPanel gamePanel;               // Ссылка на игровую панель (для возврата)

    // Конструктор
    Inventory(JFrame parent, JPanel game, Player p) {
        this.parentFrame = parent;
        this.gamePanel = game;
        this.player = p;
        second.pauseTimers(player);

        setFocusable(true);
        addKeyListener(this);

        slotImage = ImageLoader.loadImage("/assets/rect1.png");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(40, 40, 40, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Berlin Sans FB", Font.PLAIN, 60));
        g.drawString("Inventory", getWidth() / 2 - 110, 110);

        int slotSize = 100;
        int spacing = 30;
        int startX = (getWidth() - (6 * slotSize + 5 * spacing)) / 2;
        int startY = getHeight() / 2 - 200;

        // Рисуем основной инвентарь
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                int x = startX + col * (slotSize + spacing);
                int y = startY + row * (slotSize + spacing);
                g.drawImage(slotImage, x, y, slotSize, slotSize, null);

                // Получаем предмет из инвентаря игрока
                Item item = player.inventory[row][col];

                if (item != null && !item.isEmpty()) {
                    //иконка
                    g.drawImage(item.icon, x + 18, y + 18, slotSize - 36, slotSize - 36, null);

                    //количество
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString(String.valueOf(item.count), x + slotSize - 21, y + slotSize - 8);
                }
            }
        }

        // хотбар
        int hotbarY = getHeight() - slotSize - 20;
        int hotbarStartX = (getWidth() - (6 * slotSize + 5 * spacing)) / 2;

        for (int i = 0; i < 6; i++) {
            int x = hotbarStartX + i * (slotSize + spacing);
            int y = hotbarY;

            g.drawImage(slotImage, x, y, slotSize, slotSize, null);

            Item item = player.hotbar[i];
            if (item != null && !item.isEmpty()) {
                g.drawImage(item.icon, x + 18, y + 18, slotSize - 36, slotSize - 36, null);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString(String.valueOf(item.count), x + slotSize - 21, y + slotSize - 8);
            }
        }

        //рамка выбора
        g.setColor(Color.YELLOW);
        if (player.isSelectingHotbar) {
            int x = hotbarStartX + player.selectedCol * (slotSize + spacing);
            g.drawRect(x - 2, hotbarY - 2, slotSize + 4, slotSize + 4);
        } else {
            int x = startX + player.selectedCol * (slotSize + spacing);
            int y = startY + player.selectedRow * (slotSize + spacing);
            g.drawRect(x - 2, y - 2, slotSize + 4, slotSize + 4);
        }
    }

    private void transferItem() {
        if (player.isSelectingHotbar) {
            // Выбран слот в хотбаре → переносим в инвентарь
            Item item = player.hotbar[player.selectedCol];
            if (item != null && !item.isEmpty()) {
                // Ищем первый свободный слот в инвентаре (сверху слева)
                boolean placed = false;
                for (int row = 0; row < 3 && !placed; row++) {
                    for (int col = 0; col < 6; col++) {
                        if (player.inventory[row][col] == null || player.inventory[row][col].isEmpty()) {
                            player.inventory[row][col] = item;
                            player.hotbar[player.selectedCol] = null;
                            placed = true;
                            break;
                        }
                    }
                }
            }
        } else {
            // переносим в хотбар
            Item item = player.inventory[player.selectedRow][player.selectedCol];
            if (item != null && !item.isEmpty()) {
                // Ищем пустой слот в хотбаре
                int emptySlot = -1;
                for (int i = 0; i < 6; i++) {
                    if (player.hotbar[i] == null || player.hotbar[i].isEmpty()) {
                        emptySlot = i;
                        break;
                    }
                }
                if (emptySlot != -1) {
                    player.hotbar[emptySlot] = item;
                    player.inventory[player.selectedRow][player.selectedCol] = null;
                }
            }
        }
    }

    private void backToGame() {
        second.resumeTimers(player);
        parentFrame.remove(this);
        parentFrame.add(gamePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        gamePanel.requestFocusInWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            if (player.isSelectingHotbar) {
                player.isSelectingHotbar = false;
            } else {
                player.selectedRow = Math.max(0, player.selectedRow - 1);
            }
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            if (!player.isSelectingHotbar && player.selectedRow == 2) {
                player.isSelectingHotbar = true;
            } else if (!player.isSelectingHotbar) {
                player.selectedRow = Math.min(2, player.selectedRow + 1);
            }
        }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            player.selectedCol = Math.max(0, player.selectedCol - 1);
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            player.selectedCol = Math.min(5, player.selectedCol + 1);
        }

        if (key == KeyEvent.VK_ENTER) {
            transferItem();
        }

        if (key == KeyEvent.VK_E) {
            backToGame();
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}