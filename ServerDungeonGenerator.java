package world;

import java.util.Collections;
import entities.Enemy;
import entities.Boss;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerDungeonGenerator {
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
    private static List<List<Tile>> gateCorridorGroups = new ArrayList<>();

    public static Dungeon generateRandomDungeon(int tileSize, Random random) {
        gateCorridorGroups.clear();
        List<Room> rooms = new ArrayList<>();
        List<Enemy> enemies = new CopyOnWriteArrayList<>();
        Set<OccupiedTile> occupiedPositions = new HashSet<>();
        int maxGridWidthTiles = ROOM_GRID_COLS * ROOM_GRID_CELL_TILE_WIDTH;
        int maxGridHeightTiles = ROOM_GRID_ROWS * ROOM_GRID_CELL_TILE_HEIGHT;
        Tile[][] tempTiles = new Tile[maxGridWidthTiles][maxGridHeightTiles];

        for (int x = 0; x < maxGridWidthTiles; x++) {
            for (int y = 0; y < maxGridHeightTiles; y++) {
                tempTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, TILE_SIZE, null);
            }
        }

        Map<String, Room> roomGridMap = new HashMap<>();
        int numRoomsToGenerate = random.nextInt(5) + 10;
        int spawnGridCol = random.nextInt(ROOM_GRID_COLS);
        int spawnGridRow = random.nextInt(ROOM_GRID_ROWS);
        int spawnRoomX = spawnGridCol * ROOM_GRID_CELL_TILE_WIDTH + random.nextInt(ROOM_GRID_CELL_TILE_WIDTH - MAX_ROOM_WIDTH_TILES);
        int spawnRoomY = spawnGridRow * ROOM_GRID_CELL_TILE_HEIGHT + random.nextInt(ROOM_GRID_CELL_TILE_HEIGHT - MAX_ROOM_HEIGHT_TILES);
        Room spawnRoom = new Room(spawnRoomX, spawnRoomY, MAX_ROOM_WIDTH_TILES, MAX_ROOM_HEIGHT_TILES, Room.RoomType.SPAWN);
        rooms.add(spawnRoom);
        roomGridMap.put(spawnGridCol + "," + spawnGridRow, spawnRoom);

        for (int x = spawnRoom.getGridX() + 1; x < spawnRoom.getGridX() + spawnRoom.getWidth() - 1; x++) {
            for (int y = spawnRoom.getGridY() + 1; y < spawnRoom.getGridY() + spawnRoom.getHeight() - 1; y++) {
                if (isValidGridCoord(x, y, tempTiles)) {
                    tempTiles[x][y] = new Tile(Tile.TileType.FLOOR, x, y, TILE_SIZE, null);
                }
            }
        }

        int minUsedX = spawnRoom.getGridX();
        int maxUsedX = spawnRoom.getGridX() + spawnRoom.getWidth();
        int minUsedY = spawnRoom.getGridY();
        int maxUsedY = spawnRoom.getGridY() + spawnRoom.getHeight();

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
                        newRoom.draw(tempTiles, TILE_SIZE, null, null);
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

        Map<Room, Integer> distances = new HashMap<>();
        Queue<Room> roomDistanceQueue = new LinkedList<>();
        distances.put(spawnRoom, 0);
        roomDistanceQueue.add(spawnRoom);

        while (!roomDistanceQueue.isEmpty()) {
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
            for (Map.Entry<String, Room> entry : roomGridMap.entrySet()) {
                if (entry.getValue().equals(bossRoomCandidate)) {
                    bossRoomKey = entry.getKey();
                    break;
                }
            }
            if (bossRoomKey != null) {
                roomGridMap.put(bossRoomKey, actualBossRoom);
            }
            rooms.add(actualBossRoom);
            actualBossRoom.draw(tempTiles, TILE_SIZE, null, null);
            minUsedX = Math.min(minUsedX, actualBossRoom.getGridX());
            maxUsedX = Math.max(maxUsedX, actualBossRoom.getGridX() + actualBossRoom.getWidth());
            minUsedY = Math.min(minUsedY, actualBossRoom.getGridY());
            maxUsedY = Math.max(maxUsedY, actualBossRoom.getGridY() + actualBossRoom.getHeight());
        }

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
                            tempTiles[x][y] = new Tile(Tile.TileType.SHOP_FLOOR, x, y, TILE_SIZE, null);
                        }
                    }
                }
            } else {
                System.out.println("Warning: Shop area does not fit in spawn room.");
            }
        }

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
                generateCorridorBetweenRooms(tempTiles, connectedRoom, roomToAddToMst, null, null, newGateGroup);
                gateCorridorGroups.add(newGateGroup);
                addEdgesToPriorityQueue(roomToAddToMst, rooms, pq, roomGridMap);
            }
        }

        int margin = 5;
        minUsedX = Math.max(0, minUsedX - margin);
        maxUsedX = Math.min(maxGridWidthTiles - 1, maxUsedX + margin);
        minUsedY = Math.max(0, minUsedY - margin);
        maxUsedY = Math.min(maxGridHeightTiles - 1, maxUsedY + margin);
        int finalWidthTiles = maxUsedX - minUsedX + 1;
        int finalHeightTiles = maxUsedY - minUsedY + 1;
        Tile[][] finalTiles = new Tile[finalWidthTiles][finalHeightTiles];
        Map<String, Tile> coordinateToFinalGateTiles = new HashMap<>();

        for (int x = 0; x < finalWidthTiles; x++) {
            for (int y = 0; y < finalHeightTiles; y++) {
                int originalX = minUsedX + x;
                int originalY = minUsedY + y;
                if (isValidGridCoord(originalX, originalY, tempTiles) && tempTiles[originalX][originalY] != null) {
                    Tile originalTile = tempTiles[originalX][originalY];
                    if (originalTile.getType() == Tile.TileType.GATE) {
                        Tile finalGateTile = new Tile(Tile.TileType.GATE, x, y, TILE_SIZE, null, null);
                        finalTiles[x][y] = finalGateTile;
                        String coordKey = originalX + "," + originalY;
                        coordinateToFinalGateTiles.put(coordKey, finalGateTile);
                    } else if (originalTile.getType() == Tile.TileType.BOX || originalTile.getType() == Tile.TileType.DESTRUCTIBLE_WALL) {
                        finalTiles[x][y] = new Tile(originalTile.getType(), x, y, TILE_SIZE, null, originalTile.getHealth(), null);
                    } else if (originalTile.getType() == Tile.TileType.WEAPON_CRATE) {
                        finalTiles[x][y] = new Tile(originalTile.getType(), x, y, TILE_SIZE, null, null, originalTile.getWeaponId());
                    } else {
                        finalTiles[x][y] = new Tile(originalTile.getType(), x, y, TILE_SIZE, null);
                    }
                } else {
                    finalTiles[x][y] = new Tile(Tile.TileType.WALL, x, y, TILE_SIZE, null);
                }
            }
        }

        List<List<Tile>> correctedGateCorridorGroups = new ArrayList<>();
        for (List<Tile> originalGateGroup : gateCorridorGroups) {
            List<Tile> correctedGroup = new ArrayList<>();
            for (Tile originalGateTile : originalGateGroup) {
                String coordKey = originalGateTile.getGridX() + "," + originalGateTile.getGridY();
                Tile correctedTile = coordinateToFinalGateTiles.get(coordKey);
                if (correctedTile != null) {
                    correctedGroup.add(correctedTile);
                }
            }
            if (!correctedGroup.isEmpty()) {
                correctedGateCorridorGroups.add(correctedGroup);
            }
        }
        gateCorridorGroups = correctedGateCorridorGroups;

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

        Dungeon dungeon = new Dungeon(finalTiles, TILE_SIZE, enemies, playerSpawnPixelX, playerSpawnPixelY, gateCorridorGroups,
                correctedBossGridX, correctedBossGridY, bossRoomWidth, bossRoomHeight);

        if (actualBossRoom != null) {
            int bossSpawnX = correctedBossGridX + actualBossRoom.getWidth() / 2;
            int bossSpawnY = correctedBossGridY + actualBossRoom.getHeight() / 2;
            float finalBossSpawnX = (float)bossSpawnX * TILE_SIZE + TILE_SIZE / 2.0f - actualBossRoom.getWidth() / 2.0f;
            float finalBossSpawnY = (float)bossSpawnY * TILE_SIZE + TILE_SIZE / 2.0f - actualBossRoom.getHeight() / 2.0f;
            if (isValidGridCoord(bossSpawnX, bossSpawnY, finalTiles) &&
                    finalTiles[bossSpawnX][bossSpawnY].getType() == Tile.TileType.FLOOR &&
                    !occupiedPositions.contains(new OccupiedTile(bossSpawnX, bossSpawnY))) {
                Boss bossEnemy = new Boss(finalBossSpawnX, finalBossSpawnY, TILE_SIZE * 2, TILE_SIZE * 2, null, 500, null, dungeon, null, null);
                bossEnemy.setMoveSpeed(35.0f);
                bossEnemy.setAttackDamage(40.0f);
                enemies.add(bossEnemy);
                occupiedPositions.add(new OccupiedTile(bossSpawnX, bossSpawnY));
            }
        }

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
                            enemies.add(new Enemy(finalEnemySpawnX, finalEnemySpawnY, enemyWidth, enemyHeight, null, initialHealth, null, dungeon, null, null));
                            occupiedPositions.add(new OccupiedTile(correctedEnemyGridX, correctedEnemyGridY));
                            placed = true;
                        }
                        placementAttempts++;
                    }
                }
            }
        }

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
                            finalTiles[correctedBoxGridX][correctedBoxGridY] = new Tile(Tile.TileType.BOX, correctedBoxGridX, correctedBoxGridY, TILE_SIZE, null, 3.0f, null);
                            occupiedPositions.add(new OccupiedTile(correctedBoxGridX, correctedBoxGridY));
                            placed = true;
                        }
                        placementAttempts++;
                    }
                }
            }
        }

        if (shopRoom != null) {
            int shopCenterX = shopRoom.getGridX() - minUsedX + shopRoom.getWidth() / 2;
            int shopCenterY = shopRoom.getGridY() - minUsedY + shopRoom.getHeight() / 2;
            int crate1X = shopCenterX - 2;
            int crate1Y = shopCenterY;
            int crate2X = shopCenterX + 2;
            int crate2Y = shopCenterY;
            if (isValidGridCoord(crate1X, crate1Y, finalTiles) && (finalTiles[crate1X][crate1Y].getType() == Tile.TileType.FLOOR || finalTiles[crate1X][crate1Y].getType() == Tile.TileType.SHOP_FLOOR)) {
                String randomWeaponId1 = "weapon_" + random.nextInt(100);
                finalTiles[crate1X][crate1Y] = new Tile(Tile.TileType.WEAPON_CRATE, crate1X, crate1Y, TILE_SIZE, null, null, randomWeaponId1);
                occupiedPositions.add(new OccupiedTile(crate1X, crate1Y));
            }
            if (isValidGridCoord(crate2X, crate2Y, finalTiles) && (finalTiles[crate2X][crate2Y].getType() == Tile.TileType.FLOOR || finalTiles[crate2X][crate2Y].getType() == Tile.TileType.SHOP_FLOOR)) {
                String randomWeaponId2 = "weapon_" + random.nextInt(100);
                finalTiles[crate2X][crate2Y] = new Tile(Tile.TileType.WEAPON_CRATE, crate2X, crate2Y, TILE_SIZE, null, null, randomWeaponId2);
                occupiedPositions.add(new OccupiedTile(crate2X, crate2Y));
            }
        }

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

    private static void generateCorridorBetweenRooms(Tile[][] tiles, Room r1, Room r2, Map<Tile.TileType, Object> tileTextures, Map<Integer, Object> gateDestructionTextures, List<Tile> gateGroup) {
        int startX = r1.getCenterX();
        int startY = r1.getCenterY();
        int endX = r2.getCenterX();
        int endY = r2.getCenterY();
        boolean horizontalFirst = Math.abs(startX - endX) > Math.abs(startY - endY);
        int gateX, gateY;

        if (horizontalFirst) {
            drawCorridorSegment(tiles, startX, startY, endX, startY, null, CORRIDOR_WIDTH_TILES);
            drawCorridorSegment(tiles, endX, startY, endX, endY, null, CORRIDOR_WIDTH_TILES);
            int horizontalMidX = startX + (endX - startX) / 2;
            gateX = horizontalMidX;
            gateY = startY;
        } else {
            drawCorridorSegment(tiles, startX, startY, startX, endY, null, CORRIDOR_WIDTH_TILES);
            drawCorridorSegment(tiles, startX, endY, endX, endY, null, CORRIDOR_WIDTH_TILES);
            int verticalMidY = startY + (endY - startY) / 2;
            gateX = startX;
            gateY = verticalMidY;
        }

        drawGateSegment(tiles, gateX, gateY, null, CORRIDOR_WIDTH_TILES, null, gateGroup);
    }

    private static void drawCorridorSegment(Tile[][] tiles, int x1, int y1, int x2, int y2, Map<Tile.TileType, Object> tileTextures, int width) {
        int halfWidth = width / 2;
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                for (int xOffset = -halfWidth; xOffset <= halfWidth; xOffset++) {
                    int currentX = x1 + xOffset;
                    if (isValidGridCoord(currentX, y, tiles)) {
                        if (tiles[currentX][y].getType() == Tile.TileType.GATE) {
                            continue;
                        }
                        if (tiles[currentX][y].getType() == Tile.TileType.WALL) {
                            tiles[currentX][y] = new Tile(Tile.TileType.FLOOR, currentX, y, TILE_SIZE, null);
                        }
                    }
                }
            }
        } else if (y1 == y2) {
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                for (int yOffset = -halfWidth; yOffset <= halfWidth; yOffset++) {
                    int currentY = y1 + yOffset;
                    if (isValidGridCoord(x, currentY, tiles)) {
                        if (tiles[x][currentY].getType() == Tile.TileType.GATE) {
                            continue;
                        }
                        if (tiles[x][currentY].getType() == Tile.TileType.WALL) {
                            tiles[x][currentY] = new Tile(Tile.TileType.FLOOR, x, currentY, TILE_SIZE, null);
                        }
                    }
                }
            }
        }
    }

    private static void drawGateSegment(Tile[][] tiles, int centerX, int centerY, Map<Tile.TileType, Object> tileTextures, int width, Map<Integer, Object> gateDestructionTextures, List<Tile> gateGroup) {
        int totalWidth = width;
        int totalHeight = width;
        int startX = centerX - totalWidth / 2;
        int startY = centerY - totalHeight / 2;

        for (int xOffset = 0; xOffset < totalWidth; xOffset++) {
            for (int yOffset = 0; yOffset < totalHeight; yOffset++) {
                int currentX = startX + xOffset;
                int currentY = startY + yOffset;
                if (isValidGridCoord(currentX, currentY, tiles)) {
                    Tile gateTile = new Tile(Tile.TileType.GATE, currentX, currentY, TILE_SIZE, null, null);
                    gateGroup.add(gateTile);
                    tiles[currentX][currentY] = gateTile;
                }
            }
        }
    }

    private static boolean isValidGridCoord(int x, int y, Tile[][] tiles) {
        return x >= 0 && x < tiles.length && y >= 0 && y < tiles[0].length;
    }

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
}