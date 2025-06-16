package Soul_Knight.entities;

import Soul_Knight.input.InputHandler;
import Soul_Knight.rendering.Texture;
import Soul_Knight.physics.CollisionManager;
import Soul_Knight.entities.weapons.Weapon; // Győződj meg róla, hogy ez az import megvan!

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import java.nio.DoubleBuffer;
import org.lwjgl.system.MemoryStack;

public class Player extends Entity {

    private float moveSpeed = 200.0f;
    private Texture texture;
    private Weapon currentWeapon;
    private long windowHandle;

    public Player(float x, float y, float width, float height, Texture texture, long windowHandle) {
        super(x, y, width, height);
        this.texture = texture;
        this.windowHandle = windowHandle;
        // Fegyver inicializálása: (sebzés, tűzgyorsaság, lövedéksebesség, lövedékméret)
        // A tűzgyorsaságot most 10.0f-re állítjuk, ami 0.1 másodperc töltési időt jelent.
        this.currentWeapon = new Weapon(10, 10.0f, 400.0f, 10.0f); // <<-- VÁLTOZÁS ITT!
    }

    @Override
    public void update(float deltaTime, Object... args) {
        InputHandler inputHandler = null;
        CollisionManager collisionManager = null;
        double currentTime = 0;

        for (Object arg : args) {
            if (arg instanceof InputHandler) {
                inputHandler = (InputHandler) arg;
            } else if (arg instanceof CollisionManager) {
                collisionManager = (CollisionManager) arg; // JAVÍTVA: Explicit cast hozzáadva
            } else if (arg instanceof Double) {
                currentTime = (Double) arg;
            }
        }

        if (inputHandler == null) return;

        float prevX = x;
        float prevY = y;

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

        this.x += dx;
        this.y += dy;

        if (collisionManager != null) {
            collisionManager.resolvePlayerTileCollisions(this, prevX, prevY);
        }

        if (inputHandler.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && currentWeapon != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer xPos = stack.mallocDouble(1);
                DoubleBuffer yPos = stack.mallocDouble(1);
                glfwGetCursorPos(windowHandle, xPos, yPos);

                float mouseX = (float) xPos.get(0);
                float mouseY = (float) yPos.get(0);

                Projectile newProjectile = currentWeapon.shoot(this, x, y, mouseX, mouseY, (float)currentTime);
                if (newProjectile != null) {
                    // Lövedék hozzáadását a GameManager kezeli
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
            glTexCoord2f(0, 1); glVertex2f(x, y);
            glTexCoord2f(1, 1); glVertex2f(x + width, y);
            glTexCoord2f(1, 0); glVertex2f(x + width, y + height);
            glTexCoord2f(0, 0); glVertex2f(x, y + height);
            glEnd();
            texture.unbind();
        } else {
            glColor3f(1.0f, 0.0f, 1.0f);
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
