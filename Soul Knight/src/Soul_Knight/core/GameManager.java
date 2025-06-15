package Soul_Knight.core;

import Soul_Knight.world.Dungeon;
import Soul_Knight.input.InputHandler;
import Soul_Knight.rendering.MapRenderer;
import Soul_Knight.rendering.Texture;
import Soul_Knight.rendering.TextureLoader;
import Soul_Knight.entities.Player;
import Soul_Knight.entities.Enemy;
import Soul_Knight.entities.Projectile;
import Soul_Knight.physics.CollisionManager;
import Soul_Knight.world.Tile;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;
import java.nio.DoubleBuffer; // EZ AZ ÚJ IMPORT!
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

    private long window; // A GLFW ablak handle-je
    private int width = 1280;
    private int height = 720;
    private String title = "Soul Knight LWJGL";

    private Player player;
    private Dungeon currentDungeon;
    private MapRenderer mapRenderer;
    private InputHandler inputHandler;
    private CollisionManager collisionManager; // ÚJ: Ütközés kezelő

    private Texture playerTexture;
    private Map<Tile.TileType, Texture> tileTextures;
    private Texture enemyTexture; // ÚJ: Ellenség textúra

    private List<Projectile> projectiles; // ÚJ: Aktív lövedékek listája

    public void run() {
        init(); // Inicializálás
        loop(); // Játék loop
        cleanup(); // Tisztítás
    }

    private void init() {
        // Inicializálja a GLFW-t
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Ablak konfiguráció
        glfwDefaultWindowHints(); // Alapértelmezett hint-ek
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Az ablak kezdetben rejtett
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Az ablak átméretezhető

        // Ablak létrehozása
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Ablak méretének lekérdezése (ha a hint-ekkel nem fix méretet állítunk be)
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            width = pWidth.get(0);
            height = pHeight.get(0);
        }

        // Input kezelő beállítása
        inputHandler = new InputHandler(window);

        // Középre igazítás
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // OpenGL kontextus létrehozása
        glfwMakeContextCurrent(window);
        GL.createCapabilities(); // Inicializálja az OpenGL képességeket

        // V-Sync bekapcsolása (képkockaszám szinkronizálása a monitor frissítési gyakoriságával)
        glfwSwapInterval(1);

        // Az ablak láthatóvá tétele
        glfwShowWindow(window);

        // OpenGL alap beállítások 2D-hez (projektor mátrix)
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0); // 2D ortogonális projekció (felső-bal sarok 0,0)
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_BLEND); // Áttetszőség engedélyezése
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D); // Textúra engedélyezése OpenGL-ben

        // --- Textúrák betöltése ---
        playerTexture = TextureLoader.loadTexture("character1.png");
        if (playerTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a játékos textúrát.");
        }

        tileTextures = new HashMap<>();
        tileTextures.put(Tile.TileType.FLOOR, TextureLoader.loadTexture("grass.png"));
        tileTextures.put(Tile.TileType.WALL, TextureLoader.loadTexture("wall_tile.png"));

        enemyTexture = TextureLoader.loadTexture("enemy.png"); // ÚJ: Ellenség textúra betöltése
        if (enemyTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni az ellenség textúrát.");
        }

        // Ellenőrizzük, hogy minden textúra betöltődött-e
        if (tileTextures.get(Tile.TileType.FLOOR) == null || tileTextures.get(Tile.TileType.WALL) == null) {
            System.err.println("HIBA: Nem sikerült betölteni az egyik csempe textúrát.");
        }
        // --- Textúrák betöltése vége ---

        // Játék entitások inicializálása
        player = new Player(width / 2, height / 2, 50, 50, playerTexture, window); // Ablak handle átadása a Player-nek

        // Pálya inicializálása
        currentDungeon = new Dungeon(20, 20, 32, tileTextures, enemyTexture); // Ellenség textúra átadása
        mapRenderer = new MapRenderer();
        collisionManager = new CollisionManager(currentDungeon); // Ütközés kezelő inicializálása

        projectiles = new ArrayList<>(); // Lövedékek listájának inicializálása
    }

    private void loop() {
        double lastTime = glfwGetTime();
        double accumulator = 0.0;
        final double frameTime = 1.0 / 60.0; // Frissítés 60 FPS-en

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            accumulator += deltaTime;

            // Frissítés fix lépésekben
            while (accumulator >= frameTime) {
                update((float) frameTime, currentTime); // Átadjuk a currentTime-et
                accumulator -= frameTime;
            }

            render(); // Rajzolás
            glfwSwapBuffers(window); // Bufferek cseréje (az elkészült kép megjelenítése)
            glfwPollEvents(); // Események feldolgozása (input, ablak események)
        }
    }

    private void update(float deltaTime, double currentTime) {
        // Játékos mozgás frissítése input és ütközés alapján
        // A Player.update() visszatérhet az új lövedékekkel
        player.update(deltaTime, inputHandler, collisionManager, currentTime);

        // Kezeljük a játékos által kilőtt lövedékeket
        // Ha a Player.update() egy Projectile-lal térne vissza:
        Projectile newProjectile = player.getCurrentWeapon().shoot(player, player.getX(), player.getY(), (float)getCursorX(), (float)getCursorY(), (float)currentTime);
        if (newProjectile != null) {
            projectiles.add(newProjectile);
        }

        // Ellenségek frissítése (egyelőre nem mozognak)
        for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
            enemy.update(deltaTime);
        }

        // Lövedékek frissítése és ütközések kezelése
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update(deltaTime);

            // Lövedék ütközés falakkal
            int projGridX = (int) (projectile.getX() / currentDungeon.getMainRoom().getTileSize());
            int projGridY = (int) (projectile.getY() / currentDungeon.getMainRoom().getTileSize());

            // Ellenőrizzük a lövedék körüli csempéket
            boolean hitWall = false;
            for (int x = projGridX - 1; x <= projGridX + 1; x++) {
                for (int y = projGridY - 1; y <= projGridY + 1; y++) {
                    Tile tile = currentDungeon.getMainRoom().getTile(x, y);
                    if (tile != null && tile.getType() == Tile.TileType.WALL) {
                        // Kisebb optimalizálás: nem kell egy új entitást létrehozni minden egyes ellenőrzéshez
                        // Ehelyett használjuk a Tile bounds-át (ha hozzáadjuk) vagy a tile méretét és pozícióját közvetlenül
                        // Jelenlegi megoldás: létrehoz egy ideiglenes Entitást a fal reprezentálására
                        if (collisionManager.checkCollision(projectile, new Soul_Knight.entities.Entity(x * tile.getSize(), y * tile.getSize(), tile.getSize(), tile.getSize()) {
                            @Override public void update(float dt, Object... args) {} @Override public void render() {}
                        })) {
                            hitWall = true;
                            break;
                        }
                    }
                }
                if (hitWall) break;
            }
            if (hitWall) {
                projectile.setAlive(false); // A lövedék eltűnik falba ütközéskor
            }

            // Lövedék ütközés ellenségekkel
            for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
                if (enemy.isAlive() && collisionManager.checkCollision(projectile, enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    projectile.setAlive(false); // A lövedék eltűnik, ha eltalál egy ellenséget
                    break; // Egy lövedék csak egy ellenséget sebezzen
                }
            }

            // Ha a lövedék nem él, távolítsuk el
            if (!projectile.isAlive()) {
                projectileIterator.remove();
            }
        }

        // Halott ellenségek eltávolítása
        currentDungeon.getMainRoom().getEnemies().removeIf(enemy -> !enemy.isAlive());
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Tisztítja a képernyőt (fekete háttér)

        // Pálya rajzolása
        mapRenderer.render(currentDungeon);

        // Ellenségek rajzolása
        for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
            enemy.render();
        }

        // Játékos rajzolása
        player.render();

        // Lövedékek rajzolása
        for (Projectile projectile : projectiles) {
            projectile.render();
        }
    }

    private void cleanup() {
        // Callback-ek felszabadítása
        glfwFreeCallbacks(window);
        // Ablak felszabadítása
        glfwDestroyWindow(window);
        // GLFW felszabadítása és a hibakezelő leállítása
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        // --- Textúrák felszabadítása ---
        if (playerTexture != null) playerTexture.delete();
        if (tileTextures != null) {
            for (Texture texture : tileTextures.values()) {
                if (texture != null) texture.delete();
            }
        }
        if (enemyTexture != null) enemyTexture.delete(); // ÚJ: Ellenség textúra felszabadítása
        // --- Textúrák felszabadítása vége ---
    }

    /**
     * Segédmetódus az egér X koordinátájának lekéréséhez.
     */
    private double getCursorX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xPos = stack.mallocDouble(1);
            DoubleBuffer yPos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xPos, yPos);
            return xPos.get(0);
        }
    }

    /**
     * Segédmetódus az egér Y koordinátájának lekéréséhez.
     */
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
