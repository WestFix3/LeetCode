package physics;

import entities.Entity;
import entities.Player;
import entities.Projectile;
import world.Dungeon;
import world.Tile;
// import world.Room; // <-- TÖRÖLD VAGY VÉLEMÉNYEZD KI EZT AZ IMPORTOT!

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

        // A Room currentRoom = dungeon.getMainRoom(); sorra már nincs szükség!

        // Ideiglenesen visszaállítjuk a játékost az előző pozíciójába
        float originalX = player.getX();
        float originalY = player.getY();
        player.setX(prevX);
        player.setY(prevY);

        // Ellenőrizzük a játékos X-irányú mozgását
        player.setX(originalX);
        if (checkPlayerCollidesWithWalls(player, dungeon)) { // <-- Most a dungeon-t adjuk át
            player.setX(prevX);
        }

        // Ellenőrizzük a játékos Y-irányú mozgását
        player.setY(originalY);
        if (checkPlayerCollidesWithWalls(player, dungeon)) { // <-- Most a dungeon-t adjuk át
            player.setY(prevY);
        }
    }

    /**
     * Segédmetódus, ami ellenőrzi, hogy a játékos ütközik-e falakkal az aktuális pozíciójában.
     * @param player A játékos entitás.
     * @param dungeon A teljes dungeon (pálya).
     * @return Igaz, ha ütközik fallal, hamis egyébként.
     */
    private boolean checkPlayerCollidesWithWalls(Player player, Dungeon dungeon) {
        Rectangle playerBounds = player.getBounds();
        int tileSize = dungeon.getTileSize(); // <-- A tileSize a Dungeon-től jön

        // Ellenőrizzük a játékos hitboxát érintő csempéket
        int minTileX = (int) (playerBounds.x / tileSize);
        int maxTileX = (int) ((playerBounds.x + playerBounds.width) / tileSize);
        int minTileY = (int) (playerBounds.y / tileSize);
        int maxTileY = (int) ((playerBounds.y + playerBounds.height) / tileSize);

        // Győződjünk meg róla, hogy a min/max tile koordináták a pálya határain belül legyenek
        minTileX = Math.max(0, minTileX);
        maxTileX = Math.min(dungeon.getWidthTiles() - 1, maxTileX);
        minTileY = Math.max(0, minTileY);
        maxTileY = Math.min(dungeon.getHeightTiles() - 1, maxTileY);


        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                Tile tile = dungeon.getTile(x, y); // <-- A csempét a Dungeon-től kérjük el
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