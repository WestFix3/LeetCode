package Soul_Knight.rendering;

import static org.lwjgl.opengl.GL11.*; // Importáljuk az OpenGL konstansokat

// Egy egyszerű osztály a betöltött textúra reprezentálására
public class Texture {
    private final int id; // Az OpenGL textúra azonosítója
    private final int width; // A textúra szélessége pixelekben
    private final int height; // A textúra magassága pixelekben

    public Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Bekapcsolja ezt a textúrát az OpenGL renderelési pipeline-ban
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    // Kikapcsolja a textúrát
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Felszabadítja a textúra erőforrásait a GPU-ról
    public void delete() {
        glDeleteTextures(id);
    }
}