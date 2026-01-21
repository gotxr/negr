import java.awt.image.BufferedImage;

public class Item {
    public BufferedImage icon;
    public String type;
    public int count;

    public Item(BufferedImage icon, String type, int count) {
        this.icon = icon;
        this.type = type;
        this.count = count;
    }

    public boolean isEmpty() {
        return icon == null || count <= 0;
    }
}