import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Main {
    // Ablak mérete
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    // Pálya mérete (16x16 rács)
    private static final int GRID_SIZE = 16;
    private static final int CELL_SIZE = WINDOW_WIDTH / GRID_SIZE;

    // Játékos pozíciója
    private static int playerX = 8;
    private static int playerY = 8;

    // Input állapot nyomon követése
    private static boolean keyProcessed = false;

    // Textúra ID-k
    private static int playerTexture;
    private static int backgroundTexture; // ÚJ: Háttér textúra ID

    public static void main(String[] args) {
        // GLFW inicializálása
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init hiba!");
        }

        // Ablak létrehozása
        long window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "16x16 Pálya + Karakter + Háttér", 0, 0);
        if (window == 0) {
            glfwTerminate();
            throw new IllegalStateException("Ablak létrehozása sikertelen!");
        }

        // OpenGL kontextus beállítása
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Textúrák betöltése
        playerTexture = loadTexture("C:\\Users\\Felhasználó\\Desktop\\SZAKDOLGOZAT\\character1.png");
        backgroundTexture = loadTexture("C:\\Users\\Felhasználó\\Desktop\\SZAKDOLGOZAT\\grass.png");

        // 2D-s nézet beállítása
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        // Fő játékciklus
        while (!glfwWindowShouldClose(window)) {
            // Háttörlés (fekete) - Ezt megtarthatod, de a háttérkép eltakarja.
            glClear(GL_COLOR_BUFFER_BIT);

            // ÚJ: Háttér rajzolása (először, hogy alul legyen)
            renderBackground();

            // Input kezelése
            handleInput(window);

            // Pálya és játékos rajzolása
            //renderGrid();
            renderPlayer();

            // Buffercsere
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // Tisztítás
        glDeleteTextures(playerTexture);
        glDeleteTextures(backgroundTexture); // ÚJ: Háttér textúra felszabadítása
        glfwTerminate();
    }

    // Textúra betöltése (ugyanaz, mint eredetileg)
    private static int loadTexture(String filePath) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Textúra paraméterek beállítása
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Kép betöltése STB-vel
            ByteBuffer imageBuffer = STBImage.stbi_load(filePath, width, height, channels, 4);
            if (imageBuffer == null) {
                throw new RuntimeException("Kép betöltése sikertelen: " + STBImage.stbi_failure_reason());
            }

            // Textúra feltöltése
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);

            // Memória felszabadítása
            STBImage.stbi_image_free(imageBuffer);
        }

        return textureID;
    }

    // Input kezelése (ugyanaz, mint eredetileg)
    private static void handleInput(long window) {
        if ((glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) && !keyProcessed) {
            if (playerX > 0) playerX--;
            keyProcessed = true;
        } else if ((glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) && !keyProcessed) {
            if (playerX < GRID_SIZE - 1) playerX++;
            keyProcessed = true;
        } else if ((glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) && !keyProcessed) {
            if (playerY > 0) playerY--;
            keyProcessed = true;
        } else if ((glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) && !keyProcessed) {
            if (playerY < GRID_SIZE - 1) playerY++;
            keyProcessed = true;
        }

        if (glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_LEFT) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_RIGHT) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_UP) != GLFW_PRESS &&
                glfwGetKey(window, GLFW_KEY_DOWN) != GLFW_PRESS) {
            keyProcessed = false;
        }
    }

    // Rács rajzolása (ugyanaz, mint eredetileg)
    private static void renderGrid() {
        glColor3f(1.0f, 1.0f, 1.0f);

        for (int x = 0; x <= GRID_SIZE; x++) {
            glBegin(GL_LINES);
            glVertex2f(x * CELL_SIZE, 0);
            glVertex2f(x * CELL_SIZE, WINDOW_HEIGHT);
            glEnd();
        }

        for (int y = 0; y <= GRID_SIZE; y++) {
            glBegin(GL_LINES);
            glVertex2f(0, y * CELL_SIZE);
            glVertex2f(WINDOW_WIDTH, y * CELL_SIZE);
            glEnd();
        }
    }

    // Játékos rajzolása (most már képként)
    private static void renderPlayer() {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, playerTexture);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(playerX * CELL_SIZE, playerY * CELL_SIZE);
        glTexCoord2f(1, 0);
        glVertex2f((playerX + 1) * CELL_SIZE, playerY * CELL_SIZE);
        glTexCoord2f(1, 1);
        glVertex2f((playerX + 1) * CELL_SIZE, (playerY + 1) * CELL_SIZE);
        glTexCoord2f(0, 1);
        glVertex2f(playerX * CELL_SIZE, (playerY + 1) * CELL_SIZE);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }

    // ÚJ: Háttér rajzolása
    private static void renderBackground() {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, backgroundTexture);
        glColor3f(1.0f, 1.0f, 1.0f); // Fontos, hogy fehér legyen, hogy a textúra színei érvényesüljenek

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(0, 0);
        glTexCoord2f(1, 0); glVertex2f(WINDOW_WIDTH, 0);
        glTexCoord2f(1, 1); glVertex2f(WINDOW_WIDTH, WINDOW_HEIGHT);
        glTexCoord2f(0, 1); glVertex2f(0, WINDOW_HEIGHT);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }
}