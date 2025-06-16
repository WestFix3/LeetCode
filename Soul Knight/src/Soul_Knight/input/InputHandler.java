package Soul_Knight.input;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private long window;
    private boolean[] keys;
    private boolean[] mouseButtons;
    private boolean[] mouseButtonsLastFrame; // Előző képkocka egérgomb állapotai

    public InputHandler(long window) {
        this.window = window;
        this.keys = new boolean[GLFW_KEY_LAST + 1];
        this.mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
        this.mouseButtonsLastFrame = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key >= 0 && key <= GLFW_KEY_LAST) {
                    keys[key] = action != GLFW_RELEASE;
                    // System.out.println("DEBUG: Billentyű esemény: Key=" + key + ", Action=" + action + ", keys[" + key + "]=" + keys[key]);
                }
            }
        });

        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                    mouseButtons[button] = action != GLFW_RELEASE;
                }
            }
        });
    }

    /**
     * Ezt a metódust a GameManager hívja meg minden frissítés elején,
     * hogy elmentse az egérgombok előző állapotát.
     */
    public void update() {
        // DEBUG: Ellenőrizzük az update() hívását és a másolást
        // System.out.println("DEBUG: InputHandler.update() hívva. mouseButtons[0]=" + mouseButtons[GLFW_MOUSE_BUTTON_LEFT] + ", mouseButtonsLastFrame[0]=" + mouseButtonsLastFrame[GLFW_MOUSE_BUTTON_LEFT]);
    }

    public boolean isKeyDown(int keyCode) {
        return keys[keyCode];
    }

    public boolean isMouseButtonDown(int buttonCode) {
        return mouseButtons[buttonCode];
    }

    /**
     * Ellenőrzi, hogy az egérgomb éppen most lett-e lenyomva (egyetlen kattintás).
     * @param buttonCode Az egérgomb kódja (pl. GLFW_MOUSE_BUTTON_LEFT).
     * @return Igaz, ha a gomb lenyomva van, és az előző képkockán nem volt lenyomva.
     */
    public boolean isMouseButtonPressed(int buttonCode) {
        // FONTOS DEBUG: Láthatjuk a jelenlegi és az előző állapotot minden egyes ellenőrzéskor
        return mouseButtons[buttonCode] && !mouseButtonsLastFrame[buttonCode];
    }
}
