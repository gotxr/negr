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

        // Рисуем основной инвентарь (3x6)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                int x = startX + col * (slotSize + spacing);
                int y = startY + row * (slotSize + spacing);

                // Рисуем рамку слота
                g.drawImage(slotImage, x, y, slotSize, slotSize, null);

                // Получаем предмет из инвентаря игрока
                Item item = player.inventory[row][col];

                if (item != null && !item.isEmpty()) {
                    // Рисуем иконку предмета
                    g.drawImage(item.icon, x + 18, y + 18, slotSize - 36, slotSize - 36, null);

                    // Рисуем количество в правом нижнем углу
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString(String.valueOf(item.count), x + slotSize - 21, y + slotSize - 8);
                }
            }
        }

        int selX = startX + player.selectedInvCol * (slotSize + spacing);
        int selY = startY + player.selectedInvRow * (slotSize + spacing);
        g.setColor(Color.YELLOW);
        g.drawRect(selX - 2, selY - 2, slotSize + 4, slotSize + 4);

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

        int hotbarSelX = hotbarStartX + player.selectedHotbarSlot * (slotSize + spacing);
        g.setColor(Color.CYAN);
        g.drawRect(hotbarSelX - 2, hotbarY - 2, slotSize + 4, slotSize + 4);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Управление рамкой выбора
        if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
            player.selectedInvRow = Math.max(0, player.selectedInvRow - 1);
        }
        if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
            player.selectedInvRow = Math.min(2, player.selectedInvRow + 1);
        }
        if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
            player.selectedInvCol = Math.max(0, player.selectedInvCol - 1);
        }
        if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            player.selectedInvCol = Math.min(5, player.selectedInvCol + 1);
        }

        /*
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (player.selectedHotbarSlot >= 0) {
            }
        }*/

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // выбран предмет в инвентаре
            Item invItem = player.inventory[player.selectedInvRow][player.selectedInvCol];
            if (invItem != null && !invItem.isEmpty()) {
                // Ищем пустой слот в хотбаре
                int emptySlot = -1;
                for (int i = 0; i < 6; i++) {
                    if (player.hotbar[i] == null || player.hotbar[i].isEmpty()) {
                        emptySlot = i;
                        break;
                    }
                }
                if (emptySlot != -1) {
                    player.hotbar[emptySlot] = invItem;
                    player.inventory[player.selectedInvRow][player.selectedInvCol] = null;
                }
            } else {
                // Выбран пустой слот в инвентаре; проверяем хотбар
                Item hotbarItem = player.hotbar[player.selectedHotbarSlot];
                if (hotbarItem != null && !hotbarItem.isEmpty()) {
                    // Переносим в инвентарь без проверки места
                    player.inventory[player.selectedInvRow][player.selectedInvCol] = hotbarItem;
                    player.hotbar[player.selectedHotbarSlot] = null;
                }
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_E) {
            parentFrame.remove(this);           // Удаляем инвентарь
            parentFrame.add(gamePanel);         // Возвращаем игру
            parentFrame.revalidate();           // Пересчитываем компоновку
            parentFrame.repaint();              // Перерисовываем
            gamePanel.requestFocusInWindow();   // Возвращаем фокус
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}