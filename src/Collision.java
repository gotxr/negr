public class Collision {
    public int x, y, width, height;

    public Collision(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Проверяет, находится ли точка (px, py) внутри этого прямоугольника
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    // Проверяет, пересекается ли прямоугольник игрока с этой зоной
    public boolean intersects(int px, int py, int playerWidth, int playerHeight) {
        return px + playerWidth > x && px < x + width &&
                py + playerHeight > y && py < y + height;
    }
}