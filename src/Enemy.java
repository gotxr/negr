public class Enemy {
    public int x;
    public int y;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int health = 2;

    public boolean isAlive() {
        return health > 0;
    }

}