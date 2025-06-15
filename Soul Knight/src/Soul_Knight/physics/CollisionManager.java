package Soul_Knight.physics;

import Soul_Knight.entities.Entity;
import Soul_Knight.entities.Player;
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
     * Feloldja a játékos és a falak közötti ütközéseket.
     * Ez a metódus megakadályozza, hogy a játékos átmenjen a falakon.
     * @param player A játékos entitás.
     */
    public void resolvePlayerTileCollisions(Player player) {
        if (player == null || dungeon == null) return;

        Room currentRoom = dungeon.getMainRoom(); // Feltételezzük, hogy egy szobában vagyunk
        if (currentRoom == null) return;

        int playerGridX = (int) (player.getX() / currentRoom.getTileSize());
        int playerGridY = (int) (player.getY() / currentRoom.getTileSize());

        // Ellenőrizzük a játékos körüli csempéket (3x3-as rácsban)
        for (int x = playerGridX - 1; x <= playerGridX + 1; x++) {
            for (int y = playerGridY - 1; y <= playerGridY + 1; y++) {
                Tile tile = currentRoom.getTile(x, y);
                if (tile != null && tile.getType() == Tile.TileType.WALL) {
                    // Ha a csempe egy fal
                    Rectangle playerBounds = player.getBounds();
                    Rectangle tileBounds = new Rectangle(x * tile.getSize(), y * tile.getSize(), tile.getSize(), tile.getSize());

                    if (playerBounds.intersects(tileBounds)) {
                        // Ütközés történt, feloldjuk
                        float overlapX = 0;
                        float overlapY = 0;

                        // Ütközési vektor kiszámítása
                        if (playerBounds.x < tileBounds.x && playerBounds.x + playerBounds.width > tileBounds.x) {
                            overlapX = (playerBounds.x + playerBounds.width) - tileBounds.x;
                        } else if (playerBounds.x > tileBounds.x && tileBounds.x + tileBounds.width > playerBounds.x) {
                            overlapX = (tileBounds.x + tileBounds.width) - playerBounds.x;
                        }

                        if (playerBounds.y < tileBounds.y && playerBounds.y + playerBounds.height > tileBounds.y) {
                            overlapY = (playerBounds.y + playerBounds.height) - tileBounds.y;
                        } else if (playerBounds.y > tileBounds.y && tileBounds.y + tileBounds.height > playerBounds.y) {
                            overlapY = (tileBounds.y + tileBounds.height) - playerBounds.y;
                        }

                        // Csak a kisebb átfedés irányába mozdítsuk el
                        if (overlapX != 0 && overlapY != 0) {
                            if (Math.abs(overlapX) < Math.abs(overlapY)) {
                                player.setX(player.getX() - (overlapX > 0 ? overlapX : -overlapX)); // Előző pozícióra vissza, vagy eltolás
                            } else {
                                player.setY(player.getY() - (overlapY > 0 ? overlapY : -overlapY));
                            }
                        } else if (overlapX != 0) {
                            player.setX(player.getX() - (overlapX > 0 ? overlapX : -overlapX));
                        } else if (overlapY != 0) {
                            player.setY(player.getY() - (overlapY > 0 ? overlapY : -overlapY));
                        }
                    }
                }
            }
        }
    }
}
