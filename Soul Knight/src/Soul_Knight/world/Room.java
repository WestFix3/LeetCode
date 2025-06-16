package Soul_Knight.world;

import Soul_Knight.entities.Enemy;
import Soul_Knight.rendering.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Room {
    private Tile[][] tiles;
    private int widthTiles;
    private int heightTiles;
    private int tileSize;
    private Map<Tile.TileType, Texture> tileTextures;
    private List<Enemy> enemies;
    private Texture enemyTexture;

    public Room(int widthTiles, int heightTiles, int tileSize, Map<Tile.TileType, Texture> tileTextures, Texture enemyTexture) {
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileSize = tileSize;
        this.tiles = new Tile[widthTiles][heightTiles];
        this.tileTextures = tileTextures;
        this.enemies = new ArrayList<>();
        this.enemyTexture = enemyTexture;
        initializeRoom();
    }

    private void initializeRoom() {
        // Egy nagyon egyszerű, alapértelmezett szoba generálása
        for (int x = 0; x < widthTiles; x++) {
            for (int y = 0; y < heightTiles; y++) {
                if (x == 0 || x == widthTiles - 1 || y == 0 || y == heightTiles - 1) {
                    tiles[x][y] = new Tile(Tile.TileType.WALL, x, y, tileSize, tileTextures.get(Tile.TileType.WALL));
                } else {
                    tiles[x][y] = new Tile(Tile.TileType.FLOOR, x, y, tileSize, tileTextures.get(Tile.TileType.FLOOR));
                }
            }
        }

        // Példa "ajtókra"

        // Felső ajtó (egy csempe, középen)
        tiles[widthTiles / 2][0] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, 0, tileSize, tileTextures.get(Tile.TileType.FLOOR));

        // Alsó ajtó (egy csempe, középen)
        tiles[widthTiles / 2][heightTiles - 1] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, heightTiles - 1, tileSize, tileTextures.get(Tile.TileType.FLOOR));

        // Bal ajtó (egy csempe, középen)
        tiles[0][heightTiles / 2] = new Tile(Tile.TileType.FLOOR, 0, heightTiles / 2, tileSize, tileTextures.get(Tile.TileType.FLOOR));

        // ----- JOBB OLDALI BEJÁRAT MÉRETÉNEK BEÁLLÍTÁSA -----
        // Példa: 5 csempe magasságú ajtó a jobb oldalon, középen
        // Feltételezzük, hogy a heightTiles páratlan, vagy elegendően nagy az 5 csempéhez
        int doorHeight = 5; // Az ajtó magassága csempékben (Módosítva 3-ról 5-re)
        int startY = heightTiles / 2 - (doorHeight / 2); // Kezdő Y pozíció a középtől

        for (int y = 0; y < doorHeight; y++) {
            // Győződjünk meg róla, hogy az Y koordináta érvényes a pályán belül
            if (startY + y >= 0 && startY + y < heightTiles) {
                tiles[widthTiles - 1][startY + y] = new Tile(Tile.TileType.FLOOR, widthTiles - 1, startY + y, tileSize, tileTextures.get(Tile.TileType.FLOOR));
            }
        }
        // -----------------------------------------------------

        // Később itt lehet ellenségeket, tárgyakat spawnolni
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < widthTiles && y >= 0 && y < heightTiles) {
            return tiles[x][y];
        }
        return null; // Vagy egy "üres" csempe típust ad vissza
    }

    public int getWidthTiles() {
        return widthTiles;
    }

    public int getHeightTiles() {
        return heightTiles;
    }

    public int getTileSize() {
        return tileSize;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
