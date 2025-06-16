package Soul_Knight.physics;

import Soul_Knight.entities.Entity;
import Soul_Knight.entities.Player;
import Soul_Knight.entities.Projectile; // ÚJ IMPORT: Lövedék ütközéshez
import Soul_Knight.world.Dungeon;
import Soul_Knight.world.Tile;
import Soul_Knight.world.Room;

/**
 * Kezeli az entitások közötti ütközéseket és a falakkal való ütközések feloldását.
 */
public class CollisionManager {

    private Dungeon dungeon;

    public CollisionManager(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    /**
     * Ellenőrzi, hogy két entitás metszi-e egymást a hitboxuk alapján.
     * @param a Az első entitás.
     * @param b A második entitás.
     * @return Igaz, ha a hitboxuk metszi egymást, hamis egyébként.
     */
    public boolean checkCollision(Entity a, Entity b) {
        if (a == null || b == null) return false;
        return a.getBounds().intersects(b.getBounds());
    }

    /**
     * Ellenőrzi, hogy egy entitás ütközik-e egy adott csempével.
     * @param entity Az entitás.
     * @param tile A csempe.
     * @return Igaz, ha ütköznek, hamis egyébként.
     */
    public boolean checkTileCollision(Entity entity, Tile tile) {
        if (entity == null || tile == null) return false;
        return entity.getBounds().intersects(tile.getBounds());
    }

    /**
     * Feloldja a játékos és a falak közötti ütközéseket.
     * Ez a metódus megakadályozza, hogy a játékos átmenjen a falakon.
     * A mozgás előtti pozíció alapján próbálja feloldani az ütközést.
     * @param player A játékos entitás.
     * @param prevX A játékos előző X pozíciója.
     * @param prevY A játékos előző Y pozíciója.
     */
    public void resolvePlayerTileCollisions(Player player, float prevX, float prevY) {
        if (player == null || dungeon == null) return;

        Room currentRoom = dungeon.getMainRoom();
        if (currentRoom == null) return;

        // Ideiglenesen visszaállítjuk a játékost az előző pozíciójába
        float originalX = player.getX();
        float originalY = player.getY();
        player.setX(prevX); // Visszaállítjuk az X pozíciót az ütközés előtti állapotra
        player.setY(prevY); // Visszaállítjuk az Y pozíciót az ütközés előtti állapotra

        // Ellenőrizzük a játékos X-irányú mozgását
        player.setX(originalX); // A teljes X mozgás alkalmazása
        if (checkPlayerCollidesWithWalls(player, currentRoom)) {
            player.setX(prevX); // Ha ütközött, visszaállítjuk az X-et
        }

        // Ellenőrizzük a játékos Y-irányú mozgását
        player.setY(originalY); // A teljes Y mozgás alkalmazása
        if (checkPlayerCollidesWithWalls(player, currentRoom)) {
            player.setY(prevY); // Ha ütközött, visszaállítjuk az Y-t
        }
    }

    /**
     * Segédmetódus, ami ellenőrzi, hogy a játékos ütközik-e falakkal az aktuális pozíciójában.
     * @param player A játékos entitás.
     * @param room Az aktuális szoba.
     * @return Igaz, ha ütközik fallal, hamis egyébként.
     */
    private boolean checkPlayerCollidesWithWalls(Player player, Room room) {
        Rectangle playerBounds = player.getBounds();
        int tileSize = room.getTileSize();

        // Ellenőrizzük a játékos hitboxát érintő csempéket
        int minTileX = (int) (playerBounds.x / tileSize);
        int maxTileX = (int) ((playerBounds.x + playerBounds.width) / tileSize);
        int minTileY = (int) (playerBounds.y / tileSize);
        int maxTileY = (int) ((playerBounds.y + playerBounds.height) / tileSize);

        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                Tile tile = room.getTile(x, y);
                if (tile != null && tile.getType() == Tile.TileType.WALL) {
                    if (playerBounds.intersects(tile.getBounds())) {
                        return true; // Ütközés történt egy fallal
                    }
                }
            }
        }
        return false;
    }
}
