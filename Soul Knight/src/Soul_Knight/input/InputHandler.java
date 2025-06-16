package Soul_Knight.input;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private long window;
    private boolean[] keys;
    private boolean[] mouseButtons;
    private boolean[] mouseButtonsLastFrame;

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
                }
            }
        });

        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                    mouseButtons[button] = action != GLFW_RELEASE;
                    // System.out.println("DEBUG: GLFW Egérgomb Esemény: Gomb=" + button + ", Akció=" + action + " (1=nyomva, 0=felengedve). Jelenlegi mouseButtons[" + button + "]=" + mouseButtons[button]); // Kikommentelve
                }
            }
        });
    }

    public void update() {
        System.arraycopy(mouseButtons, 0, mouseButtonsLastFrame, 0, mouseButtons.length);
        // System.out.println("DEBUG: InputHandler.update() hívva. mouseButtons[0]=" + mouseButtons[GLFW_MOUSE_BUTTON_LEFT] + ", mouseButtonsLastFrame[0]=" + mouseButtonsLastFrame[GLFW_MOUSE_BUTTON_LEFT]); // Kikommentelve
    }

    public boolean isKeyDown(int keyCode) {
        return keys[keyCode];
    }

    public boolean isMouseButtonDown(int buttonCode) {
        return mouseButtons[buttonCode];
    }

    public boolean isMouseButtonPressed(int buttonCode) {
        // System.out.println("DEBUG: isMouseButtonPressed ellenőrzés: Gomb=" + buttonCode + ", Jelenlegi=" + mouseButtons[buttonCode] + ", Előző=" + mouseButtonsLastFrame[buttonCode] + ", Eredmény=" + (mouseButtons[buttonCode] && !mouseButtonsLastFrame[buttonCode])); // Kikommentelve
        return mouseButtons[buttonCode] && !mouseButtonsLastFrame[buttonCode];
    }
}
