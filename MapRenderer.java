package rendering;

import world.Dungeon;
import world.Tile;

import static org.lwjgl.opengl.GL11.*;

public class MapRenderer {

    public MapRenderer() {
        // A MapRenderer-nek nem kell sok inicializálás,
        // mivel az OpenGL kontextust már beállítottuk.
    }

    public void render(Dungeon dungeon) { // <-- Dungeon-t fogad paraméterként
        int tileSize = dungeon.getTileSize(); // A tileSize a Dungeon-ből jön

        // Iterálunk a Dungeon csempéin
        for (int x = 0; x < dungeon.getWidthTiles(); x++) { // <-- Dungeon.getWidthTiles()
            for (int y = 0; y < dungeon.getHeightTiles(); y++) { // <-- Dungeon.getHeightTiles()
                Tile tile = dungeon.getTile(x, y); // <-- Közvetlenül a Dungeon-től kérjük a Tile-t
                if (tile != null) {
                    Texture texture = tile.getTexture();
                    if (texture != null) {
                        texture.bind();
                        glColor3f(1.0f, 1.0f, 1.0f); // Fehér szín, hogy a textúra színei érvényesüljenek

                        float screenX = x * tileSize;
                        float screenY = y * tileSize;

                        glBegin(GL_QUADS);
                        glTexCoord2f(0, 1); glVertex2f(screenX, screenY);
                        glTexCoord2f(1, 1); glVertex2f(screenX + tileSize, screenY);
                        glTexCoord2f(1, 0); glVertex2f(screenX + tileSize, screenY + tileSize);
                        glTexCoord2f(0, 0); glVertex2f(screenX, screenY + tileSize);
                        glEnd();

                        texture.unbind();
                    } else {
                        // Ha valamiért nincs textúra, rajzoljon egy színes négyzetet
                        float colorR, colorG, colorB;
                        switch (tile.getType()) {
                            case FLOOR:
                                colorR = 0.7f; colorG = 0.7f; colorB = 0.7f;
                                break;
                            case WALL:
                                colorR = 0.3f; colorG = 0.3f; colorB = 0.3f;
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
        glColor3f(1.0f, 1.0f, 1.0f);
    }
}