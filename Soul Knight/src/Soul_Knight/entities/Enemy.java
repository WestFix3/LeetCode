package Soul_Knight.entities;

import Soul_Knight.rendering.Texture;
import Soul_Knight.input.InputHandler; // Bár az ellenségnek nincs inputja, az Entity update() szignatúra miatt. Később törölhető.

import static org.lwjgl.opengl.GL11.*;

/**
 * Alapvető ellenség entitás, életerővel és textúrával.
 */
public class Enemy extends Entity {

    private int health;
    private Texture texture;
    private float moveSpeed = 50.0f; // Példa mozgási sebesség

    public Enemy(float x, float y, float width, float height, Texture texture, int initialHealth) {
        super(x, y, width, height);
        this.texture = texture;
        this.health = initialHealth;
    }

    @Override
    public void update(float deltaTime, Object... args) {
        // Egyszerű mozgás példa (pl. jobbra-balra) - Később AI jön ide
        // Ha szeretnéd, hogy az ellenség mozogjon, itt kellene az AI-t implementálni.
        // Például:
        // this.x += moveSpeed * deltaTime;
        // if (this.x > 800 || this.x < 100) moveSpeed *= -1; // Visszafordul
    }

    /**
     * Kár kiosztása az ellenségnek.
     * @param damage A kiosztott sebzés mértéke.
     */
    public void takeDamage(int damage) {
        this.health -= damage;
        System.out.println("Ellenség sebződött! HP: " + this.health);
    }

    /**
     * Ellenőrzi, hogy az ellenség él-e még.
     * @return Igaz, ha az életereje nagyobb, mint 0, hamis egyébként.
     */
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public void render() {
        if (texture != null) {
            texture.bind();
            glColor3f(1.0f, 1.0f, 1.0f); // Fehér szín, hogy a textúra színei érvényesüljenek
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(x, y);
            glTexCoord2f(1, 0); glVertex2f(x + width, y);
            glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
            glTexCoord2f(0, 1); glVertex2f(x, y + height);
            glEnd();
            texture.unbind();
        } else {
            // Ha nincs textúra, rajzoljon piros négyzetet
            glColor3f(1.0f, 0.0f, 0.0f);
            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(x + width, y);
            glVertex2f(x + width, y + height);
            glVertex2f(x, y + height);
            glEnd();
            glColor3f(1.0f, 1.0f, 1.0f);
        }
    }
}
