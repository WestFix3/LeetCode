package world;

import entities.Enemy;
import rendering.Texture;
import java.util.List;
import java.util.Map;

/**
 * Ez az osztály reprezentálja az egész dungeon-t,
 * egy generált Tile[][] mátrix alapján.
 */
public class Dungeon {
    private Tile[][] tiles;
    private int widthTiles;
    private int heightTiles;
    private int tileSize;
    private List<Enemy> enemies; // A Dungeon kezeli az összes ellenséget
    private float playerSpawnX; // <-- ÚJ MEZŐ
    private float playerSpawnY; // <-- ÚJ MEZŐ

    // A konstruktor most egy előre generált csempe mátrixot fogad el
    public Dungeon(Tile[][] generatedTiles, int tileSize, List<Enemy> generatedEnemies, float playerSpawnX, float playerSpawnY) { // <-- Módosított konstruktor
        this.tiles = generatedTiles;
        this.widthTiles = generatedTiles.length;
        this.heightTiles = generatedTiles[0].length;
        this.tileSize = tileSize;
        this.enemies = generatedEnemies;
        this.playerSpawnX = playerSpawnX; // <-- Inicializálás
        this.playerSpawnY = playerSpawnY; // <-- Inicializálás

        System.out.println("DEBUG: Generált pálya mérete (blokkban): " + widthTiles + "x" + heightTiles);
        System.out.println("DEBUG: Játékos spawn pozíciója (pixelben): " + playerSpawnX + ", " + playerSpawnY);
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < widthTiles && y >= 0 && y < heightTiles) {
            return tiles[x][y];
        }
        return null;
    }

    public int getWidthTiles() {
        return widthTiles;
    }

    public int getHeightTiles() {
        return heightTiles;
    }

    public int getTileSize() { // <-- Ennek a metódusnak itt kell lennie!
        return tileSize;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public float getPlayerSpawnX() { // <-- ÚJ GETTER
        return playerSpawnX;
    }

    public float getPlayerSpawnY() { // <-- ÚJ GETTER
        return playerSpawnY;
    }
}