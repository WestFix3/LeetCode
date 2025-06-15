package Soul_Knight.world;

import Soul_Knight.rendering.Texture; // Új import

public class Tile {
    public enum TileType {
        FLOOR,
        WALL
        // Később: DOOR, TRAP, SPAWN_POINT, etc.
    }

    private TileType type;
    private int gridX, gridY; // Rács pozíció
    private int size; // Csempe mérete pixelben
    private Texture texture; // A csempe textúrája

    public Tile(TileType type, int gridX, int gridY, int size, Texture texture) {
        this.type = type;
        this.gridX = gridX;
        this.gridY = gridY;
        this.size = size;
        this.texture = texture; // Textúra inicializálása
    }

    public TileType getType() {
        return type;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getSize() {
        return size;
    }

    public Texture getTexture() {
        return texture;
    }
}