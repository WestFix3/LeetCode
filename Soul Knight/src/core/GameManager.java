package core;

import world.Dungeon;
import world.DungeonGenerator;
import input.InputHandler;
import rendering.MapRenderer;
import rendering.Texture;
import rendering.TextureLoader;
import entities.Player;
import entities.Enemy;
import entities.Projectile;
import physics.CollisionManager;
import world.Tile;
import rendering.Camera; // <-- ÚJ IMPORT: Itt importáljuk a Camera osztályt!

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameManager {

    private long window;
    private int width = 1280;
    private int height = 720;
    private String title = "Soul Knight LWJGL";

    private Player player;
    private Dungeon currentDungeon;
    private MapRenderer mapRenderer;
    private InputHandler inputHandler;
    private CollisionManager collisionManager;
    private Camera camera; // <-- KAMERA DEKLARÁCIÓ: Ide add hozzá ezt a sort!

    private Texture playerTexture;
    private Map<Tile.TileType, Texture> tileTextures;
    private Texture enemyTexture;

    private List<Projectile> projectiles;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            width = pWidth.get(0);
            height = pHeight.get(0);
        }

        inputHandler = new InputHandler(window);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);

        glfwShowWindow(window);

        // --- GL Beállítások ---
        // A korábbi glMatrixMode(GL_PROJECTION); és glOrtho(...) sorokat TÖRÖLD vagy VÉLEMÉNYEZD KI innen,
        // mert a kamera fogja ezeket beállítani!
        // glMatrixMode(GL_PROJECTION);
        // glLoadIdentity();
        // glOrtho(0.0, width, height, 0.0, -1.0, 1.0); // <-- TÖRÖLD VAGY VÉLEMÉNYEZD KI EZT A SORT!
        // glMatrixMode(GL_MODELVIEW);
        // glLoadIdentity(); // <-- TÖRÖLD VAGY VÉLEMÉNYEZD KI EZT A SORT IS!

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

        // --- Kamera inicializálása (itt!) ---
        camera = new Camera(width, height); // <-- KAMERA INICIALIZÁLÁS: Hozd létre a kamerát a képernyő méretével

        // --- Textúrák betöltése ---
        playerTexture = TextureLoader.loadTexture("character1.png");
        if (playerTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a játékos textúrát.");
        }

        tileTextures = new HashMap<>();
        tileTextures.put(Tile.TileType.FLOOR, TextureLoader.loadTexture("grass.png"));
        tileTextures.put(Tile.TileType.WALL, TextureLoader.loadTexture("wall_tile.png"));

        enemyTexture = TextureLoader.loadTexture("enemy1.png");
        if (enemyTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni az ellenség textúrát.");
        } else {
            System.out.println("DEBUG: Ellenség textúra sikeresen betöltve.");
        }

        if (tileTextures.get(Tile.TileType.FLOOR) == null || tileTextures.get(Tile.TileType.WALL) == null) {
            System.err.println("HIBA: Nem sikerült betölteni az egyik csempe textúrát.");
        }
        // --- Textúrák betöltése VÉGE ---

        // --- Dungeon generálása és inicializálása ---
        currentDungeon = DungeonGenerator.generateRandomDungeon(32, tileTextures, enemyTexture);

        // Játékos inicializálása a generált spawn pozícióval
        player = new Player(
                currentDungeon.getPlayerSpawnX(), // <-- A Dungeon-től kapott X
                currentDungeon.getPlayerSpawnY(), // <-- A Dungeon-től kapott Y
                50, 50, playerTexture, window);

        // Kezdeti kamera pozíció beállítása, hogy a játékos spawn pontjára mutasson
        // Ezt a follow metódus is megteszi majd az update-ben, de jó kezdetnek
        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels); // <-- KAMERA KEZDETI POZÍCIÓ BEÁLLÍTÁSA

        mapRenderer = new MapRenderer();
        collisionManager = new CollisionManager(currentDungeon);

        projectiles = new ArrayList<>();
    }

    private void update(float deltaTime, double currentTime) {
        player.update(deltaTime, inputHandler, collisionManager, currentTime);

        // A kamera frissítése, hogy kövesse a játékost
        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels); // <-- KAMERA FRISSÍTÉSE: Minden update ciklusban kövesse a játékost

        // Az ellenségeket most a currentDungeon-től kérjük le
        for (Enemy enemy : currentDungeon.getEnemies()) {
            enemy.update(deltaTime);
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update(deltaTime);

            boolean hitWall = false;
            int tileSize = currentDungeon.getTileSize();

            int projGridX = (int) (projectile.getX() / tileSize);
            int projGridY = (int) (projectile.getY() / tileSize);

            // Ellenőrizzük a körülötte lévő csempéket
            int minCheckX = Math.max(0, projGridX - 1);
            int maxCheckX = Math.min(currentDungeon.getWidthTiles() - 1, projGridX + 1);
            int minCheckY = Math.max(0, projGridY - 1);
            int maxCheckY = Math.min(currentDungeon.getHeightTiles() - 1, projGridY + 1);


            for (int x = minCheckX; x <= maxCheckX; x++) {
                for (int y = minCheckY; y <= maxCheckY; y++) {
                    Tile tile = currentDungeon.getTile(x, y);
                    if (tile != null && tile.getType() == Tile.TileType.WALL) {
                        if (collisionManager.checkTileCollision(projectile, tile)) {
                            hitWall = true;
                            break;
                        }
                    }
                }
                if (hitWall) break;
            }
            if (hitWall) {
                projectile.setAlive(false);
            }

            // Ellenség ütközés
            for (Enemy enemy : currentDungeon.getEnemies()) {
                if (enemy.isAlive() && collisionManager.checkCollision(projectile, enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    projectile.setAlive(false);
                    break;
                }
            }

            if (!projectile.isAlive()) {
                projectileIterator.remove();
            }
        }

        int initialEnemyCount = currentDungeon.getEnemies().size();
        currentDungeon.getEnemies().removeIf(enemy -> !enemy.isAlive());
        if (currentDungeon.getEnemies().size() < initialEnemyCount) {
            System.out.println("DEBUG: Ellenség(ek) eltávolítva. Jelenlegi szám: " + currentDungeon.getEnemies().size());
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camera.applyTransform(); // <-- KAMERA TRANSZFORMÁCIÓ ALKALMAZÁSA: Ezt itt kell meghívni, Mielőtt bármit rajzolsz!

        // Ezek az elemek a világkoordinátáikon rajzolódnak, de a kamera eltolja a nézetet.
        mapRenderer.render(currentDungeon);

        for (Enemy enemy : currentDungeon.getEnemies()) {
            enemy.render();
        }

        player.render();

        for (Projectile projectile : projectiles) {
            projectile.render();
        }
    }

    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        if (playerTexture != null) playerTexture.delete();
        if (tileTextures != null) {
            for (Texture texture : tileTextures.values()) {
                if (texture != null) texture.delete();
            }
        }
        if (enemyTexture != null) enemyTexture.delete();
    }

    private void loop() {
        double lastTime = glfwGetTime();
        double accumulator = 0.0;
        final double frameTime = 1.0 / 60.0;

        while (!glfwWindowShouldClose(window)) {
            inputHandler.update();
            glfwPollEvents();

            double currentTime = glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            accumulator += deltaTime;

            Projectile newProjectile = null;
            // Csak akkor lőjön, ha az egérgomb lenyomása új esemény
            if (inputHandler.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                // --- KORRIGÁLT KÓD: KURZOR POZÍCIÓ ÁTALAKÍTÁSA VILÁG KOORDINÁTÁVÁ ---
                // A kurzor pozíciója a képernyőn (pixelben)
                float cursorScreenX = (float) getCursorX();
                float cursorScreenY = (float) getCursorY();

                // A kamera aktuális X és Y pozíciója (világkoordinátában, a kamera bal felső sarka)
                float cameraWorldX = camera.getX();
                float cameraWorldY = camera.getY();

                // A kurzor pozíciójának átalakítása világkoordinátává
                // Egyszerűen hozzáadjuk a kamera pozícióját a képernyő koordinátához
                float targetWorldX = cursorScreenX + cameraWorldX;
                float targetWorldY = cursorScreenY + cameraWorldY;

                newProjectile = player.getCurrentWeapon().shoot(
                        player,
                        player.getX(), // Lövedék kezdeti X pozíciója (játékos)
                        player.getY(), // Lövedék kezdeti Y pozíciója (játékos)
                        targetWorldX,  // Célpont X pozíciója (világkoordinátában)
                        targetWorldY,  // Célpont Y pozíciója (világkoordinátában)
                        (float) currentTime
                );
                // --- VÉGE KORRIGÁLT KÓDNAK ---
            }

            if (newProjectile != null) {
                projectiles.add(newProjectile);
            }

            while (accumulator >= frameTime) {
                update((float) frameTime, currentTime);
                accumulator -= frameTime;
            }

            render();
            glfwSwapBuffers(window);
        }
    }

    // A getCursorX és getCursorY metódusokat nem kell változtatni,
    // mert ők az abszolút képernyő koordinátákat adják vissza.
    private double getCursorX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xPos = stack.mallocDouble(1);
            DoubleBuffer yPos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xPos, yPos);
            return xPos.get(0);
        }
    }

    private double getCursorY() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xPos = stack.mallocDouble(1);
            DoubleBuffer yPos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xPos, yPos);
            return yPos.get(0);
        }
    }

    public static void main(String[] args) {
        new GameManager().run();
    }
}