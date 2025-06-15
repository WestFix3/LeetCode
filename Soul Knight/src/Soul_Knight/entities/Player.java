package Soul_Knight.entities;

import Soul_Knight.input.InputHandler;
import Soul_Knight.rendering.Texture;
import Soul_Knight.physics.CollisionManager; // ÚJ IMPORT!
import Soul_Knight.entities.weapons.Weapon; // ÚJ IMPORT!

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos; // ÚJ IMPORT az egér pozícióhoz
import java.nio.DoubleBuffer;
import org.lwjgl.system.MemoryStack; // ÚJ IMPORT a MemoryStack-hez

public class Player extends Entity {

    private float moveSpeed = 200.0f; // Mozgási sebesség pixel/másodpercben
    private Texture texture; // A játékos textúrája
    private Weapon currentWeapon; // A játékos aktuális fegyvere
    private long windowHandle; // Szükséges az egér pozíciójának lekérdezéséhez

    public Player(float x, float y, float width, float height, Texture texture, long windowHandle) {
        super(x, y, width, height);
        this.texture = texture;
        this.windowHandle = windowHandle; // Ablak handle átadása
        // Kezdő fegyver beállítása (Példa: 10 sebzés, 2 lövés/mp, 400 sebesség, 10 méretű lövedék)
        this.currentWeapon = new Weapon(10, 2.0f, 400.0f, 10.0f);
    }

    /**
     * Frissíti a játékos állapotát (mozgás, lövés, ütközések).
     * @param deltaTime Az utolsó frissítés óta eltelt idő.
     * @param args Változó számú argumentumok, elsősorban InputHandler és CollisionManager.
     */
    @Override
    public void update(float deltaTime, Object... args) {
        InputHandler inputHandler = null;
        CollisionManager collisionManager = null;
        double currentTime = 0; // Hozzáadjuk az aktuális időt a fegyver cooldownhoz

        for (Object arg : args) {
            if (arg instanceof InputHandler) {
                inputHandler = (InputHandler) arg;
            } else if (arg instanceof CollisionManager) {
                collisionManager = (CollisionManager) arg;
            } else if (arg instanceof Double) { // A GameManager átadja az aktuális időt
                currentTime = (Double) arg;
            }
        }

        if (inputHandler == null) return;

        float oldX = x;
        float oldY = y;

        float dx = 0;
        float dy = 0;

        if (inputHandler.isKeyDown(GLFW_KEY_W)) {
            dy -= moveSpeed * deltaTime;
        }
        if (inputHandler.isKeyDown(GLFW_KEY_S)) {
            dy += moveSpeed * deltaTime;
        }
        if (inputHandler.isKeyDown(GLFW_KEY_A)) {
            dx -= moveSpeed * deltaTime;
        }
        if (inputHandler.isKeyDown(GLFW_KEY_D)) {
            dx += moveSpeed * deltaTime;
        }

        // Mozgás alkalmazása
        this.x += dx;
        this.y += dy;

        // Ütközés ellenőrzés és feloldás a falakkal
        if (collisionManager != null) {
            collisionManager.resolvePlayerTileCollisions(this);
        }

        // Lövés kezelése (egér bal gombjára)
        if (inputHandler.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && currentWeapon != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer xPos = stack.mallocDouble(1);
                DoubleBuffer yPos = stack.mallocDouble(1);
                glfwGetCursorPos(windowHandle, xPos, yPos); // Egér pozíciójának lekérdezése

                float mouseX = (float) xPos.get(0);
                float mouseY = (float) yPos.get(0);

                // A fegyver shoot metódusa majd visszatér egy lövedékkel, ha lőhet
                // Ez a lövedék majd hozzá kell adni a GameManager lövedék listájához
                // A GameManager felel majd a lövedékek update/render/collision logikájáért
                // Jelenleg csak a példa kedvéért, nem itt történik a lövedék tényleges hozzáadása
                // (ezt majd a GameManager update() metódusában kell kezelni).
                Projectile newProjectile = currentWeapon.shoot(this, x, y, mouseX, mouseY, (float)currentTime);
                if (newProjectile != null) {
                    // Ezt a lövedéket VALAHOGYAN át kell adni a GameManager-nek,
                    // hogy hozzáadja a saját listájához.
                    // Jelenleg ez egy "TODO" pont a GameManager számára.
                    // Egy lehetséges megoldás: a Player.update() visszatérhet Projectile listával,
                    // vagy a GameManager egy listát ad át a Player.update() metódusnak, amit a Player feltölthet.
                    // Most egyszerűség kedvéért csak kiírom a konzolra.
                    // System.out.println("Lövedék kilőve!");
                }
            }
        }
    }

    @Override
    public void render() {
        if (texture != null) {
            texture.bind();
            glColor3f(1.0f, 1.0f, 1.0f);
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(x, y);
            glTexCoord2f(1, 0); glVertex2f(x + width, y);
            glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
            glTexCoord2f(0, 1); glVertex2f(x, y + height);
            glEnd();
            texture.unbind();
        } else {
            glColor3f(1.0f, 0.0f, 1.0f); // Magenta, ha nincs textúra
            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(x + width, y);
            glVertex2f(x + width, y + height);
            glVertex2f(x, y + height);
            glEnd();
        }
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }
}
