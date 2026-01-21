import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
        button1Y = getHeight() / 2 - 300;

        g.drawImage(fon, 0, 0, getWidth(), getHeight(), null);

        g.drawImage(knopka1, button1X, button1Y, null);
        g.drawImage(knopka2, getWidth() / 2 - 175, getHeight() / 2 - 100, null);
        g.drawImage(knopka3, getWidth() / 2 - 175, getHeight() / 2 + 100, null);
    }

    public static void main(String[] args) {
        new game();
    }
}