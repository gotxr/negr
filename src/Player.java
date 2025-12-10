import java.awt.image.BufferedImage;

public class Player {
    public int x, y;
    public int health = 100, hunger = 100, thirst = 100;
    public BufferedImage image;

    public Item[][] inventory = new Item[3][6];
    public Item[] hotbar = new Item[6];

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.image = ImageLoader.loadImage("/assets/pers1.png");
    }

    public void setStartPos(int width, int height) {
        if (x == 0 && y == 0) {
            x = width / 2 - 175;
            y = height / 2 - 300;
        }
    }
}