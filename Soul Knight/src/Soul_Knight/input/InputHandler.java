package Soul_Knight.input;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private long window;
    private boolean[] keys;
    private boolean[] mouseButtons;

    public InputHandler(long window) {
        this.window = window;
        this.keys = new boolean[GLFW_KEY_LAST + 1]; // Összes billentyű állapotának tárolása
        this.mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1]; // Összes egérgomb állapotának tárolása

        // Billentyűzet callback beállítása
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key >= 0 && key <= GLFW_KEY_LAST) {
                    keys[key] = action != GLFW_RELEASE; // Igaz, ha lenyomva vagy ismételve, Hamis, ha felengedve
                }
            }
        });

        // Egér gomb callback beállítása
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                    mouseButtons[button] = action != GLFW_RELEASE;
                }
            }
        });
    }

    public boolean isKeyDown(int keyCode) {
        return keys[keyCode];
    }

    public boolean isMouseButtonDown(int buttonCode) {
        return mouseButtons[buttonCode];
    }

    // Később hozzáadhatunk egér pozíció lekérdezést is
}