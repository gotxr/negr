import java.io.*;

public class GameSave {

    public static void save(Player player, int locationId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("save.txt"))) {
            // Игрок
            writer.write("health=" + player.health);
            writer.newLine();
            writer.write("hunger=" + player.hunger);
            writer.newLine();
            writer.write("thirst=" + player.thirst);
            writer.newLine();
            writer.write("x=" + player.x);
            writer.newLine();
            writer.write("y=" + player.y);
            writer.newLine();
            // Сохраняем: сколько миллисекунд прошло с начала текущего дня/ночи
            long timeIntoCycle = System.currentTimeMillis() - player.dayStartTime;
            writer.write("timeIntoCycle=" + timeIntoCycle);
            writer.newLine();
            writer.write("isNight=" + player.isNight);
            writer.newLine();

            // Локация
            writer.write("location=" + locationId);
            writer.newLine();

            // Яблони
            writer.write("appleTreeStages=");
            for (int i = 0; i < player.appleTreeStages.length; i++) {
                writer.write(String.valueOf(player.appleTreeStages[i]));
                if (i < player.appleTreeStages.length - 1) writer.write(",");
            }
            writer.newLine();

            writer.write("appleTreeTimes=");
            for (int i = 0; i < player.appleTreeLastUpdateTime.length; i++) {
                writer.write(String.valueOf(player.appleTreeLastUpdateTime[i]));
                if (i < player.appleTreeLastUpdateTime.length - 1) writer.write(",");
            }
            writer.newLine();

            // Саженцы
            writer.write("sproutCount=" + player.sprouts.size());
            writer.newLine();
            for (int i = 0; i < player.sprouts.size(); i++) {
                long[] s = player.sprouts.get(i);
                writer.write("sprout" + i + "=" + s[0] + "," + s[1] + "," + s[2]);
                writer.newLine();
            }

            // Инвентарь
            for (int row = 0; row < 3; row++) {
                writer.write("invRow" + row + "=");
                for (int col = 0; col < 6; col++) {
                    Item item = player.inventory[row][col];
                    if (item != null && !item.isEmpty()) {
                        writer.write(item.type + "," + item.count);
                    } else {
                        writer.write("empty,0");
                    }
                    if (col < 5) writer.write(";");
                }
                writer.newLine();
            }

            // Хотбар
            writer.write("hotbar=");
            for (int i = 0; i < 6; i++) {
                Item item = player.hotbar[i];
                if (item != null && !item.isEmpty()) {
                    writer.write(item.type + "," + item.count);
                } else {
                    writer.write("empty,0");
                }
                if (i < 5) writer.write(";");
            }
            writer.newLine();

            // Сохранение деревьев
            writer.write("treeCount=" + countNonNull(player.trees));
            writer.newLine();
            for (int[] tree : player.trees) {
                if (tree != null) {
                    writer.write("tree=" + tree[0] + "," + tree[1]);
                    writer.newLine();
                }
            }

            // Сохранение камней
            writer.write("stoneCount=" + countNonNull(player.stones));
            writer.newLine();
            for (int[] stone : player.stones) {
                if (stone != null) {
                    writer.write("stone=" + stone[0] + "," + stone[1]);
                    writer.newLine();
                }
            }

            System.out.println("Игра сохранена!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Player load() {
        try (BufferedReader reader = new BufferedReader(new FileReader("save.txt"))) {
            Player p = new Player(0, 0);
            p.image = ImageLoader.loadImage("/assets/pers1.png");
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("health=")) p.health = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("hunger=")) p.hunger = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("thirst=")) p.thirst = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("x=")) p.x = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("y=")) p.y = Integer.parseInt(line.split("=")[1]);
                else if (line.startsWith("isNight=")) p.isNight = Boolean.parseBoolean(line.split("=")[1]);
                else if (line.startsWith("timeIntoCycle=")) {
                    long timeIntoCycle = Long.parseLong(line.split("=")[1]);
                    p.dayStartTime = System.currentTimeMillis() - timeIntoCycle;
                }
                else if (line.startsWith("appleTreeStages=")) {
                    String[] parts = line.split("=")[1].split(",");
                    for (int i = 0; i < parts.length && i < p.appleTreeStages.length; i++) {
                        p.appleTreeStages[i] = Integer.parseInt(parts[i]);
                    }
                }
                else if (line.startsWith("appleTreeTimes=")) {
                    String[] parts = line.split("=")[1].split(",");
                    for (int i = 0; i < parts.length && i < p.appleTreeLastUpdateTime.length; i++) {
                        p.appleTreeLastUpdateTime[i] = Long.parseLong(parts[i]);
                    }
                }
                else if (line.startsWith("sproutCount=")) {
                    int count = Integer.parseInt(line.split("=")[1]);
                    p.sprouts.clear();
                    for (int i = 0; i < count; i++) {
                        String sproutLine = reader.readLine();
                        if (sproutLine == null) break;
                        String[] parts = sproutLine.split("=")[1].split(",");
                        p.sprouts.add(new long[]{
                                Long.parseLong(parts[0]),
                                Long.parseLong(parts[1]),
                                Long.parseLong(parts[2])
                        });
                    }
                }
                else if (line.startsWith("treeCount=")) {
                    int count = Integer.parseInt(line.split("=")[1]);
                    p.trees = new int[7][];
                    for (int i = 0; i < count; i++) {
                        String treeLine = reader.readLine();
                        String[] parts = treeLine.split("=")[1].split(",");
                        p.trees[i] = new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
                    }
                    // Остальные слоты = null
                    for (int i = count; i < 7; i++) {
                        p.trees[i] = null;
                    }
                }
                else if (line.startsWith("stoneCount=")) {
                    int count = Integer.parseInt(line.split("=")[1]);
                    p.stones = new int[7][];
                    for (int i = 0; i < count; i++) {
                        String stoneLine = reader.readLine();
                        String[] parts = stoneLine.split("=")[1].split(",");
                        p.stones[i] = new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
                    }
                    // Остальные слоты = null
                    for (int i = count; i < 7; i++) {
                        p.stones[i] = null;
                    }
                }
                else if (line.startsWith("invRow0=") || line.startsWith("invRow1=") || line.startsWith("invRow2=")) {
                    int row = Character.getNumericValue(line.charAt(6));
                    String[] slots = line.split("=")[1].split(";");
                    for (int col = 0; col < slots.length && col < 6; col++) {
                        String[] parts = slots[col].split(",");
                        if (parts.length >= 2 && !"empty".equals(parts[0]) && !"0".equals(parts[1])) {
                            Item item = new Item(null, parts[0], Integer.parseInt(parts[1]));
                            p.inventory[row][col] = item;
                        } else {
                            p.inventory[row][col] = null;
                        }
                    }
                }
                else if (line.startsWith("hotbar=")) {
                    String[] slots = line.split("=")[1].split(";");
                    for (int i = 0; i < slots.length && i < 6; i++) {
                        String[] parts = slots[i].split(",");
                        if (parts.length >= 2 && !"empty".equals(parts[0]) && !"0".equals(parts[1])) {
                            Item item = new Item(null, parts[0], Integer.parseInt(parts[1]));
                            p.hotbar[i] = item;
                        } else {
                            p.hotbar[i] = null;
                        }
                    }
                }
            }

            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int loadLocation() {
        try (BufferedReader reader = new BufferedReader(new FileReader("save.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("location=")) {
                    return Integer.parseInt(line.split("=")[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int countNonNull(int[][] array) {
        int count = 0;
        for (int[] item : array) {
            if (item != null) count++;
        }
        return count;
    }


}