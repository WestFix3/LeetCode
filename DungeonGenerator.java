package world;

import entities.Enemy;
import entities.Boss;
import physics.CollisionManager;
import rendering.Texture;
import rendering.TextRenderer;
import entities.weapons.WeaponFactory;
import entities.Effect;
import java.util.concurrent.ThreadLocalRandom;

import java.util.*;

public class DungeonGenerator {
    public static Dungeon generateRandomDungeon(int tileSize, Map<Tile.TileType, Texture> tileTextures, Texture enemyTexture, Map<Integer, Texture> boxDamageTextures, Map<Integer, Texture> gateAnimationTextures, Texture weaponCrateTexture, Texture openCrateTexture, Texture emptyCrateTexture, WeaponFactory weaponFactory, TextRenderer textRenderer, Map<Effect.EffectType, Texture> effectTextures, Texture teleportPadTexture, Random random2) {

        final int TILE_SIZE = tileSize;
        final Random random = random2;

        gateCorridorGroups.clear();

        List<Room> rooms = new ArrayList<>();
        List<Enemy> enemies = new java.util.concurrent.CopyOnWriteArrayList<>();
        Set<OccupiedTile> occupiedPositions = new HashSet<>();

        Map<Integer, Texture> gateDestructionTextures = gateAnimationTextures;

        int maxGridWidthTiles = ROOM_GRID_COLS * ROOM_GRID_CELL_TILE_WIDTH;
        int maxGridHeightTiles = ROOM_GRID_ROWS * ROOM_GRID_CELL_TILE_HEIGHT;

        Tile[][] tempTiles = new Tile[maxGridWidthTiles][maxGridHeightTiles];

        // 1. Az összes csempe kitöltése fallal
        for (int x = 0; x < maxGridWidthTiles; x++) {
            for (int y = 0; y < maxGridHeightTiles; y++) {
                tempTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, TILE_SIZE, tileTextures.get(Tile.TileType.WALL));
            }
        }

        Map<String, Room> roomGridMap = new HashMap<>();

        // 2. Spawn szoba generálása
        int numRoomsToGenerate = random.nextInt(5) + 10;
        int spawnGridCol = random.nextInt(ROOM_GRID_COLS);
        int spawnGridRow = random.nextInt(ROOM_GRID_ROWS);

        int spawnRoomX = spawnGridCol * ROOM_GRID_CELL_TILE_WIDTH + random.nextInt(ROOM_GRID_CELL_TILE_WIDTH - MAX_ROOM_WIDTH_TILES);
        int spawnRoomY = spawnGridRow * ROOM_GRID_CELL_TILE_HEIGHT + random.nextInt(ROOM_GRID_CELL_TILE_HEIGHT - MAX_ROOM_HEIGHT_TILES);

        Room spawnRoom = new Room(spawnRoomX, spawnRoomY, MAX_ROOM_WIDTH_TILES, MAX_ROOM_HEIGHT_TILES, Room.RoomType.SPAWN);
        rooms.add(spawnRoom);
        roomGridMap.put(spawnGridCol + "," + spawnGridRow, spawnRoom);

        // Spawn szoba padlója
        for (int x = spawnRoom.getGridX() + 1; x < spawnRoom.getGridX() + spawnRoom.getWidth() - 1; x++) {
            for (int y = spawnRoom.getGridY() + 1; y < spawnRoom.getGridY() + spawnRoom.getHeight() - 1; y++) {
                if (isValidGridCoord(x, y, tempTiles)) {
                    tempTiles[x][y] = new Tile(Tile.TileType.FLOOR, x, y, TILE_SIZE, tileTextures.get(Tile.TileType.FLOOR));
                }
            }
        }

        int minUsedX = spawnRoom.getGridX();
        int maxUsedX = spawnRoom.getGridX() + spawnRoom.getWidth();
        int minUsedY = spawnRoom.getGridY();
        int maxUsedY = spawnRoom.getGridY() + spawnRoom.getHeight();

        // 3. További szobák generálása (BFS alapú)
        Queue<Room> roomsQueue = new LinkedList<>();
        roomsQueue.add(spawnRoom);
        Set<String> visitedRoomGridCells = new HashSet<>();
        visitedRoomGridCells.add(spawnGridCol + "," + spawnGridRow);
        int roomsPlacedCount = 1;

        while (!roomsQueue.isEmpty() && roomsPlacedCount < numRoomsToGenerate) {
            Room current = roomsQueue.poll();

            int currentCol = -1, currentRow = -1;
            for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
                if (entry.getValue().equals(current)) {
                    String[] coords = entry.getKey().split(",");
                    currentCol = Integer.parseInt(coords[0]);
                    currentRow = Integer.parseInt(coords[1]);
                    break;
                }
            }
            if (currentCol == -1 || currentRow == -1) continue;

            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            List<int[]> shuffledDirections = new ArrayList<>(List.of(directions));
            Collections.shuffle(shuffledDirections, random);

            for (int[] dir : shuffledDirections) {
                if (roomsPlacedCount >= numRoomsToGenerate) break;

                int newGridCol = currentCol + dir[0];
                int newGridRow = currentRow + dir[1];
                String cellKey = newGridCol + "," + newGridRow;

                if (newGridCol >= 0 && newGridCol < ROOM_GRID_COLS &&
                        newGridRow >= 0 && newGridRow < ROOM_GRID_ROWS &&
                        !visitedRoomGridCells.contains(cellKey) && !roomGridMap.containsKey(cellKey)) {

                    int newRoomWidth = MAX_ROOM_WIDTH_TILES;
                    int newRoomHeight = MAX_ROOM_HEIGHT_TILES;

                    int newRoomX = newGridCol * ROOM_GRID_CELL_TILE_WIDTH + random.nextInt(ROOM_GRID_CELL_TILE_WIDTH - newRoomWidth);
                    int newRoomY = newGridRow * ROOM_GRID_CELL_TILE_HEIGHT + random.nextInt(ROOM_GRID_CELL_TILE_HEIGHT - newRoomHeight);

                    if (newRoomX + newRoomWidth < maxGridWidthTiles && newRoomY + newRoomHeight < maxGridHeightTiles) {
                        Room newRoom = new Room(newRoomX, newRoomY, newRoomWidth, newRoomHeight, Room.RoomType.NORMAL);
                        rooms.add(newRoom);
                        roomGridMap.put(cellKey, newRoom);
                        visitedRoomGridCells.add(cellKey);

                        newRoom.draw(tempTiles, TILE_SIZE, tileTextures, boxDamageTextures);

                        minUsedX = Math.min(minUsedX, newRoom.getGridX());
                        maxUsedX = Math.max(maxUsedX, newRoom.getGridX() + newRoom.getWidth());
                        minUsedY = Math.min(minUsedY, newRoom.getGridY());
                        maxUsedY = Math.max(maxUsedY, newRoom.getGridY() + newRoom.getHeight());

                        roomsQueue.add(newRoom);
                        roomsPlacedCount++;
                    }
                }
            }
        }

        // 4. Boss szoba kijelölése és méretezése
        Map<Room, Integer> distances = new HashMap<>();
        Queue<Room> roomDistanceQueue = new LinkedList<>();

        distances.put(spawnRoom, 0);
        roomDistanceQueue.add(spawnRoom);

        while(!roomDistanceQueue.isEmpty()) {
            Room currentDistRoom = roomDistanceQueue.poll();
            int currentDist = distances.get(currentDistRoom);

            int currentDistCol = -1, currentDistRow = -1;
            for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
                if (entry.getValue().equals(currentDistRoom)) {
                    String[] coords = entry.getKey().split(",");
                    currentDistCol = Integer.parseInt(coords[0]);
                    currentDistRow = Integer.parseInt(coords[1]);
                    break;
                }
            }
            if (currentDistCol == -1 || currentDistRow == -1) continue;

            int[][] dirNeighbours = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] dir : dirNeighbours) {
                int neighborCol = currentDistCol + dir[0];
                int neighborRow = currentDistRow + dir[1];
                String neighborKey = neighborCol + "," + neighborRow;

                if (roomGridMap.containsKey(neighborKey)) {
                    Room neighborRoom = roomGridMap.get(neighborKey);
                    if (!distances.containsKey(neighborRoom)) {
                        distances.put(neighborRoom, currentDist + 1);
                        roomDistanceQueue.add(neighborRoom);
                    }
                }
            }
        }

        Room bossRoomCandidate = null;
        Room actualBossRoom = null;
        List<Room> potentialBossRooms = new ArrayList<>(rooms);
        potentialBossRooms.sort(Comparator.comparingInt(r -> distances.getOrDefault(r, 0)).reversed());

        for (Room room : potentialBossRooms) {
            if (room.getType() == Room.RoomType.NORMAL) {
                bossRoomCandidate = room;
                break;
            }
        }

        if (bossRoomCandidate == null) {
            bossRoomCandidate = spawnRoom;
            bossRoomCandidate.type = Room.RoomType.BOSS_ROOM;
            actualBossRoom = bossRoomCandidate;
            System.out.println("Warning: No suitable boss room found, using spawn room as fallback boss room.");
        } else {
            rooms.remove(bossRoomCandidate);
            int newBossWidth = (int)(bossRoomCandidate.getWidth() * BOSS_ROOM_SCALE_FACTOR);
            int newBossHeight = (int)(bossRoomCandidate.getHeight() * BOSS_ROOM_SCALE_FACTOR);
            newBossWidth = Math.max(newBossWidth, 3);
            newBossHeight = Math.max(newBossHeight, 3);
            int proposedNewBossX = bossRoomCandidate.getCenterX() - newBossWidth / 2;
            int proposedNewBossY = bossRoomCandidate.getCenterY() - newBossHeight / 2;
            proposedNewBossX = Math.max(0, proposedNewBossX);
            proposedNewBossY = Math.max(0, proposedNewBossY);
            newBossWidth = Math.min(newBossWidth, maxGridWidthTiles - proposedNewBossX);
            newBossHeight = Math.min(newBossHeight, maxGridHeightTiles - proposedNewBossY);
            actualBossRoom = new Room(proposedNewBossX, proposedNewBossY, newBossWidth, newBossHeight, Room.RoomType.BOSS_ROOM);

            String bossRoomKey = null;
            for(Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
                if (entry.getValue().equals(bossRoomCandidate)) {
                    bossRoomKey = entry.getKey();
                    break;
                }
            }
            if (bossRoomKey != null) {
                roomGridMap.put(bossRoomKey, actualBossRoom);
            }
            rooms.add(actualBossRoom);

            actualBossRoom.draw(tempTiles, TILE_SIZE, tileTextures, boxDamageTextures);

            minUsedX = Math.min(minUsedX, actualBossRoom.getGridX());
            maxUsedX = Math.max(maxUsedX, actualBossRoom.getGridX() + actualBossRoom.getWidth());
            minUsedY = Math.min(minUsedY, actualBossRoom.getGridY());
            maxUsedY = Math.max(maxUsedY, actualBossRoom.getGridY() + actualBossRoom.getHeight());
        }

        // 5. SHOP szoba elhelyezése
        Room shopRoom = null;
        if (random.nextDouble() < 0.3) {
            int shopRoomMinX = spawnRoom.getGridX() + SHOP_WALL_OFFSET;
            int shopRoomMinY = spawnRoom.getGridY() + SHOP_WALL_OFFSET;
            int shopInnerWidth = SHOP_INNER_ROOM_SIZE;
            int shopInnerHeight = SHOP_INNER_ROOM_SIZE;
            if (shopRoomMinX + shopInnerWidth < spawnRoom.getGridX() + spawnRoom.getWidth() &&
                    shopRoomMinY + shopInnerHeight < spawnRoom.getGridY() + spawnRoom.getHeight()) {
                shopRoom = new Room(shopRoomMinX, shopRoomMinY, shopInnerWidth, shopInnerHeight, Room.RoomType.SHOP);
                for (int x = shopRoomMinX; x < shopRoomMinX + shopInnerWidth; x++) {
                    for (int y = shopRoomMinY; y < shopRoomMinY + shopInnerHeight; y++) {
                        if (isValidGridCoord(x, y, tempTiles)) {
                            tempTiles[x][y] = new Tile(Tile.TileType.SHOP_FLOOR, x, y, TILE_SIZE, tileTextures.get(Tile.TileType.SHOP_FLOOR));
                        }
                    }
                }
            } else {
                System.out.println("Warning: Shop area does not fit in spawn room.");
            }
        }

        // 6. Folyosók generálása (MST + Extra)
        Set<Room> mstRooms = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>();

        mstRooms.add(spawnRoom);
        addEdgesToPriorityQueue(spawnRoom, rooms, pq, roomGridMap);

        while (!pq.isEmpty() && mstRooms.size() < rooms.size()) {
            Edge lightestEdge = pq.poll();
            Room r1 = lightestEdge.source;
            Room r2 = lightestEdge.destination;
            boolean r1InMst = mstRooms.contains(r1);
            boolean r2InMst = mstRooms.contains(r2);
            if (r1InMst && r2InMst) {
                continue;
            }
            if (r1InMst || r2InMst) {
                Room roomToAddToMst = r1InMst ? r2 : r1;
                Room connectedRoom = r1InMst ? r1 : r2;
                mstRooms.add(roomToAddToMst);
                List<Tile> newGateGroup = new ArrayList<>();
                generateCorridorBetweenRooms(tempTiles, connectedRoom, roomToAddToMst, tileTextures, gateDestructionTextures, newGateGroup);
                gateCorridorGroups.add(newGateGroup);
                addEdgesToPriorityQueue(roomToAddToMst, rooms, pq, roomGridMap);
            }
        }

//        // Extra folyosók
//        int extraCorridorsToDraw = random.nextInt(rooms.size() / 2) + 1;
//        int attempts = 0;
//        final int MAX_ATTEMPTS = 100;
//
//        while (extraCorridorsToDraw > 0 && attempts < MAX_ATTEMPTS) {
//            Room r1 = rooms.get(random.nextInt(rooms.size()));
//            Room r2 = rooms.get(random.nextInt(rooms.size()));
//            int r1Col = -1, r1Row = -1;
//            int r2Col = -1, r2Row = -1;
//            for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
//                if (entry.getValue().equals(r1)) {
//                    String[] coords = entry.getKey().split(",");
//                    r1Col = Integer.parseInt(coords[0]);
//                    r1Row = Integer.parseInt(coords[1]);
//                }
//                if (entry.getValue().equals(r2)) {
//                    String[] coords = entry.getKey().split(",");
//                    r2Col = Integer.parseInt(coords[0]);
//                    r2Row = Integer.parseInt(coords[1]);
//                }
//            }
//            if (r1 != r2 && r1Col != -1 && r2Col != -1 &&
//                    Math.abs(r1Col - r2Col) <= 1 && Math.abs(r1Row - r2Row) <= 1) {
//                List<Tile> newGateGroup = new ArrayList<>();
//                generateCorridorBetweenRooms(tempTiles, r1, r2, tileTextures, gateDestructionTextures, newGateGroup);
//                gateCorridorGroups.add(newGateGroup);
//                extraCorridorsToDraw--;
//            }
//            attempts++;
//        }

        // 7. A pálya méretének levágása és a finalTiles létrehozása - JAVÍTOTT VERZIÓ
        int margin = 5;
        minUsedX = Math.max(0, minUsedX - margin);
        maxUsedX = Math.min(maxGridWidthTiles - 1, maxUsedX + margin);
        minUsedY = Math.max(0, minUsedY - margin);
        maxUsedY = Math.min(maxGridHeightTiles - 1, maxUsedY + margin);

        int finalWidthTiles = maxUsedX - minUsedX + 1;
        int finalHeightTiles = maxUsedY - minUsedY + 1;
        Tile[][] finalTiles = new Tile[finalWidthTiles][finalHeightTiles];

// ⚠️ KOORDINÁTA ALAPÚ MAPPING - ez biztosan működik!
        Map<String, Tile> coordinateToFinalGateTiles = new HashMap<>();

        for (int x = 0; x < finalWidthTiles; x++) {
            for (int y = 0; y < finalHeightTiles; y++) {
                int originalX = minUsedX + x;
                int originalY = minUsedY + y;

                if (isValidGridCoord(originalX, originalY, tempTiles) && tempTiles[originalX][originalY] != null) {
                    Tile originalTile = tempTiles[originalX][originalY];

                    if (originalTile.getType() == Tile.TileType.GATE) {
                        Tile finalGateTile = new Tile(
                                Tile.TileType.GATE,
                                x, y,  // FINAL koordináták
                                TILE_SIZE,
                                originalTile.getTexture(),
                                originalTile.getDamageTextures()
                        );
                        finalTiles[x][y] = finalGateTile;

                        // ⚠️ KOORDINÁTA ALAPÚ MAPPING
                        String coordKey = originalX + "," + originalY;
                        coordinateToFinalGateTiles.put(coordKey, finalGateTile);

                        System.out.println("Mapped gate by coordinates: " + coordKey + " -> final " + x + "," + y);

                    } else if (originalTile.getType() == Tile.TileType.BOX || originalTile.getType() == Tile.TileType.DESTRUCTIBLE_WALL) {
                        finalTiles[x][y] = new Tile(
                                originalTile.getType(), x, y, TILE_SIZE,
                                originalTile.getTexture(), originalTile.getHealth(), originalTile.getDamageTextures()
                        );
                    } else if (originalTile.getType() == Tile.TileType.WEAPON_CRATE) {
                        finalTiles[x][y] = new Tile(
                                originalTile.getType(), x, y, TILE_SIZE,
                                originalTile.getTexture(), originalTile.getEmptyTexture(), originalTile.getWeaponId()
                        );
                    } else {
                        finalTiles[x][y] = new Tile(
                                originalTile.getType(), x, y, TILE_SIZE, originalTile.getTexture()
                        );
                    }
                } else {
                    finalTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, TILE_SIZE, tileTextures.get(Tile.TileType.WALL));
                }
            }
        }

// ⚠️ KOORDINÁTA ALAPÚ FRISSÍTÉS
        List<List<Tile>> correctedGateCorridorGroups = new ArrayList<>();
        for (List<Tile> originalGateGroup : gateCorridorGroups) {
            List<Tile> correctedGroup = new ArrayList<>();
            for (Tile originalGateTile : originalGateGroup) {
                // Koordináta alapú keresés
                String coordKey = originalGateTile.getGridX() + "," + originalGateTile.getGridY();
                Tile correctedTile = coordinateToFinalGateTiles.get(coordKey);

                if (correctedTile != null) {
                    correctedGroup.add(correctedTile);
                    System.out.println("✓ Found gate by coordinates: " + coordKey + " -> final " + correctedTile.getGridX() + "," + correctedTile.getGridY());
                } else {
                    System.out.println("❌ WARNING: Gate tile not found by coordinates: " + coordKey);
                    // További debug info
                    System.out.println("   Original gate - Type: " + originalGateTile.getType() + ", Coords: " + originalGateTile.getGridX() + "," + originalGateTile.getGridY());
                }
            }

            if (!correctedGroup.isEmpty()) {
                correctedGateCorridorGroups.add(correctedGroup);
                System.out.println("✓ Corrected gate group has " + correctedGroup.size() + " tiles");
            } else {
                System.out.println("❌ WARNING: Empty corrected gate group!");
                // További debug
                System.out.println("   Original group had " + originalGateGroup.size() + " tiles");
                for (Tile t : originalGateGroup) {
                    System.out.println("   Original tile: " + t.getGridX() + "," + t.getGridY() + " - " + t.getType());
                }
            }
        }
        gateCorridorGroups = correctedGateCorridorGroups;

        // 8. Változók kiszámítása a Dungeon konstruktorhoz
        float playerSpawnPixelX = (float)((spawnRoom.getCenterX() - minUsedX) * TILE_SIZE + TILE_SIZE / 2);
        float playerSpawnPixelY = (float)((spawnRoom.getCenterY() - minUsedY) * TILE_SIZE + TILE_SIZE / 2);

        int correctedBossGridX = 0;
        int correctedBossGridY = 0;
        int bossRoomWidth = 0;
        int bossRoomHeight = 0;

        if (actualBossRoom != null) {
            correctedBossGridX = actualBossRoom.getGridX() - minUsedX;
            correctedBossGridY = actualBossRoom.getGridY() - minUsedY;
            bossRoomWidth = actualBossRoom.getWidth();
            bossRoomHeight = actualBossRoom.getHeight();
        }

        // 9. DUNGEON LÉTREHOZÁSA
        Dungeon dungeon = new Dungeon(
                finalTiles,
                TILE_SIZE,
                enemies,
                playerSpawnPixelX,
                playerSpawnPixelY,
                gateCorridorGroups,
                correctedBossGridX,
                correctedBossGridY,
                bossRoomWidth,
                bossRoomHeight
        );

        CollisionManager collisionManager = new CollisionManager(dungeon);

        // ⚠️ DEBUG ELLENŐRZÉSEK - A DUNGEON LÉTREHOZÁSA UTÁN ⚠️
        System.out.println("=== FINAL GATE VERIFICATION ===");
        System.out.println("minUsedX: " + minUsedX + ", minUsedY: " + minUsedY);
        System.out.println("finalTiles size: " + finalTiles.length + " x " + finalTiles[0].length);

        // Végső Gate számolás
        int finalGateCount = 0;
        for (int x = 0; x < finalTiles.length; x++) {
            for (int y = 0; y < finalTiles[0].length; y++) {
                if (finalTiles[x][y].getType() == Tile.TileType.GATE) {
                    finalGateCount++;
                }
            }
        }
        System.out.println("FINAL GATE COUNT: " + finalGateCount);
        System.out.println("Gate groups: " + gateCorridorGroups.size());

        // 10. ELLENSÉGEK ÉS LÁDÁK GENERÁLÁSA

        // Boss ellenség
        if (actualBossRoom != null) {
            int bossSpawnX = correctedBossGridX + actualBossRoom.getWidth() / 2;
            int bossSpawnY = correctedBossGridY + actualBossRoom.getHeight() / 2;
            float finalBossSpawnX = (float)bossSpawnX * TILE_SIZE + TILE_SIZE / 2.0f - actualBossRoom.getWidth() / 2.0f;
            float finalBossSpawnY = (float)bossSpawnY * TILE_SIZE + TILE_SIZE / 2.0f - actualBossRoom.getHeight() / 2.0f;

            if (isValidGridCoord(bossSpawnX, bossSpawnY, finalTiles) &&
                    finalTiles[bossSpawnX][bossSpawnY].getType() == Tile.TileType.FLOOR &&
                    !occupiedPositions.contains(new OccupiedTile(bossSpawnX, bossSpawnY))) {

                Boss bossEnemy = new Boss(
                        finalBossSpawnX,
                        finalBossSpawnY,
                        TILE_SIZE * 2, TILE_SIZE * 2,
                        enemyTexture,
                        500,
                        textRenderer,
                        dungeon,
                        collisionManager,
                        null
                );

                bossEnemy.setMoveSpeed(35.0f);
                bossEnemy.setAttackDamage(40.0f);

                enemies.add(bossEnemy);
                occupiedPositions.add(new OccupiedTile(bossSpawnX, bossSpawnY));
                System.out.println("Generated BOSS at corrected grid: " + bossSpawnX + ", " + bossSpawnY);
            } else {
                System.out.println("Warning: Boss entity could not be placed in boss room (corrected: " + bossSpawnX + "," + bossSpawnY + ") - already occupied or invalid.");
            }
        }

        // Sima ellenségek
        for (Room room : rooms) {
            if (room.getType() == Room.RoomType.SPAWN || room.getType() == Room.RoomType.BOSS_ROOM || room.getType() == Room.RoomType.SHOP) {
                continue;
            }
            if (room.getType() == Room.RoomType.NORMAL) {
                int numEnemies = random.nextInt(3) + 2;
                for (int i = 0; i < numEnemies; i++) {
                    int correctedEnemyGridX = -1;
                    int correctedEnemyGridY = -1;
                    boolean placed = false;
                    int placementAttempts = 0;
                    final int MAX_PLACEMENT_ATTEMPTS = 50;

                    while (!placed && placementAttempts < MAX_PLACEMENT_ATTEMPTS) {
                        int originalEnemyGridX = room.getGridX() + 1 + random.nextInt(room.getWidth() - 2);
                        int originalEnemyGridY = room.getGridY() + 1 + random.nextInt(room.getHeight() - 2);

                        correctedEnemyGridX = originalEnemyGridX - minUsedX;
                        correctedEnemyGridY = originalEnemyGridY - minUsedY;

                        if (isValidGridCoord(correctedEnemyGridX, correctedEnemyGridY, finalTiles) &&
                                finalTiles[correctedEnemyGridX][correctedEnemyGridY].getType() == Tile.TileType.FLOOR &&
                                !occupiedPositions.contains(new OccupiedTile(correctedEnemyGridX, correctedEnemyGridY))) {
                            float enemyWidth = TILE_SIZE;
                            float enemyHeight = TILE_SIZE;
                            float finalEnemySpawnX = (float)correctedEnemyGridX * TILE_SIZE + TILE_SIZE / 2.0f - enemyWidth / 2.0f;
                            float finalEnemySpawnY = (float)correctedEnemyGridY * TILE_SIZE + TILE_SIZE / 2.0f - enemyHeight / 2.0f;

                            int initialHealth = 100;

                            enemies.add(new Enemy(
                                    finalEnemySpawnX,
                                    finalEnemySpawnY,
                                    enemyWidth, enemyHeight, enemyTexture, initialHealth,
                                    textRenderer,
                                    dungeon,
                                    collisionManager,
                                    null
                            ));
                            occupiedPositions.add(new OccupiedTile(correctedEnemyGridX, correctedEnemyGridY));
                            placed = true;
                        }
                        placementAttempts++;
                    }
                    if (!placed) {
                        System.out.println("Warning: Could not place enemy in room after " + MAX_PLACEMENT_ATTEMPTS + " attempts.");
                    }
                }
            }
        }

        // Boxok elhelyezése
        for (Room room : rooms) {
            if (room.getType() == Room.RoomType.SPAWN || room.getType() == Room.RoomType.SHOP) {
                continue;
            }
            if (room.getType() == Room.RoomType.NORMAL || room.getType() == Room.RoomType.BOSS_ROOM) {
                int numBoxes = random.nextInt(3) + 1;
                for (int i = 0; i < numBoxes; i++) {
                    int correctedBoxGridX = -1;
                    int correctedBoxGridY = -1;
                    boolean placed = false;
                    int placementAttempts = 0;
                    final int MAX_PLACEMENT_ATTEMPTS = 50;

                    while (!placed && placementAttempts < MAX_PLACEMENT_ATTEMPTS) {
                        int originalBoxGridX = room.getGridX() + 1 + random.nextInt(room.getWidth() - 2);
                        int originalBoxGridY = room.getGridY() + 1 + random.nextInt(room.getHeight() - 2);

                        correctedBoxGridX = originalBoxGridX - minUsedX;
                        correctedBoxGridY = originalBoxGridY - minUsedY;

                        if (isValidGridCoord(correctedBoxGridX, correctedBoxGridY, finalTiles) &&
                                (finalTiles[correctedBoxGridX][correctedBoxGridY].getType() == Tile.TileType.FLOOR || finalTiles[correctedBoxGridX][correctedBoxGridY].getType() == Tile.TileType.SHOP_FLOOR) &&
                                !occupiedPositions.contains(new OccupiedTile(correctedBoxGridX, correctedBoxGridY))) {

                            finalTiles[correctedBoxGridX][correctedBoxGridY] = new Tile(
                                    Tile.TileType.BOX,
                                    correctedBoxGridX,
                                    correctedBoxGridY,
                                    TILE_SIZE,
                                    tileTextures.get(Tile.TileType.BOX),
                                    3.0f,
                                    boxDamageTextures
                            );
                            occupiedPositions.add(new OccupiedTile(correctedBoxGridX, correctedBoxGridY));
                            placed = true;
                        }
                        placementAttempts++;
                    }
                    if (!placed) {
                        System.out.println("Warning: Could not place box in room after " + MAX_PLACEMENT_ATTEMPTS + " attempts.");
                    }
                }
            }
        }

        // Fegyverládák elhelyezése a shop szobában
        if (shopRoom != null) {
            int shopCenterX = shopRoom.getGridX() - minUsedX + shopRoom.getWidth() / 2;
            int shopCenterY = shopRoom.getGridY() - minUsedY + shopRoom.getHeight() / 2;

            int crate1X = shopCenterX - 2;
            int crate1Y = shopCenterY;
            int crate2X = shopCenterX + 2;
            int crate2Y = shopCenterY;

            // 1. láda
            if (isValidGridCoord(crate1X, crate1Y, finalTiles) && (finalTiles[crate1X][crate1Y].getType() == Tile.TileType.FLOOR || finalTiles[crate1X][crate1Y].getType() == Tile.TileType.SHOP_FLOOR)) {
                String randomWeaponId1 = weaponFactory.getRandomWeaponId();
                finalTiles[crate1X][crate1Y] = new Tile(
                        Tile.TileType.WEAPON_CRATE, crate1X, crate1Y, TILE_SIZE, weaponCrateTexture, emptyCrateTexture, randomWeaponId1
                );
                occupiedPositions.add(new OccupiedTile(crate1X, crate1Y));
            } else {
                System.out.println("Warning: Could not place weapon crate 1 in shop room.");
            }

            // 2. láda
            if (isValidGridCoord(crate2X, crate2Y, finalTiles) && (finalTiles[crate2X][crate2Y].getType() == Tile.TileType.FLOOR || finalTiles[crate2X][crate2Y].getType() == Tile.TileType.SHOP_FLOOR)) {
                String randomWeaponId2 = weaponFactory.getRandomWeaponId();
                finalTiles[crate2X][crate2Y] = new Tile(
                        Tile.TileType.WEAPON_CRATE, crate2X, crate2Y, TILE_SIZE, weaponCrateTexture, emptyCrateTexture, randomWeaponId2
                );
                occupiedPositions.add(new OccupiedTile(crate2X, crate2Y));
            } else {
                System.out.println("Warning: Could not place weapon crate 2 in shop room.");
            }
        }

        // 11. Visszatérés a Dungeon objektummal
        return dungeon;
    }

    private static void addEdgesToPriorityQueue(Room startRoom, List<Room> allRooms, PriorityQueue<Edge> pq, Map<String, Room> roomGridMap) {
        int startCol = -1, startRow = -1;
        for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
            if (entry.getValue().equals(startRoom)) {
                String[] coords = entry.getKey().split(",");
                startCol = Integer.parseInt(coords[0]);
                startRow = Integer.parseInt(coords[1]);
                break;
            }
        }
        if (startCol == -1 || startRow == -1) return;

        for (Room otherRoom : allRooms) {
            if (!otherRoom.equals(startRoom)) {
                int otherCol = -1, otherRow = -1;
                for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
                    if (entry.getValue().equals(otherRoom)) {
                        String[] coords = entry.getKey().split(",");
                        otherCol = Integer.parseInt(coords[0]);
                        otherRow = Integer.parseInt(coords[1]);
                        break;
                    }
                }
                if (otherCol != -1 && otherRow != -1) {
                    int dx = Math.abs(startCol - otherCol);
                    int dy = Math.abs(startRow - otherRow);
                    int weight = dx + dy;
                    pq.add(new Edge(startRoom, otherRoom, weight));
                }
            }
        }
    }

    private static final int TILE_SIZE = 53;
    private static final int MIN_ROOM_WIDTH_TILES = 20;
    private static final int MAX_ROOM_WIDTH_TILES = 20;
    private static final int MIN_ROOM_HEIGHT_TILES = 20;
    private static final int MAX_ROOM_HEIGHT_TILES = 20;
    private static final double BOSS_ROOM_SCALE_FACTOR = 0.5;
    private static final int CORRIDOR_WIDTH_TILES = 3;
    private static final int SHOP_INNER_ROOM_SIZE = 4;

    private static final int SHOP_WALL_OFFSET = 1;
    private static final int ROOM_GRID_COLS = 5;
    private static final int ROOM_GRID_ROWS = 5;
    private static final int ROOM_GRID_CELL_TILE_WIDTH = (MAX_ROOM_WIDTH_TILES + CORRIDOR_WIDTH_TILES + 4);

    private static final int ROOM_GRID_CELL_TILE_HEIGHT = (MAX_ROOM_HEIGHT_TILES + CORRIDOR_WIDTH_TILES + 4);

    private static Random random = new Random();

    public static List<List<Tile>> gateCorridorGroups = new ArrayList<>();
    private static class Edge implements Comparable<Edge> {
        Room source;
        Room destination;

        int weight;

        public Edge(Room source, Room destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

    }
    private static class OccupiedTile {

        int x, y;

        public OccupiedTile(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OccupiedTile that = (OccupiedTile) o;
            return x == that.x && y == that.y;
        }
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

    }

    private static void generateCorridorBetweenRooms(Tile[][] tiles, Room r1, Room r2,
                                                     Map<Tile.TileType, Texture> tileTextures,
                                                     Map<Integer, Texture> gateDestructionTextures,
                                                     List<Tile> gateGroup) {

        int startX = r1.getCenterX();
        int startY = r1.getCenterY();
        int endX = r2.getCenterX();
        int endY = r2.getCenterY();

        boolean horizontalFirst = Math.abs(startX - endX) > Math.abs(startY - endY);

        int gateX, gateY;

        if (horizontalFirst) {
            // 1. Vízszintes szakasz
            drawCorridorSegment(tiles, startX, startY, endX, startY, tileTextures, CORRIDOR_WIDTH_TILES);
            // 2. Függőleges szakasz
            drawCorridorSegment(tiles, endX, startY, endX, endY, tileTextures, CORRIDOR_WIDTH_TILES);

            // ⚠️ JAVÍTÁS: Gate a VÍZSZINTES szakasz KÖZEPÉRE
            int horizontalMidX = startX + (endX - startX) / 2;
            gateX = horizontalMidX;
            gateY = startY;

        } else {
            // 1. Függőleges szakasz
            drawCorridorSegment(tiles, startX, startY, startX, endY, tileTextures, CORRIDOR_WIDTH_TILES);
            // 2. Vízszintes szakasz
            drawCorridorSegment(tiles, startX, endY, endX, endY, tileTextures, CORRIDOR_WIDTH_TILES);

            // ⚠️ JAVÍTÁS: Gate a FÜGGŐLEGES szakasz KÖZEPÉRE
            int verticalMidY = startY + (endY - startY) / 2;
            gateX = startX;
            gateY = verticalMidY;
        }

        // Debug ellenőrzés
        System.out.println("Gate center calculated at: " + gateX + "," + gateY);

        // Gate elhelyezése
        drawGateSegment(tiles, gateX, gateY, tileTextures, CORRIDOR_WIDTH_TILES, gateDestructionTextures, gateGroup);
    }

    private static void drawCorridorSegment(Tile[][] tiles, int x1, int y1, int x2, int y2,
                                            Map<Tile.TileType, Texture> tileTextures, int width) {

        int halfWidth = width / 2;
        int tilesChanged = 0;
        int gatesPreserved = 0;

        if (x1 == x2) {
            // Függőleges folyosó
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                for (int xOffset = -halfWidth; xOffset <= halfWidth; xOffset++) {
                    int currentX = x1 + xOffset;
                    if (isValidGridCoord(currentX, y, tiles)) {
                        // ⚠️ FONTOS: Gate-eket SOHA ne írjuk felül!
                        if (tiles[currentX][y].getType() == Tile.TileType.GATE) {
                            gatesPreserved++;
                            continue; // Gate megtartása
                        }
                        // Csak WALL-t cseréljünk FLOOR-ra
                        if (tiles[currentX][y].getType() == Tile.TileType.WALL) {
                            tiles[currentX][y] = new Tile(Tile.TileType.FLOOR, currentX, y, TILE_SIZE, tileTextures.get(Tile.TileType.FLOOR));
                            tilesChanged++;
                        }
                    }
                }
            }
        } else if (y1 == y2) {
            // Vízszintes folyosó
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                for (int yOffset = -halfWidth; yOffset <= halfWidth; yOffset++) {
                    int currentY = y1 + yOffset;
                    if (isValidGridCoord(x, currentY, tiles)) {
                        // ⚠️ FONTOS: Gate-eket SOHA ne írjuk felül!
                        if (tiles[x][currentY].getType() == Tile.TileType.GATE) {
                            gatesPreserved++;
                            continue; // Gate megtartása
                        }
                        // Csak WALL-t cseréljünk FLOOR-ra
                        if (tiles[x][currentY].getType() == Tile.TileType.WALL) {
                            tiles[x][currentY] = new Tile(Tile.TileType.FLOOR, x, currentY, TILE_SIZE, tileTextures.get(Tile.TileType.FLOOR));
                            tilesChanged++;
                        }
                    }
                }
            }
        }

        System.out.println("Corridor segment drawn: " + tilesChanged + " WALL->FLOOR, " + gatesPreserved + " gates preserved");
    }

    private static void drawGateSegment(Tile[][] tiles, int centerX, int centerY,
                                        Map<Tile.TileType, Texture> tileTextures, int width,
                                        Map<Integer, Texture> gateDestructionTextures, List<Tile> gateGroup) {

        // ⚠️ JAVÍTÁS: Gate méret pontos számítása
        int totalWidth = width;  // CORRIDOR_WIDTH_TILES = 3
        int totalHeight = width; // Mindig négyzet alakú

        System.out.println("Drawing gate segment at center: " + centerX + "," + centerY + " with size: " + totalWidth + "x" + totalHeight);

        int startX = centerX - totalWidth / 2;
        int startY = centerY - totalHeight / 2;

        int gatesPlaced = 0;

        for (int xOffset = 0; xOffset < totalWidth; xOffset++) {
            for (int yOffset = 0; yOffset < totalHeight; yOffset++) {
                int currentX = startX + xOffset;
                int currentY = startY + yOffset;

                if (isValidGridCoord(currentX, currentY, tiles)) {
                    // ⚠️ MINDEN pozíciót GATE-re kell cserélni, nem csak a FLOOR-okat
                    Texture currentGateTexture = gateDestructionTextures.get(0);

                    Tile gateTile = new Tile(
                            Tile.TileType.GATE,
                            currentX, currentY,
                            TILE_SIZE,
                            currentGateTexture,
                            gateDestructionTextures
                    );

                    gateGroup.add(gateTile);
                    tiles[currentX][currentY] = gateTile;
                    gatesPlaced++;

                    System.out.println("  Placed GATE at: " + currentX + "," + currentY);
                }
            }
        }

        System.out.println("Total gates placed: " + gatesPlaced + " (expected: " + (totalWidth * totalHeight) + ")");

        // ⚠️ ELLENŐRZÉS: Nézzük meg, tényleg GATE-ek lettek-e
        int verifiedGates = 0;
        for (int xOffset = 0; xOffset < totalWidth; xOffset++) {
            for (int yOffset = 0; yOffset < totalHeight; yOffset++) {
                int checkX = startX + xOffset;
                int checkY = startY + yOffset;
                if (isValidGridCoord(checkX, checkY, tiles) && tiles[checkX][checkY].getType() == Tile.TileType.GATE) {
                    verifiedGates++;
                }
            }
        }
        System.out.println("Verified gates in area: " + verifiedGates);

        if (verifiedGates != totalWidth * totalHeight) {
            System.out.println("❌ ERROR: Gate placement incomplete! Expected " + (totalWidth * totalHeight) + ", got " + verifiedGates);
        }
    }

    private static boolean isValidGridCoord(int x, int y, Tile[][] tiles) {
        return x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length;
    }
}