import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PauseMenu extends JPanel {

    Player player;
    JFrame parentFrame;
    JPanel gamePanel;
    BufferedImage menu1, menu2, menu3;
    int buttonWidth, buttonHeight;

    public PauseMenu(JFrame parent, JPanel game, Player p) {
        this.parentFrame = parent;
        this.gamePanel = game;
        this.player = p;
        second.pauseTimers(player);

        menu1 = ImageLoader.loadImage("/assets/menu1.png");
        menu2 = ImageLoader.loadImage("/assets/menu2.png");
        menu3 = ImageLoader.loadImage("/assets/menu3.png");

        buttonWidth = menu1.getWidth();
        buttonHeight = menu1.getHeight();

        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                int centerX = getWidth() / 2 - buttonWidth / 2;
                int button1Y = getHeight() / 2 - 250;
                int button2Y = getHeight() / 2 - 50;
                int button3Y = getHeight() / 2 + 150;

                // Кнопка 1: Сохранить
                if (mouseX >= centerX && mouseX <= centerX + buttonWidth &&
                        mouseY >= button1Y && mouseY <= button1Y + buttonHeight) {

                    int locId;
                    if (gamePanel instanceof second) {
                        locId = 0;
                    } else {
                        locId = 1;
                    }
                    GameSave.save(player, locId);
                    JOptionPane.showMessageDialog(null, "Игра сохранена!", "Сохранение", JOptionPane.INFORMATION_MESSAGE);
                }

                // Кнопка 2: В меню
                if (mouseX >= centerX && mouseX <= centerX + buttonWidth &&
                        mouseY >= button2Y && mouseY <= button2Y + buttonHeight) {

                    second.resumeTimers(player);
                    parentFrame.remove(PauseMenu.this); // ← ПРАВИЛЬНО: ссылка на панель
                    parentFrame.add(new game());
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }

                // Кнопка 3: Продолжить
                if (mouseX >= centerX && mouseX <= centerX + buttonWidth &&
                        mouseY >= button3Y && mouseY <= button3Y + buttonHeight) {

                    second.resumeTimers(player);
                    parentFrame.remove(PauseMenu.this); // ← ПРАВИЛЬНО: ссылка на панель
                    parentFrame.add(gamePanel);
                    parentFrame.revalidate();
                    parentFrame.repaint();
                    gamePanel.requestFocusInWindow();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int centerX = getWidth() / 2 - buttonWidth / 2;
        int button1Y = getHeight() / 2 - 250;
        int button2Y = getHeight() / 2 - 50;
        int button3Y = getHeight() / 2 + 150;

        g.setColor(new Color(40, 40, 40, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Berlin Sans FB", Font.PLAIN, 60));
        g.drawString("PAUSE", getWidth() / 2 - 90, 110);

        if (menu1 != null) g.drawImage(menu1, centerX, button1Y, null);
        if (menu2 != null) g.drawImage(menu2, centerX, button2Y, null);
        if (menu3 != null) g.drawImage(menu3, centerX, button3Y, null);
    }
}