import javax.swing.*;
import java.awt.*;

public class second extends JFrame {

    Image pers1;
    Image rect1;
    Image rect2;
    Image rect3;
    Image rect4;
    Image rect5;
    Image rect6;
    Image rect7;
    Image rect8;
    Image fon;

    second() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

    
        fon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/location1(bez_dereva).png"));
        pers1 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/pers1.png"));
        rect1 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect1.png"));
        rect2 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect2.png"));
        rect3 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect3.png"));
        rect4 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect4.png"));
        rect5 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect5.png"));
        rect6 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect6.png"));
        rect7 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect7.png"));
        rect8 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/rect8.png"));
    
        setVisible(true);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        
        g.drawImage(fon, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(pers1, getWidth()/2 - 175, getHeight()/2 - 300, this);
        g.drawImage(rect1, getWidth()/2 - 600, getHeight()/2 + 330, this);
        g.drawImage(rect2, getWidth()/2 - 450, getHeight()/2 + 330, this);
        g.drawImage(rect3, getWidth()/2 - 300, getHeight()/2 + 330, this);
        g.drawImage(rect4, getWidth()/2 - 150, getHeight()/2 + 330, this);
        g.drawImage(rect5, getWidth()/2 + 0, getHeight()/2 + 330, this);
        g.drawImage(rect6, getWidth()/2 + 150, getHeight()/2 + 330, this);
        g.drawImage(rect7, getWidth()/2 + 300, getHeight()/2 + 330, this);
        g.drawImage(rect8, getWidth()/2 + 450, getHeight()/2 + 330, this);
    }
    
    public static void main(String[] args) {
        second w = new second();
    
        

    }
}