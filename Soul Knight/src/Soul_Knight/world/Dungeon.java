package Soul_Knight.world;

import Soul_Knight.rendering.Texture;
import java.util.Map;

/**
 * Ez az osztály fogja reprezentálni az egész dungeon-t,
 * ami kezdetben csak egyetlen szobából áll majd.
 * Később ide jön a komplexebb DungeonGeneration logika.
 */
public class Dungeon {
    private Room mainRoom;
    private int widthTiles;
    private int heightTiles;
    private int tileSize;

    public Dungeon(int widthTiles, int heightTiles, int tileSize, Map<Tile.TileType, Texture> tileTextures, Texture enemyTexture) {
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileSize = tileSize;
        // Kezdetben csak egyetlen szobát generálunk, és átadjuk neki a textúra map-et és az ellenség textúrát
        this.mainRoom = new Room(widthTiles, heightTiles, tileSize, tileTextures, enemyTexture);
        // Később itt jön a komplexebb algoritmus, ami több szobát is generál és összeköt
    }

    public Room getMainRoom() {
        return mainRoom;
    }

    // Később lehetnek metódusok a szobák közötti váltáshoz
}
