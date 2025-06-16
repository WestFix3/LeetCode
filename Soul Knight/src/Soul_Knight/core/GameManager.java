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

    private long window; // A GLFW ablak handle-je
    private int width = 1280;
    private int height = 720;
    private String title = "Soul Knight LWJGL";

    private Player player;
    private Dungeon currentDungeon;
    private MapRenderer mapRenderer;
    private InputHandler inputHandler;
    private CollisionManager collisionManager;

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

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

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

        player = new Player(width / 2, height / 2, 50, 50, playerTexture, window);

        currentDungeon = new Dungeon(20, 20, 32, tileTextures, enemyTexture);
        mapRenderer = new MapRenderer();
        collisionManager = new CollisionManager(currentDungeon);

        projectiles = new ArrayList<>();
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

            // VÁLTOZTATÁS: Lövés logika KIVÉVE a fix időlépéses ciklusból
            Projectile newProjectile = null;
            if (inputHandler.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                System.out.println("Lövés kérés érzékelve! (GameManager)"); // Debug üzenet
                newProjectile = player.getCurrentWeapon().shoot(player, player.getX(), player.getY(), (float)getCursorX(), (float)getCursorY(), (float)currentTime);
                if (newProjectile != null) {
                    System.out.println("Lövedék kilőve! (GameManager)"); // Debug üzenet
                } else {
                    System.out.println("Lövés kérés, de a fegyver cooldownon van vagy más okból nem lőhetett. (GameManager)"); // Debug üzenet
                }
            }

            // Ha a lövés sikeres volt, add hozzá a lövedéket (ez is a fix update ciklus kívül van most)
            if (newProjectile != null) {
                projectiles.add(newProjectile);
            }


            while (accumulator >= frameTime) {
                update((float) frameTime, currentTime); // Az update metódus már NEM kezeli a lövést
                accumulator -= frameTime;
            }

            render();
            glfwSwapBuffers(window);
        }
    }

    private void update(float deltaTime, double currentTime) {
        // VÁLTOZTATÁS: A lövés logikát innen áthelyeztük a loop() metódusba.
        // A Player.update() továbbra is kapja a currentTime-et, bár a Player nem közvetlenül használja lövésre.
        player.update(deltaTime, inputHandler, collisionManager, currentTime);

        for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
            enemy.update(deltaTime);
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update(deltaTime);

            boolean hitWall = false;
            int tileSize = currentDungeon.getMainRoom().getTileSize();

            int projGridX = (int) (projectile.getX() / tileSize);
            int projGridY = (int) (projectile.getY() / tileSize);

            for (int x = projGridX - 1; x <= projGridX + 1; x++) {
                for (int y = projGridY - 1; y <= projGridY + 1; y++) {
                    Tile tile = currentDungeon.getMainRoom().getTile(x, y);
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

            for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
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

        int initialEnemyCount = currentDungeon.getMainRoom().getEnemies().size();
        currentDungeon.getMainRoom().getEnemies().removeIf(enemy -> !enemy.isAlive());
        if (currentDungeon.getMainRoom().getEnemies().size() < initialEnemyCount) {
            System.out.println("DEBUG: Ellenség(ek) eltávolítva. Jelenlegi szám: " + currentDungeon.getMainRoom().getEnemies().size());
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mapRenderer.render(currentDungeon);

        for (Enemy enemy : currentDungeon.getMainRoom().getEnemies()) {
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
