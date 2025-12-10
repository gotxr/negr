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

        g.setColor(new Color(50, 50, 50, 100));
        g.fillRect(0, 0, getWidth(), getHeight());

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

        // Рисуем горячую панель (6 слотов внизу)
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
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Закрытие инвентаря по E
        if (e.getKeyCode() == KeyEvent.VK_E) {
            parentFrame.remove(this);           // Удаляем инвентарь
            parentFrame.add(gamePanel);         // Возвращаем игру
            parentFrame.revalidate();           // Пересчитываем компоновку
            parentFrame.repaint();              // Перерисовываем
            gamePanel.requestFocusInWindow();   // Возвращаем фокус
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}