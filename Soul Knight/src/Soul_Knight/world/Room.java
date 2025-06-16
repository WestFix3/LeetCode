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
        // A szoba generálása (falak és padló)
        for (int x = 0; x < widthTiles; x++) {
            for (int y = 0; y < heightTiles; y++) {
                if (x == 0 || x == widthTiles - 1 || y == 0 || y == heightTiles - 1) {
                    tiles[x][y] = new Tile(Tile.TileType.WALL, x, y, tileSize, tileTextures.get(Tile.TileType.WALL));
                } else {
                    tiles[x][y] = new Tile(Tile.TileType.FLOOR, x, y, tileSize, tileTextures.get(Tile.TileType.FLOOR));
                }
            }
        }

        // Ajtók beállítása
        int doorHeight = 5;
        int startY = heightTiles / 2 - (doorHeight / 2);
        for (int y = 0; y < doorHeight; y++) {
            if (startY + y >= 0 && startY + y < heightTiles) {
                tiles[widthTiles / 2][0] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, 0, tileSize, tileTextures.get(Tile.TileType.FLOOR)); // Felső
                tiles[widthTiles / 2][heightTiles - 1] = new Tile(Tile.TileType.FLOOR, widthTiles / 2, heightTiles - 1, tileSize, tileTextures.get(Tile.TileType.FLOOR)); // Alsó
                tiles[0][heightTiles / 2] = new Tile(Tile.TileType.FLOOR, 0, heightTiles / 2, tileSize, tileTextures.get(Tile.TileType.FLOOR)); // Bal
                tiles[widthTiles - 1][startY + y] = new Tile(Tile.TileType.FLOOR, widthTiles - 1, startY + y, tileSize, tileTextures.get(Tile.TileType.FLOOR)); // Jobb
            }
        }

        // ----- ELLENSÉGEK SPAWNOLÁSA: A KÉPERNYŐ KÖZELÉBE (ahol a játékos van) -----
        // Mivel a játékos a (1280/2, 720/2) = (640, 360) képernyőkoordinátán van,
        // az ellenségeket is hasonlóan kell elhelyezni, ha láthatóvá akarjuk tenni őket.

        // Hozzunk létre egy referencia pontot a képernyő középpontjában
        //float screenCenterX = 1280 / 2; // Ez a GameManager.width / 2 értéke
        //float screenCenterY = 720 / 2;  // Ez a GameManager.height / 2 értéke
        float screenCenterX = 500 / 2;
        float screenCenterY = 400 / 2;

        // Spawnoljunk 3 ellenséget a képernyő középpontja körül
        for (int i = 0; i < 3; i++) {
            // A spawn terület legyen a képernyő középpontja +/- egy bizonyos offset
            float spawnOffsetX = (float) (Math.random() * 200 - 100); // -100 és +100 pixel között
            float spawnOffsetY = (float) (Math.random() * 200 - 100); // -100 és +100 pixel között

            float enemyX = screenCenterX + spawnOffsetX;
            float enemyY = screenCenterY + spawnOffsetY;

            Enemy newEnemy = new Enemy(enemyX, enemyY, 40, 40, enemyTexture, 50); // 40x40 méret, 50 HP
            enemies.add(newEnemy);
            System.out.println("DEBUG: Ellenség spawnolva a (" + enemyX + ", " + enemyY + ") pozícióra. HP: " + newEnemy.getHealth());
        }
        // -----------------------------------------------------------------------
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

    public int getTileSize() {
        return tileSize;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
