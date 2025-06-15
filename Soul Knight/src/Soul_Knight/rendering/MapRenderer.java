package Soul_Knight.rendering;

import Soul_Knight.world.Dungeon;
import Soul_Knight.world.Tile;
import Soul_Knight.world.Room;

import static org.lwjgl.opengl.GL11.*;

public class MapRenderer {

    public MapRenderer() {
        // A MapRenderer-nek nem kell sok inicializálás,
        // mivel az OpenGL kontextust már beállítottuk.
    }

    public void render(Dungeon dungeon) {
        Room room = dungeon.getMainRoom();
        int tileSize = room.getTileSize();

        for (int x = 0; x < room.getWidthTiles(); x++) {
            for (int y = 0; y < room.getHeightTiles(); y++) {
                Tile tile = room.getTile(x, y);
                if (tile != null) {
                    Texture texture = tile.getTexture(); // Lekérjük a csempe textúráját
                    if (texture != null) {
                        texture.bind(); // Bekapcsolja a csempe textúráját
                        glColor3f(1.0f, 1.0f, 1.0f); // Fehér szín, hogy a textúra színei érvényesüljenek

                        // A csempe pozíciója a képernyőn
                        float screenX = x * tileSize;
                        float screenY = y * tileSize;

                        glBegin(GL_QUADS);
                        // Textúra koordináták (0,0) a bal alsó, (1,1) a jobb felső
                        // Mivel az STBImage alapértelmezetten felülről-lefelé olvassa be,
                        // és az OpenGL alulról-felfelé rajzolja a textúrákat,
                        // ezért a vertikális koordinátákat felcserélhetjük (vagy stbi_set_flip_vertically_on_load(true)
                        // használatával fordíthatunk a betöltéskor).
                        // Az STBImage esetében a stbi_set_flip_vertically_on_load(true) használatával az (0,0) a bal felső sarok lesz a textúrában.
                        // Tehát a Vertex 2f(x,y) a bal felső sarka lesz a négyzetnek.
                        glTexCoord2f(0, 0); glVertex2f(screenX, screenY); // Bal felső sarok
                        glTexCoord2f(1, 0); glVertex2f(screenX + tileSize, screenY); // Jobb felső sarok
                        glTexCoord2f(1, 1); glVertex2f(screenX + tileSize, screenY + tileSize); // Jobb alsó sarok
                        glTexCoord2f(0, 1); glVertex2f(screenX, screenY + tileSize); // Bal alsó sarok
                        glEnd();

                        texture.unbind(); // Kikapcsolja a textúrát
                    } else {
                        // Ha valamiért nincs textúra (pl. hiba), akkor rajzoljon egy színes négyzetet
                        float colorR, colorG, colorB;
                        switch (tile.getType()) {
                            case FLOOR:
                                colorR = 0.7f; colorG = 0.7f; colorB = 0.7f; // Világosszürke
                                break;
                            case WALL:
                                colorR = 0.3f; colorG = 0.3f; colorB = 0.3f; // Sötétszürke
                                break;
                            default:
                                colorR = 1.0f; colorG = 0.0f; colorB = 1.0f; // Magenta, hiba jelzésére
                                break;
                        }
                        glColor3f(colorR, colorG, colorB);
                        float screenX = x * tileSize;
                        float screenY = y * tileSize;
                        glBegin(GL_QUADS);
                        glVertex2f(screenX, screenY);
                        glVertex2f(screenX + tileSize, screenY);
                        glVertex2f(screenX + tileSize, screenY + tileSize);
                        glVertex2f(screenX, screenY + tileSize);
                        glEnd();
                    }
                }
            }
        }
        glColor3f(1.0f, 1.0f, 1.0f); // Visszaállítja a fehér színt
    }
}