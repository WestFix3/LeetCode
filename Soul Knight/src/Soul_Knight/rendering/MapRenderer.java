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
                    Texture texture = tile.getTexture();
                    if (texture != null) {
                        texture.bind();
                        glColor3f(1.0f, 1.0f, 1.0f); // Fehér szín, hogy a textúra színei érvényesüljenek

                        float screenX = x * tileSize;
                        float screenY = y * tileSize;

                        glBegin(GL_QUADS);
                        // TEXTÚRA KOORDINÁTÁK JAVÍTVA A 180 FOKOS ELFORGATÁSRA
                        // Az (0,0) textúra koordináta a betöltött kép "alján" van, ha a stbi_set_flip_vertically_on_load(true) be van állítva.
                        // Ahhoz, hogy a kép helyesen jelenjen meg (a fájl teteje a quad tetején), a V koordinátákat fordítva adjuk meg.
                        glTexCoord2f(0, 1); glVertex2f(screenX, screenY); // Bal felső sarok (quad) -> (0,1) textúra koordináta (textúra bal alsó)
                        glTexCoord2f(1, 1); glVertex2f(screenX + tileSize, screenY); // Jobb felső sarok (quad) -> (1,1) textúra koordináta (textúra jobb alsó)
                        glTexCoord2f(1, 0); glVertex2f(screenX + tileSize, screenY + tileSize); // Jobb alsó sarok (quad) -> (1,0) textúra koordináta (textúra jobb felső)
                        glTexCoord2f(0, 0); glVertex2f(screenX, screenY + tileSize); // Bal alsó sarok (quad) -> (0,0) textúra koordináta (textúra bal felső)
                        glEnd();

                        texture.unbind();
                    } else {
                        // Ha valamiért nincs textúra (pl. hiba), akkor rajzoljon egy színes négyzetet
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
