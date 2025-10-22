package physics;

import entities.Entity;
import entities.Enemy;
import entities.Player;
import world.Dungeon;
import world.Tile;
// Nincs java.awt.Rectangle importálva, kizárólag a saját physics.Rectangle-t használjuk.

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
        // Feltételezve, hogy az Entity.getBounds() a physics.Rectangle-t adja vissza
        return a.getBounds().intersects(b.getBounds());
    }

    /**
     * Ellenőrzi, hogy egy entitás ütközik-e egy adott csempével.
     * Mivel a Tile örökli a physics.Rectangle-t, ez a metódus most már a saját intersects-t használja.
     * @param entity Az entitás.
     * @param tile A csempe.
     * @return Igaz, ha ütköznek, hamis egyébként.
     */
    public boolean checkTileCollision(Entity entity, Tile tile) {
        if (entity == null || tile == null) return false;
        // A 'tile' egy Rectangle altípus, így az entity.getBounds().intersects(tile) működik.
        return entity.getBounds().intersects(tile);
    }

    /**
     * Segédmetódus, ami ellenőrzi, hogy egy entitás ütközik-e falakkal az aktuális pozíciójában.
     * A Tile osztály a physics.Rectangle-t örökli, és az isSolid() adja meg a falakat.
     */
    private boolean checkEntityCollidesWithSolidTiles(Entity entity, Dungeon dungeon) {
        // Feltételezzük, hogy az Entity.getBounds() a physics.Rectangle-t adja vissza
        Rectangle entityBounds = entity.getBounds();
        int tileSize = dungeon.getTileSize();

        // physics.Rectangle publikus mezőinek használata (.x, .width, stb.)
        int minTileX = (int) (entityBounds.x / tileSize);
        int maxTileX = (int) ((entityBounds.x + entityBounds.width - 1) / tileSize);
        int minTileY = (int) (entityBounds.y / tileSize);
        int maxTileY = (int) ((entityBounds.y + entityBounds.height - 1) / tileSize);

        // Pályán belüli korlátozás
        int dungeonWidthTiles = dungeon.getWidthTiles();
        int dungeonHeightTiles = dungeon.getHeightTiles();
        minTileX = Math.max(0, minTileX);
        maxTileX = Math.min(dungeonWidthTiles - 1, maxTileX);
        minTileY = Math.max(0, minTileY);
        maxTileY = Math.min(dungeonHeightTiles - 1, maxTileY);


        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                if(x >= 0 && x < dungeonWidthTiles && y >= 0 && y < dungeonHeightTiles) {
                    Tile tile = dungeon.getTile(x, y);
                    if (tile != null && tile.isSolid()) {
                        // Mivel a Tile egy Rectangle, az intersects metódus feloldható
                        if (entityBounds.intersects(tile)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Feloldja a játékos és a falak közötti ütközéseket.
     */
    public void resolvePlayerTileCollisions(Player player, float prevX, float prevY) {
        if (player == null || dungeon == null) return;

        float originalX = player.getX();
        float originalY = player.getY();
        player.setX(prevX);
        player.setY(prevY);

        // X tengely feloldása
        player.setX(originalX);
        if (checkEntityCollidesWithSolidTiles(player, dungeon)) {
            player.setX(prevX);
        }

        // Y tengely feloldása
        player.setY(originalY);
        if (checkEntityCollidesWithSolidTiles(player, dungeon)) {
            player.setY(prevY);
        }
    }


    /**
     * Feloldja az ellenség és a falak közötti ütközéseket. (JAVÍTVA: Kitolja az entitást a falból)
     */
    public void resolveEnemyTileCollisions(Enemy enemy, float prevX, float prevY) {
        if (enemy == null || dungeon == null) return;

        final float EPSILON = 0.01f; // Fontos: be kell deklarálni, ha nem tagváltozó
        int tileSize = dungeon.getTileSize();

        float desiredX = enemy.getX();
        float desiredY = enemy.getY();

        // 1. X TENGELY FELOLDÁSA
        enemy.setX(desiredX);
        enemy.setY(prevY); // Y az előző, biztonságos pozíción

        // Iteráció a csempéken, hogy megtaláljuk, melyik fallal ütköztünk
        // Hasonló logika, mint a checkEntityCollidesWithSolidTiles-ben, de itt korrigálunk
        Rectangle boundsX = enemy.getBounds();

        int minTileX = (int) (boundsX.x / tileSize);
        int maxTileX = (int) ((boundsX.x + boundsX.width - 1) / tileSize);
        int minTileY = (int) (boundsX.y / tileSize);
        int maxTileY = (int) ((boundsX.y + boundsX.height - 1) / tileSize);

        float moveX = desiredX - prevX;

        // Csempék korlátozása itt kihagyva, de a lényeg a korrekció
        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                Tile tile = dungeon.getTile(x, y);

                if (tile != null && tile.isSolid() && boundsX.intersects(tile)) {
                    // Ütközés talált X-en: KORRIGÁLÁS a fal határára!
                    if (moveX > 0) { // Jobbra mozgott
                        enemy.setX(tile.x - enemy.getWidth() - EPSILON);
                    } else if (moveX < 0) { // Balra mozgott
                        enemy.setX(tile.x + tile.width + EPSILON);
                    }

                    // A korrekció után kilépünk, csak egy csempére korrigálunk
                    desiredX = enemy.getX();
                    boundsX = enemy.getBounds();
                    break;
                }
            }
        }

        // 2. Y TENGELY FELOLDÁSA
        enemy.setX(desiredX); // X a korrigált/elfogadott X
        enemy.setY(desiredY); // Y az új, kívánt pozíción

        // Megismételjük az ellenőrzést Y tengelyre
        Rectangle boundsY = enemy.getBounds();

        minTileX = (int) (boundsY.x / tileSize);
        maxTileX = (int) ((boundsY.x + boundsY.width - 1) / tileSize);
        minTileY = (int) (boundsY.y / tileSize);
        maxTileY = (int) ((boundsY.y + boundsY.height - 1) / tileSize);

        float moveY = desiredY - prevY;

        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                Tile tile = dungeon.getTile(x, y);

                if (tile != null && tile.isSolid() && boundsY.intersects(tile)) {
                    // Ütközés talált Y-on: KORRIGÁLÁS a fal határára!
                    if (moveY > 0) { // Felfelé mozgott
                        enemy.setY(tile.y - enemy.getHeight() - EPSILON);
                    } else if (moveY < 0) { // Lefelé mozgott
                        enemy.setY(tile.y + tile.height + EPSILON);
                    }

                    // A korrekció után kilépünk
                    break;
                }
            }
        }
    }
}