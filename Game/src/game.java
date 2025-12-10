import javax.swing.*;
import java.awt.*;

public class game extends JFrame {

    Image knopka1;
    Image knopka2;
    Image knopka3;
    Image fon;

    game() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        fon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/zastavka.png"));
        knopka1 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/knopka1.png"));
        knopka2 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/knopka2.png"));
        knopka3 = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/assets/knopka3.png"));

        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);
    
        g.drawImage(fon, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(knopka1, getWidth()/2 - 175, getHeight()/2 - 300, this);
        g.drawImage(knopka2, getWidth()/2 - 175, getHeight()/2 - 100, this);
        g.drawImage(knopka3, getWidth()/2 - 175, getHeight()/2 + 100, this);
    }

    public static void main(String[] args) {
        game w = new game();

    
        

    }
}
