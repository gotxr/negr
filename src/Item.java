import java.awt.image.BufferedImage;

public class Item {
    public BufferedImage icon;
    public int count;

    public Item(BufferedImage icon, int count) {
        this.icon = icon;
        this.count = count;
    }

    public boolean isEmpty() {
        return icon == null || count == 0;
    }
}