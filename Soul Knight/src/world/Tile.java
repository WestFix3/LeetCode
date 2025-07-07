package world;

import rendering.Texture;
import physics.Rectangle;

public class Tile {
    public enum TileType {
        FLOOR,
        WALL
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
        this.texture = texture;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) { // <-- Add hozzá, ha hiányzik
        this.type = type;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridX(int gridX) { // <-- Add hozzá, ha hiányzik
        this.gridX = gridX;
    }

    public void setGridY(int gridY) { // <-- Add hozzá, ha hiányzik
        this.gridY = gridY;
    }

    public int getSize() {
        return size;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) { // <-- Add hozzá, ha hiányzik
        this.texture = texture;
    }

    /**
     * Visszaadja a csempe határoló téglalapját a képernyő koordinátáiban.
     * @return Egy Rectangle objektum, ami a csempe fizikai határait reprezentálja.
     */
    public Rectangle getBounds() {
        return new Rectangle(gridX * size, gridY * size, size, size);
    }
}