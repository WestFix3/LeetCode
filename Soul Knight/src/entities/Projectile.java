package entities;

import rendering.Texture;
import physics.Rectangle; // Szükséges a Rectanlgle osztály eléréséhez

import static org.lwjgl.opengl.GL11.*;

/**
 * Lövedék entitás, ami sebzést okozhat és mozog.
 */
public class Projectile extends Entity {

    private int damage;
    private float speed;
    private float dirX, dirY; // Mozgás iránya (normalizált vektor)
    private Entity owner; // A lövedék tulajdonosa (aki kilőtte), hogy ne sebezzük önmagunkat
    private boolean alive = true; // Él-e még a lövedék
    private float lifetime = 3.0f; // Élettartam másodpercben
    private float currentLifetime = 0.0f;

    public Projectile(float x, float y, float width, float height, int damage, float speed, float dirX, float dirY, Entity owner) {
        super(x, y, width, height);
        this.damage = damage;
        this.speed = speed;
        this.dirX = dirX;
        this.dirY = dirY;
        this.owner = owner;
    }

    @Override
    public void update(float deltaTime, Object... args) {
        if (!alive) return;

        // Mozgás
        x += dirX * speed * deltaTime;
        y += dirY * speed * deltaTime;

        // Élettartam csökkentése
        currentLifetime += deltaTime;
        if (currentLifetime >= lifetime) {
            alive = false; // Lejár az élettartama
        }
    }

    @Override
    public void render() {
        if (!alive) return;

        // Lövedék rajzolása (egyszerű fehér négyzetként)
        glColor3f(1.0f, 1.0f, 0.0f); // Sárga lövedék
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();
        glColor3f(1.0f, 1.0f, 1.0f); // Visszaállítja a fehér színt
    }

    public int getDamage() {
        return damage;
    }

    public Entity getOwner() {
        return owner;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}

