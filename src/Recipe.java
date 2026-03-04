import java.awt.image.BufferedImage;

public class Recipe {
    public String name;
    public BufferedImage outputIcon;
    public int outputAmount;
    public Ingredient[] ingredients;

    // Простой конструктор (для обратной совместимости)
    public Recipe(String name, BufferedImage outputIcon, int outputAmount,
                  String ingredient, int amount) {
        this.name = name;
        this.outputIcon = outputIcon;
        this.outputAmount = outputAmount;
        this.ingredients = new Ingredient[]{new Ingredient(ingredient, amount)};
    }

    // Сложный конструктор
    public Recipe(String name, BufferedImage outputIcon, int outputAmount,
                  Ingredient... ingredients) {
        this.name = name;
        this.outputIcon = outputIcon;
        this.outputAmount = outputAmount;
        this.ingredients = ingredients;
    }
}

class Ingredient {
    public String type;
    public int amount;

    public Ingredient(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}