package world;

import entities.Enemy;
import rendering.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.LinkedList; // Hozzáadva a Queue használatához
import java.util.Queue;     // Hozzáadva a Queue használatához

public class DungeonGenerator {

    private static final int TILE_SIZE = 32; // Egy csempe mérete pixelben (hozzáadva, hogy konzisztens legyen a GameManager-el)
    private static final int SECTION_WIDTH_TILES = 15; // Szekció szélessége blokkokban
    private static final int SECTION_HEIGHT_TILES = 15; // Szekció magassága blokkokban
    private static final int CORRIDOR_WIDTH_TILES = 4; // Folyosó szélessége blokkokban
    private static final int MIN_CORRIDOR_LENGTH_TILES = 8;
    private static final int MAX_CORRIDOR_LENGTH_TILES = 10;
    private static final int SECTIONS_PER_ROW = 3;

    private static Random random = new Random();

    // Segédosztály a szekciók adatainak tárolásához
    private static class Section {
        int gridX, gridY; // Szekció bal felső sarka grid koordinátákban
        int width, height; // Szekció mérete blokkokban
        SectionType type;

        public Section(int gridX, int gridY, int width, int height, SectionType type) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        // Középpontok, kijáratok kiszámításához segédmetódusok
        public int getCenterX() { return gridX + width / 2; }
        public int getCenterY() { return gridY + height / 2; }
        public int getTopY() { return gridY; }
        public int getBottomY() { return gridY + height - 1; }
        public int getLeftX() { return gridX; }
        public int getRightX() { return gridX + width - 1; }
    }

    private enum SectionType {
        SPAWN, NORMAL, BOSS_ROOM
    }

    public static Dungeon generateRandomDungeon(int tileSize, Map<Tile.TileType, Texture> tileTextures, Texture enemyTexture) {
        int numSections = random.nextInt(4) + 3; // 3-6 szekció
        List<Section> sections = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();

        // Egy nagy, ideiglenes grid, amire rajzolunk.
        // A maximális méretet előre meg kell becsülni, hogy elférjen minden.
        int maxGridWidth = (SECTION_WIDTH_TILES * SECTIONS_PER_ROW) + (MAX_CORRIDOR_LENGTH_TILES * (SECTIONS_PER_ROW -1)) + CORRIDOR_WIDTH_TILES * 2 + 50;
        int maxGridHeight = (SECTION_HEIGHT_TILES * ((numSections + SECTIONS_PER_ROW - 1) / SECTIONS_PER_ROW + 1)) + (MAX_CORRIDOR_LENGTH_TILES * ((numSections + SECTIONS_PER_ROW - 1) / SECTIONS_PER_ROW)) + CORRIDOR_WIDTH_TILES * 2 + SECTION_HEIGHT_TILES + 50;

        Tile[][] tempTiles = new Tile[maxGridWidth][maxGridHeight];

        // Töltsük fel az ideiglenes gridet WALL csempékkel
        for (int x = 0; x < maxGridWidth; x++) {
            for (int y = 0; y < maxGridHeight; y++) {
                tempTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, tileSize, tileTextures.get(Tile.TileType.WALL));
            }
        }

        // --- 1. Spawn szekció elhelyezése ---
        int spawnSectionX = maxGridWidth / 2 - SECTION_WIDTH_TILES / 2;
        // A spawn szekció Y pozíciója valahol a közepén legyen a maxGridHeight-nek,
        // hogy legyen tér alatta és felette is.
        // A korábbi spawnSectionY = maxGridHeight - SECTION_HEIGHT_TILES - 20; pozíció miatt
        // a játékos a pálya alján volt, ezért nem mozgott a kamera lefelé.
        int spawnSectionY = maxGridHeight / 2; // <-- Ezt módosítottam!

        Section spawnSection = new Section(spawnSectionX, spawnSectionY, SECTION_WIDTH_TILES, SECTION_HEIGHT_TILES, SectionType.SPAWN);
        sections.add(spawnSection);
        drawSection(tempTiles, spawnSection, tileTextures);

        // Nyomon követjük a generált pálya tényleges határait, hogy levághassuk
        int minUsedX = spawnSectionX;
        int maxUsedX = spawnSectionX + SECTION_WIDTH_TILES;
        int minUsedY = spawnSectionY;
        int maxUsedY = spawnSectionY + SECTION_HEIGHT_TILES;

        // Kezdő Y szint a következő szekciókhoz (a spawn szekció felett)
        int currentYLevel = spawnSectionY - (random.nextInt(MAX_CORRIDOR_LENGTH_TILES - MIN_CORRIDOR_LENGTH_TILES + 1) + MIN_CORRIDOR_LENGTH_TILES) - SECTION_HEIGHT_TILES;
        // Frissítjük a minUsedY-t, ha a következő szekció magasabban van
        minUsedY = Math.min(minUsedY, currentYLevel);


        int placedSectionsCount = 0;
        Section previousSectionInRow = null;
        Section lastPlacedSection = spawnSection; // Az előző sorból vagy spawn szekcióból

        // Itt most egy egyszerű sor alapú generálást használunk, ami felfelé halad
        while (placedSectionsCount < numSections) {
            int sectionsInThisRow = Math.min(SECTIONS_PER_ROW, numSections - placedSectionsCount);

            // Kiszámoljuk az aktuális sor szélességét a szekciók elhelyezéséhez
            int rowWidth = (sectionsInThisRow * SECTION_WIDTH_TILES) + ((sectionsInThisRow - 1) * (MAX_CORRIDOR_LENGTH_TILES + CORRIDOR_WIDTH_TILES));
            int startXForThisRow = maxGridWidth / 2 - rowWidth / 2; // Középre igazítás

            previousSectionInRow = null; // Reseteljük minden új sornál

            for (int i = 0; i < sectionsInThisRow; i++) {
                if (placedSectionsCount >= numSections) break;

                int sectionX = startXForThisRow + (i * (SECTION_WIDTH_TILES + random.nextInt(MAX_CORRIDOR_LENGTH_TILES - MIN_CORRIDOR_LENGTH_TILES + 1) + MIN_CORRIDOR_LENGTH_TILES));
                int sectionY = currentYLevel; // Az aktuális sor Y szintje

                Section newSection = new Section(sectionX, sectionY, SECTION_WIDTH_TILES, SECTION_HEIGHT_TILES, SectionType.NORMAL);
                sections.add(newSection);
                drawSection(tempTiles, newSection, tileTextures);

                // Frissítjük a használt grid határait
                minUsedX = Math.min(minUsedX, sectionX);
                maxUsedX = Math.max(maxUsedX, sectionX + SECTION_WIDTH_TILES);
                minUsedY = Math.min(minUsedY, sectionY);
                maxUsedY = Math.max(maxUsedY, sectionY + SECTION_HEIGHT_TILES);

                // --- Folyosók rajzolása ---
                if (previousSectionInRow == null && lastPlacedSection != null) {
                    // Ez az első szekció az aktuális sorban, összekötjük az előző sorbeli utolsóval (vagy a spawn szekcióval)
                    int startCorridorX = lastPlacedSection.getCenterX();
                    int startCorridorY = lastPlacedSection.getCenterY();

                    int endCorridorX = newSection.getCenterX();
                    int endCorridorY = newSection.getCenterY();

                    // Folyosó felfelé Y irányba a lastPlacedSection-től egy random pontig, majd vízszintesen, majd felfelé az új szekcióig
                    // Először függőlegesen lefelé (Y növekszik) vagy felfelé (Y csökken) a lastPlacedSection-ből
                    // Majd vízszintesen
                    // Majd függőlegesen az új szekcióig

                    // Válasszunk egy "hajlítási" pontot az Y tengelyen.
                    // Ez a pont a newSection felső Y koordinátájától lefelé, a lastPlacedSection alsó Y koordinátájától felfelé legyen.
                    // Mivel felfelé építkezünk (Y csökken), a bendY-nek KISEBBNEK kell lennie, mint a lastPlacedSection Y-ja,
                    // de NAGYOBBNAK, mint a newSection Y-ja.
                    int corridorMidY = lastPlacedSection.getTopY() - (random.nextInt(MAX_CORRIDOR_LENGTH_TILES - MIN_CORRIDOR_LENGTH_TILES + 1) + MIN_CORRIDOR_LENGTH_TILES);

                    // Biztosítjuk, hogy a bendY ne menjen túl a newSection Y-ján, vagy túl közel hozzá
                    corridorMidY = Math.max(corridorMidY, newSection.getBottomY() + CORRIDOR_WIDTH_TILES);
                    corridorMidY = Math.min(corridorMidY, lastPlacedSection.getTopY() - CORRIDOR_WIDTH_TILES);


                    // 1. Folyosó felfelé (Y csökken) a lastPlacedSection-től a hajlítási Y-ig
                    drawCorridor(tempTiles, startCorridorX, startCorridorY, startCorridorX, corridorMidY, tileTextures, CORRIDOR_WIDTH_TILES);
                    // 2. Vízszintes folyosó a hajlítási Y-on
                    drawCorridor(tempTiles, startCorridorX, corridorMidY, endCorridorX, corridorMidY, tileTextures, CORRIDOR_WIDTH_TILES);
                    // 3. Folyosó felfelé (Y csökken) a hajlítási Y-tól az új szekcióig
                    drawCorridor(tempTiles, endCorridorX, corridorMidY, endCorridorX, endCorridorY, tileTextures, CORRIDOR_WIDTH_TILES);

                } else if (previousSectionInRow != null) {
                    // Ugyanazon a sorban lévő szekciókat kötjük össze vízszintesen
                    drawCorridor(tempTiles, previousSectionInRow.getRightX(), previousSectionInRow.getCenterY(), newSection.getLeftX(), newSection.getCenterY(), tileTextures, CORRIDOR_WIDTH_TILES);
                }

                previousSectionInRow = newSection;
                lastPlacedSection = newSection; // Az utoljára elhelyezett szekció frissítése
                placedSectionsCount++;
            }
            // Lépünk feljebb a következő sorhoz
            currentYLevel -= (random.nextInt(MAX_CORRIDOR_LENGTH_TILES - MIN_CORRIDOR_LENGTH_TILES + 1) + MIN_CORRIDOR_LENGTH_TILES + SECTION_HEIGHT_TILES);
            minUsedY = Math.min(minUsedY, currentYLevel); // Frissítjük a minUsedY-t
        }

        // --- 3. Levágjuk a felesleges margót és létrehozzuk a végleges Tile[][] mátrixot ---
        // Adunk egy kis extra margót minden oldalra
        int margin = 5;
        minUsedX = Math.max(0, minUsedX - margin);
        maxUsedX = Math.min(maxGridWidth - 1, maxUsedX + margin);
        minUsedY = Math.max(0, minUsedY - margin);
        maxUsedY = Math.min(maxGridHeight - 1, maxUsedY + margin);

        int finalWidthTiles = maxUsedX - minUsedX + 1;
        int finalHeightTiles = maxUsedY - minUsedY + 1;
        Tile[][] finalTiles = new Tile[finalWidthTiles][finalHeightTiles];

        for (int x = 0; x < finalWidthTiles; x++) {
            for (int y = 0; y < finalHeightTiles; y++) {
                // Ellenőrizzük, hogy az indexek érvényesek-e a tempTiles-ben
                int originalX = minUsedX + x;
                int originalY = minUsedY + y;

                if (originalX >= 0 && originalX < maxGridWidth &&
                        originalY >= 0 && originalY < maxGridHeight &&
                        tempTiles[originalX][originalY] != null) {
                    Tile originalTile = tempTiles[originalX][originalY];
                    finalTiles[x][y] = new Tile(originalTile.getType(), x, y, tileSize, originalTile.getTexture());
                } else {
                    // Ha valamiért az eredeti területen kívül esne, vagy null lenne, legyen fal
                    finalTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, tileSize, tileTextures.get(Tile.TileType.WALL));
                }
            }
        }

        // --- 4. Játékos spawn pozíciójának kiszámítása ---
        // A spawn szekció középpontját használjuk, majd korrigáljuk a levágott pálya koordinátáihoz
        float playerSpawnTileX = (float)(spawnSection.getCenterX() - minUsedX);
        float playerSpawnTileY = (float)(spawnSection.getCenterY() - minUsedY);

        // Biztonsági ellenőrzés: Győződjünk meg róla, hogy a játékos padlóra spawnol
        // Ez a rész maradhat, de a fenti korrekció után ritkábban kellene aktiválódnia
        if (playerSpawnTileX < 0 || playerSpawnTileX >= finalWidthTiles ||
                playerSpawnTileY < 0 || playerSpawnTileY >= finalHeightTiles ||
                finalTiles[(int)playerSpawnTileX][(int)playerSpawnTileY].getType() == Tile.TileType.WALL) {

            System.err.println("WARNING: Játékos falra spawnolna vagy pályán kívül! Keresek egy padlót a spawn szekcióban.");
            boolean foundSafeSpawn = false;
            // Végigmegyünk a spawn szekció belső területén, keresve egy padlót a végső csempemátrixban
            for (int yOffset = 1; yOffset < spawnSection.height - 1; yOffset++) {
                for (int xOffset = 1; xOffset < spawnSection.width - 1; xOffset++) {
                    int checkTempX = spawnSection.gridX + xOffset; // Eredeti tempTiles koordináta
                    int checkTempY = spawnSection.gridY + yOffset; // Eredeti tempTiles koordináta

                    // Átváltás a finalTiles koordinátarendszerébe
                    int finalCheckX = checkTempX - minUsedX;
                    int finalCheckY = checkTempY - minUsedY;

                    if (finalCheckX >= 0 && finalCheckX < finalWidthTiles &&
                            finalCheckY >= 0 && finalCheckY < finalHeightTiles &&
                            finalTiles[finalCheckX][finalCheckY].getType() == Tile.TileType.FLOOR) {

                        playerSpawnTileX = (float)finalCheckX;
                        playerSpawnTileY = (float)finalCheckY;
                        foundSafeSpawn = true;
                        break;
                    }
                }
                if (foundSafeSpawn) break;
            }
            if (!foundSafeSpawn) {
                System.err.println("ERROR: Nem találtam biztonságos spawn pontot a spawn szekcióban. Játékos 0,0-ra kerül.");
                playerSpawnTileX = 0;
                playerSpawnTileY = 0;
            }
        }

        // Pixel koordinátákra alakítás
        float playerSpawnPixelX = playerSpawnTileX * tileSize + tileSize / 2;
        float playerSpawnPixelY = playerSpawnTileY * tileSize + tileSize / 2;


        // --- 5. Ellenségek elhelyezése ---
        // Az ellenségeket is a levágott pálya koordinátáihoz igazítjuk
        for (Section section : sections) {
            if (section.type == SectionType.NORMAL) {
                int numEnemiesPerSection = random.nextInt(3) + 1;
                for (int i = 0; i < numEnemiesPerSection; i++) {
                    int enemyGridX = section.gridX + random.nextInt(section.width - 2) + 1; // Ellenség csempe X a tempTiles-ben
                    int enemyGridY = section.gridY + random.nextInt(section.height - 2) + 1; // Ellenség csempe Y a tempTiles-ben

                    int finalEnemyGridX = enemyGridX - minUsedX; // Átváltás finalTiles X-re
                    int finalEnemyGridY = enemyGridY - minUsedY; // Átváltás finalTiles Y-ra

                    if (finalEnemyGridX >= 0 && finalEnemyGridX < finalWidthTiles &&
                            finalEnemyGridY >= 0 && finalEnemyGridY < finalHeightTiles &&
                            finalTiles[finalEnemyGridX][finalEnemyGridY].getType() == Tile.TileType.FLOOR) {
                        float enemyX = finalEnemyGridX * tileSize + tileSize / 2;
                        float enemyY = finalEnemyGridY * tileSize + tileSize / 2;
                        enemies.add(new Enemy(enemyX, enemyY, 40, 40, enemyTexture, 50));
                    }
                }
            }
        }

        return new Dungeon(finalTiles, tileSize, enemies, playerSpawnPixelX, playerSpawnPixelY);
    }

    private static void drawSection(Tile[][] tiles, Section section, Map<Tile.TileType, Texture> tileTextures) {
        for (int x = section.gridX; x < section.gridX + section.width; x++) {
            for (int y = section.gridY; y < section.gridY + section.height; y++) {
                if (x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length) {
                    if (x == section.gridX || x == section.gridX + section.width - 1 ||
                            y == section.gridY || y == section.gridY + section.height - 1) {
                        tiles[x][y].setType(Tile.TileType.WALL);
                        tiles[x][y].setTexture(tileTextures.get(Tile.TileType.WALL));
                    } else {
                        tiles[x][y].setType(Tile.TileType.FLOOR);
                        tiles[x][y].setTexture(tileTextures.get(Tile.TileType.FLOOR));
                    }
                }
            }
        }
    }

    private static void drawCorridor(Tile[][] tiles, int x1, int y1, int x2, int y2, Map<Tile.TileType, Texture> tileTextures, int width) {
        int halfWidth = width / 2;

        if (x1 == x2) { // Függőleges folyosó
            // Biztosítjuk, hogy y1 és y2 a megfelelő sorrendben legyenek, hogy felfelé haladjon a ciklus
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                for (int xOffset = -halfWidth; xOffset < width - halfWidth; xOffset++) {
                    int currentX = x1 + xOffset;
                    if (currentX >= 0 && currentX < tiles.length &&
                            y >= 0 && y < tiles[0].length) {
                        tiles[currentX][y].setType(Tile.TileType.FLOOR);
                        tiles[currentX][y].setTexture(tileTextures.get(Tile.TileType.FLOOR));
                    }
                }
            }
        } else if (y1 == y2) { // Vízszintes folyosó
            // Biztosítjuk, hogy x1 és x2 a megfelelő sorrendben legyenek
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                for (int yOffset = -halfWidth; yOffset < width - halfWidth; yOffset++) {
                    int currentY = y1 + yOffset;
                    if (x >= 0 && x < tiles.length &&
                            currentY >= 0 && currentY < tiles[0].length) {
                        tiles[x][currentY].setType(Tile.TileType.FLOOR);
                        tiles[x][currentY].setTexture(tileTextures.get(Tile.TileType.FLOOR));
                    }
                }
            }
        }
    }

    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}