package Soul_Knight.world;

import Soul_Knight.entities.Enemy; // ÚJ IMPORT!
import Soul_Knight.rendering.Texture; // ÚJ IMPORT!
import java.util.ArrayList; // ÚJ IMPORT!
import java.util.List; // ÚJ IMPORT!
import java.util.Map; // ÚJ IMPORT!

public class Room {
    private Tile[][] tiles;
    private int widthTiles;
    private int heightTiles;
    private int tileSize;
    private Map<Tile.TileType, Texture> tileTextures;
    private List<Enemy> enemies; // ÚJ: Ellenségek listája a szobában
    private Texture enemyTexture; // ÚJ: Ellenség textúra

    public Room(int widthTiles, int heightTiles, int tileSize, Map<Tile.TileType, Texture> tileTextures, Texture enemyTexture) {
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileSize = tileSize;
        this.tiles = new Tile[widthTiles][heightTiles];
        this.tileTextures = tileTextures;
        this.enemies = new ArrayList<>(); // Inicializáljuk a listát
        this.enemyTexture = enemyTexture; // Ellenség textúra átadása
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

        // Példa egy "ajtóra" (ez sokkal komplexebb lesz valójában)
        tiles[widthTiles / 2][0] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, 0, tileSize, tileTextures.get(Tile.TileType.FLOOR));
        tiles[widthTiles / 2][heightTiles - 1] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, heightTiles - 1, tileSize, tileTextures.get(Tile.TileType.FLOOR));
        tiles[0][heightTiles / 2] = new Tile(Tile.TileType.FLOOR, 0, heightTiles / 2, tileSize, tileTextures.get(Tile.TileType.FLOOR));
        tiles[widthTiles - 1][heightTiles / 2] = new Tile(Tile.TileType.FLOOR, widthTiles - 1, heightTiles / 2, tileSize, tileTextures.get(Tile.TileType.FLOOR));

        // ----- KEZDETI ELLENSÉGEK SPAWNOLÁSA -----
        // Példa: spawnolunk 3 ellenséget a szoba közepére
        for (int i = 0; i < 3; i++) {
            // Helyezzük el őket véletlenszerűen a szoba "padló" részén
            float enemyX = (float) ((widthTiles / 2 - 2 + Math.random() * 4) * tileSize);
            float enemyY = (float) ((heightTiles / 2 - 2 + Math.random() * 4) * tileSize);
            enemies.add(new Enemy(enemyX, enemyY, 40, 40, enemyTexture, 50)); // 40x40 méret, 50 HP
        }
        // ------------------------------------------
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
