import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class copy extends JFrame implements KeyListener {

    Player player;

    Image rect1, rect2, rect3, rect4, rect5, rect6, rect7, rect8;
    Image fon;

    copy() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/location1(bez_dereva).png"));
        rect1 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect1.png"));
        rect2 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect2.png"));
        rect3 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect3.png"));
        rect4 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect4.png"));
        rect5 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect5.png"));
        rect6 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect6.png"));
        rect7 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect7.png"));
        rect8 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect8.png"));

        setVisible(true);
        int startX = getWidth() / 2 - 175;
        int startY = getHeight() / 2 - 300;
        player = new Player(startX, startY);
        addKeyListener(this);
        setFocusable(true);


    }

    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(fon, 0, 0, getWidth(), getHeight(), this);

        g.drawImage(rect1, getWidth()/2 - 600, getHeight()/2 + 330, this);
        g.drawImage(rect2, getWidth()/2 - 450, getHeight()/2 + 330, this);
        g.drawImage(rect3, getWidth()/2 - 300, getHeight()/2 + 330, this);
        g.drawImage(rect4, getWidth()/2 - 150, getHeight()/2 + 330, this);
        g.drawImage(rect5, getWidth()/2 + 0,   getHeight()/2 + 330, this);
        g.drawImage(rect6, getWidth()/2 + 150, getHeight()/2 + 330, this);
        g.drawImage(rect7, getWidth()/2 + 300, getHeight()/2 + 330, this);
        g.drawImage(rect8, getWidth()/2 + 450, getHeight()/2 + 330, this);
        g.drawImage(player.image, player.x, player.y, this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 5;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W){
            player.y -= speed;}
        if (key == KeyEvent.VK_S){
            player.y += speed;}
        if (key == KeyEvent.VK_A){
            player.x -= speed;}
        if (key == KeyEvent.VK_D){
            player.x += speed;}
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        copy w = new copy();
    }
}