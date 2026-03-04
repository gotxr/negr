import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class game extends JFrame {

    BufferedImage knopka1, knopka2, knopka3, fon;
    int button1X, button1Y, buttonWidth, buttonHeight;

    game() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fon = ImageLoader.loadImage("/assets/zastavka.png");
        knopka1 = ImageLoader.loadImage("/assets/knopka1.png");
        knopka2 = ImageLoader.loadImage("/assets/knopka2.png");
        knopka3 = ImageLoader.loadImage("/assets/knopka3.png");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Кнопка 1: Новая игра
                if (mouseX >= button1X && mouseX <= button1X + buttonWidth &&
                        mouseY >= button1Y && mouseY <= button1Y + buttonHeight) {

                    dispose(); // закрываем заставку

                    Player player = new Player(0, 0);
                    player.image = ImageLoader.loadImage("/assets/pers1.png");
                    second.startGlobalTimers(player);

                    JFrame frame = new JFrame("Survival Game");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.add(new second(player));
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.setVisible(true);
                }

                // Кнопка 2: Продолжить
                int button2X = getWidth() / 2 - 175;
                int button2Y = getHeight() / 2 - 50;
                if (mouseX >= button2X && mouseX <= button2X + buttonWidth &&
                        mouseY >= button2Y && mouseY <= button2Y + buttonHeight) {

                    Player loaded = loadSavedPlayer();
                    if (loaded == null) {
                        JOptionPane.showMessageDialog(null, "Нет сохранённой игры", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                // Кнопка 3: Выход
                int button3X = getWidth() / 2 - 175;
                int button3Y = getHeight() / 2 + 150;
                if (mouseX >= button3X && mouseX <= button3X + buttonWidth &&
                        mouseY >= button3Y && mouseY <= button3Y + buttonHeight) {

                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        buttonWidth = knopka1.getWidth();
        buttonHeight = knopka1.getHeight();
        button1X = getWidth() / 2 - 175;
        button1Y = getHeight() / 2 - 250;

        g.drawImage(fon, 0, 0, getWidth(), getHeight(), null);

        g.drawImage(knopka1, button1X, button1Y, null);
        g.drawImage(knopka2, getWidth() / 2 - 175, getHeight() / 2 - 50, null);
        g.drawImage(knopka3, getWidth() / 2 - 175, getHeight() / 2 + 150, null);
    }

    private Player loadSavedPlayer() {
        Player p = GameSave.load();
        if (p == null) return null;

        p.image = ImageLoader.loadImage("/assets/pers1.png");

        int locId = GameSave.loadLocation();

        dispose();

        JFrame frame = new JFrame("Survival Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel startLocation;
        if (locId == 0) {
            startLocation = new second(p);
        } else if (locId == 1) {
            startLocation = new LocKamni(p);
        } else if (locId == 2) {
            startLocation = new LocPolana(p);
        } else if (locId == 3) {
            startLocation = new LocationDom(p);
        } else if (locId == 4) {
            startLocation = new BossLoc(p);
        } else {
            startLocation = new second(p);
        }
        frame.add(startLocation);
        second.startGlobalTimers(p);
        frame.setVisible(true);

        return p;
    }


    public static void main(String[] args) {
        new game();
    }
}