import java.awt.image.BufferedImage;

public class Recipe {
    public String name;
    public BufferedImage outputIcon;
    public int outputAmount;
    public String ingredient;
    public int amount;

    public Recipe(String name, BufferedImage outputIcon, int outputAmount,
                  String ingredient, int amount) {
        this.name = name;
        this.outputIcon = outputIcon;
        this.outputAmount = outputAmount;
        this.ingredient = ingredient;
        this.amount = amount;
    }
}