package core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiplayerGameServer {
    private static final int TCP_PORT = 5555;
    private static final int UDP_PORT = 5556;
    private static final int MAX_PLAYERS = 4;

    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;
    private ExecutorService threadPool;

    private Map<Integer, PlayerSession> connectedPlayers;
    private AtomicInteger playerIdCounter;

    // Játék állapot
    private GameState gameState;
    private boolean gameRunning = false;

    public MultiplayerGameServer() {
        this.connectedPlayers = new ConcurrentHashMap<>();
        this.playerIdCounter = new AtomicInteger(1);
        this.threadPool = Executors.newCachedThreadPool();
        this.gameState = new GameState();
    }

    public void startServer() {
        try {
            // TCP szerver a kapcsolatok kezelésére
            tcpServerSocket = new ServerSocket(TCP_PORT);
            System.out.println("🎮 TCP Server started on port " + TCP_PORT);

            // UDP socket a gyors játékadatokra
            udpSocket = new DatagramSocket(UDP_PORT);
            System.out.println("🎮 UDP Server started on port " + UDP_PORT);

            // TCP kapcsolatokat fogadó szál
            threadPool.execute(this::acceptTCPConnections);

            // UDP csomagokat fogadó szál
            threadPool.execute(this::handleUDPPackets);

            // Játék loop
            threadPool.execute(this::gameLoop);

            System.out.println("✅ Multiplayer Game Server is running!");

        } catch (IOException e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
        }
    }

    private void acceptTCPConnections() {
        while (!tcpServerSocket.isClosed()) {
            try {
                Socket clientSocket = tcpServerSocket.accept();

                if (connectedPlayers.size() >= MAX_PLAYERS) {
                    sendTCPResponse(clientSocket, "SERVER_FULL");
                    clientSocket.close();
                    continue;
                }

                int playerId = playerIdCounter.getAndIncrement();
                PlayerSession session = new PlayerSession(playerId, clientSocket);
                connectedPlayers.put(playerId, session);

                System.out.println("🔗 Player " + playerId + " connected from " +
                        clientSocket.getInetAddress().getHostAddress());

                // Kezeld a játékos sessiont külön szálon
                threadPool.execute(() -> handlePlayerSession(session));

            } catch (IOException e) {
                if (!tcpServerSocket.isClosed()) {
                    System.err.println("❌ Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleUDPPackets() {
        byte[] buffer = new byte[1024];

        while (!udpSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                // Feldolgozzuk az UDP csomagot külön szálon
                threadPool.execute(() -> processUDPPacket(packet));

            } catch (IOException e) {
                if (!udpSocket.isClosed()) {
                    System.err.println("❌ UDP receive error: " + e.getMessage());
                }
            }
        }
    }

    private void handlePlayerSession(PlayerSession session) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(session.getClientSocket().getInputStream()));
            PrintWriter out = new PrintWriter(session.getClientSocket().getOutputStream(), true);

            // Küldjük a játékosnak az ID-ját
            sendTCPResponse(session.getClientSocket(), "PLAYER_ID:" + session.getPlayerId());

            String message;
            while ((message = in.readLine()) != null && !session.getClientSocket().isClosed()) {
                processTCPMessage(session, message);
            }

        } catch (IOException e) {
            System.err.println("❌ Player session error: " + e.getMessage());
        } finally {
            disconnectPlayer(session.getPlayerId());
        }
    }

    private void processTCPMessage(PlayerSession session, String message) {
        System.out.println("📨 TCP from Player " + session.getPlayerId() + ": " + message);

        String[] parts = message.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "JOIN_GAME":
                handlePlayerJoin(session, data);
                break;
            case "PLAYER_READY":
                handlePlayerReady(session);
                break;
            case "CHAT_MESSAGE":
                broadcastTCPMessage("CHAT:" + session.getPlayerId() + ":" + data);
                break;
            case "PLAYER_INPUT":
                // Inputot UDP-n keresztül kezeljük, de itt is lehet
                break;
            case "DISCONNECT":
                disconnectPlayer(session.getPlayerId());
                break;
        }
    }

    private void processUDPPacket(DatagramPacket packet) {
        try {
            String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            String[] parts = message.split(":", 3);

            if (parts.length < 3) return;

            int playerId = Integer.parseInt(parts[0]);
            String command = parts[1];
            String data = parts[2];

            PlayerSession session = connectedPlayers.get(playerId);
            if (session != null) {
                // Frissítsük a játékos UDP címét
                session.setUdpAddress(packet.getAddress());
                session.setUdpPort(packet.getPort());

                processPlayerInput(playerId, command, data);
            }

        } catch (Exception e) {
            System.err.println("❌ UDP packet processing error: " + e.getMessage());
        }
    }

    private void handlePlayerJoin(PlayerSession session, String playerData) {
        // playerData formátum: "name:ability:showPathDebug"
        String[] playerInfo = playerData.split(":");
        if (playerInfo.length >= 3) {
            session.setPlayerName(playerInfo[0]);
            session.setPlayerAbility(playerInfo[1]);
            session.setShowPathDebug(Boolean.parseBoolean(playerInfo[2]));

            System.out.println("🎯 Player " + session.getPlayerId() + " joined: " +
                    session.getPlayerName() + " (" + session.getPlayerAbility() + ")");

            // Küldjük a játékosnak a jelenlegi játékállapotot
            sendGameStateToPlayer(session);

            // Értesítsük a többi játékost
            broadcastTCPMessage("PLAYER_JOINED:" + session.getPlayerId() + ":" +
                    session.getPlayerName() + ":" + session.getPlayerAbility());
        }
    }

    private void handlePlayerReady(PlayerSession session) {
        session.setReady(true);
        broadcastTCPMessage("PLAYER_READY:" + session.getPlayerId());

        // Ellenőrizzük, hogy mindenki kész van-e
        checkAllPlayersReady();
    }

    private void checkAllPlayersReady() {
        boolean allReady = connectedPlayers.values().stream()
                .allMatch(PlayerSession::isReady);

        if (allReady && connectedPlayers.size() >= 1) {
            startGame();
        }
    }

    private void startGame() {
        System.out.println("🚀 Starting multiplayer game with " + connectedPlayers.size() + " players!");

        // Generáljuk a dungeon-t
        generateSharedDungeon();

        gameRunning = true;

        // Értesítsük minden játékost
        broadcastTCPMessage("GAME_STARTING");

        // Küldjük a dungeon adatait mindenkinek
        broadcastDungeonData();
    }

    private void generateSharedDungeon() {
        // Ugyanazt a dungeon-t generáljuk minden játékosnak
        System.out.println("🏰 Generating shared dungeon...");

        // TODO: Implementáld a dungeon generálást a GameManager logikájával
        // currentDungeon = DungeonGenerator.generateRandomDungeon(...);

        // Egyelőre mock adatok
        gameState.setDungeonGenerated(true);
        gameState.setBossDefeated(false);

        // Inicializáljuk a játékos állapotokat
        for (PlayerSession session : connectedPlayers.values()) {
            PlayerState playerState = new PlayerState(
                    session.getPlayerId(),
                    session.getPlayerName(),
                    100.0f, 100.0f, // x, y pozíció
                    100.0f, 100.0f  // health, maxHealth
            );

            playerState.setAbility(session.getPlayerAbility());

            gameState.addPlayerState(session.getPlayerId(), playerState);

            System.out.println("🎯 Player state created: " + playerState);
        }

        // Példa ellenségek hozzáadása
        addSampleEnemies();
        System.out.println("✅ Shared dungeon generated with " + connectedPlayers.size() + " players");
    }

    private void addSampleEnemies() {
        // Példa ellenségek a teszteléshez
        EnemyState enemy1 = new EnemyState(1, "normal", 300.0f, 300.0f, 50.0f, 50.0f);
        EnemyState enemy2 = new EnemyState(2, "normal", 400.0f, 400.0f, 50.0f, 50.0f);
        EnemyState boss = new EnemyState(3, "boss", 500.0f, 500.0f, 200.0f, 200.0f);

        gameState.getEnemyStates().add(enemy1);
        gameState.getEnemyStates().add(enemy2);
        gameState.getEnemyStates().add(boss);

        System.out.println("👹 Added " + gameState.getEnemyStates().size() + " sample enemies");
    }

    private void gameLoop() {
        final long TICK_RATE = 60; // 60 FPS
        final long TICK_TIME_NS = 1000000000 / TICK_RATE;

        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int ticks = 0;

        while (true) {
            long now = System.nanoTime();
            long deltaTime = now - lastTime;

            if (deltaTime >= TICK_TIME_NS) {
                lastTime = now;

                if (gameRunning) {
                    updateGameState(deltaTime / 1000000000.0f);
                    ticks++;
                }

                // Másodpercenkénti statisztika
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                    System.out.println("🎮 Game TPS: " + ticks + ", Players: " + connectedPlayers.size());
                    ticks = 0;

                    // Szinkron állapot küldése
                    if (gameRunning) {
                        broadcastGameState();
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateGameState(float deltaTime) {
        if (!gameRunning) return;

        // Frissítsd a játék állapotát
        updatePlayerPositions(deltaTime);
        updateProjectiles(deltaTime);
        updateEnemies(deltaTime);
        checkCollisions();
    }

    private void updatePlayerPositions(float deltaTime) {
        for (PlayerSession session : connectedPlayers.values()) {
            if (session.hasPendingInput()) {
                String input = session.getNextInput();
                if (input != null) {
                    // Feldolgozzuk a játékos inputját
                    processPlayerMovement(session.getPlayerId(), input);
                }
            }
        }
    }

    private void processPlayerMovement(int playerId, String input) {
        // input formátum: "MOVEMENT:x,y" vagy "POSITION:x,y"
        String[] parts = input.split(":");
        if (parts.length >= 3) {
            try {
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                gameState.updatePlayerPosition(playerId, x, y);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid player movement data: " + input);
            }
        }
    }

    private void updateProjectiles(float deltaTime) {
        // TODO: Lövedékek frissítése
        // Egyszerű példa: minden lövedék mozog a sebességével
        for (ProjectileState projectile : gameState.getProjectileStates()) {
            if (projectile.isActive()) {
                projectile.setX(projectile.getX() + projectile.getVelocityX() * deltaTime);
                projectile.setY(projectile.getY() + projectile.getVelocityY() * deltaTime);

                // Inaktívvá tesszük, ha túl messzire ment
                if (Math.abs(projectile.getX()) > 1000 || Math.abs(projectile.getY()) > 1000) {
                    projectile.setActive(false);
                }
            }
        }

        // Eltávolítjuk az inaktív lövedékeket
        gameState.getProjectileStates().removeIf(proj -> !proj.isActive());
    }

    private void updateEnemies(float deltaTime) {
        // TODO: Ellenségek AI frissítése
        // Egyszerű példa: ellenségek követik a legközelebbi játékost
        for (EnemyState enemy : gameState.getEnemyStates()) {
            if (enemy.isAlive()) {
                // Keressük a legközelebbi játékost
                PlayerState closestPlayer = findClosestPlayer(enemy.getX(), enemy.getY());
                if (closestPlayer != null) {
                    // Mozgás a játékos felé
                    float dx = closestPlayer.getX() - enemy.getX();
                    float dy = closestPlayer.getY() - enemy.getY();
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance > 50) { // Ne legyen túl közel
                        float speed = 50.0f * deltaTime; // 50 pixel/másodperc
                        enemy.setX(enemy.getX() + (dx / distance) * speed);
                        enemy.setY(enemy.getY() + (dy / distance) * speed);
                    }
                }
            }
        }
    }

    private PlayerState findClosestPlayer(float enemyX, float enemyY) {
        PlayerState closest = null;
        float minDistance = Float.MAX_VALUE;

        for (PlayerState player : gameState.getPlayerStates().values()) {
            if (player.isAlive()) {
                float dx = player.getX() - enemyX;
                float dy = player.getY() - enemyY;
                float distance = dx * dx + dy * dy; // Négyzetes távolság (gyökvonás nélkül)

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = player;
                }
            }
        }

        return closest;
    }

    private void checkCollisions() {
        // TODO: Ütközések ellenőrzése
        // Egyszerű példa: lövedék-ellenség ütközések
        for (ProjectileState projectile : gameState.getProjectileStates()) {
            if (!projectile.isActive()) continue;

            for (EnemyState enemy : gameState.getEnemyStates()) {
                if (!enemy.isAlive()) continue;

                // Egyszerű kör ütközésvizsgálat
                float dx = projectile.getX() - enemy.getX();
                float dy = projectile.getY() - enemy.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < 32) { // Ütközési távolság
                    // Sebzés
                    enemy.setHealth(enemy.getHealth() - projectile.getDamage());
                    projectile.setActive(false);

                    if (enemy.getHealth() <= 0) {
                        enemy.setAlive(false);
                    }

                    System.out.println("💥 Projectile " + projectile.getProjectileId() +
                            " hit enemy " + enemy.getEnemyId() +
                            " for " + projectile.getDamage() + " damage");
                    break;
                }
            }
        }
    }

    private void processPlayerInput(int playerId, String command, String data) {
        if (!gameRunning) return;

        PlayerSession session = connectedPlayers.get(playerId);
        if (session != null) {
            session.setLastInput(command + ":" + data);
            session.addInput(command + ":" + data);

            // Azonnal továbbítsuk a többi játékosnak (kivéve a küldőt)
            broadcastUDPToOthers(playerId, playerId + ":" + command + ":" + data);
        }
    }

    private void broadcastGameState() {
        if (!gameRunning) return;

        String gameStateData = "GAME_STATE:" + gameState.serializeGameState();
        broadcastUDPToAll(gameStateData);
    }

    private void sendGameStateToPlayer(PlayerSession session) {
        // Küldjük a játékosnak a teljes játékállapotot
        String gameStateMsg = "FULL_GAME_STATE:" + gameState.serializeGameState();
        sendTCPResponse(session.getClientSocket(), gameStateMsg);
    }

    private void broadcastDungeonData() {
        // Küldjük a dungeon adatait minden játékosnak
        // Egyszerű mock adatok - a valós implementációban itt a tényleges dungeon adatokat kell küldeni
        String dungeonData = "DUNGEON_DATA:level_1|32|boss_room:10,10|enemy_spawns:5,5;15,15;20,20";
        broadcastTCPMessage(dungeonData);
    }

    // Hálózati segédmetódusok
    private void sendTCPResponse(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("❌ TCP send error: " + e.getMessage());
        }
    }

    private void broadcastTCPMessage(String message) {
        for (PlayerSession session : connectedPlayers.values()) {
            sendTCPResponse(session.getClientSocket(), message);
        }
    }

    private void broadcastUDPToAll(String message) {
        for (PlayerSession session : connectedPlayers.values()) {
            sendUDPMessage(session, message);
        }
    }

    private void broadcastUDPToOthers(int excludePlayerId, String message) {
        for (PlayerSession session : connectedPlayers.values()) {
            if (session.getPlayerId() != excludePlayerId) {
                sendUDPMessage(session, message);
            }
        }
    }

    private void sendUDPMessage(PlayerSession session, String message) {
        if (session.getUdpAddress() == null) return;

        try {
            byte[] data = message.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    session.getUdpAddress(),
                    session.getUdpPort()
            );
            udpSocket.send(packet);
        } catch (IOException e) {
            System.err.println("❌ UDP send error: " + e.getMessage());
        }
    }

    private void disconnectPlayer(int playerId) {
        PlayerSession session = connectedPlayers.remove(playerId);
        if (session != null) {
            try {
                session.getClientSocket().close();
            } catch (IOException e) {
                System.err.println("❌ Error closing player socket: " + e.getMessage());
            }

            // Eltávolítjuk a játékos állapotát is
            gameState.removePlayerState(playerId);

            System.out.println("🔌 Player " + playerId + " disconnected");
            broadcastTCPMessage("PLAYER_DISCONNECTED:" + playerId);
        }
    }

    public void stopServer() {
        try {
            gameRunning = false;

            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }

            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }

            threadPool.shutdown();

            // Zárjuk le az összes játékos kapcsolatát
            for (PlayerSession session : connectedPlayers.values()) {
                try {
                    session.getClientSocket().close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            connectedPlayers.clear();

            System.out.println("🛑 Multiplayer Game Server stopped");

        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MultiplayerGameServer server = new MultiplayerGameServer();
        server.startServer();

        // Leállítás hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));
    }
}