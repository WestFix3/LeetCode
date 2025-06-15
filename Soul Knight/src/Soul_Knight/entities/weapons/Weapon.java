package Soul_Knight.entities.weapons;

import Soul_Knight.entities.Entity;
import Soul_Knight.entities.Projectile;

/**
 * Alapvető fegyver osztály, amely lövedékeket képes kilőni.
 */
public class Weapon {

    private int damage;
    private float fireRate; // Másodpercenkénti lövések száma
    private float lastShotTime; // Az utolsó lövés óta eltelt idő
    private float projectileSpeed;
    private float projectileSize;

    public Weapon(int damage, float fireRate, float projectileSpeed, float projectileSize) {
        this.damage = damage;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.projectileSize = projectileSize;
        this.lastShotTime = 0; // Kezdetben azonnal lőhet
    }

    /**
     * Megpróbál kilőni egy lövedéket.
     * @param shooter Az entitás, ami lő.
     * @param startX A lövedék kezdő X koordinátája.
     * @param startY A lövedék kezdő Y koordinátája.
     * @param targetX A cél X koordinátája.
     * @param targetY A cél Y koordinátája.
     * @param currentTime A játék aktuális ideje.
     * @return Egy Projectile objektum, ha sikeresen lőtt, különben null.
     */
    public Projectile shoot(Entity shooter, float startX, float startY, float targetX, float targetY, float currentTime) {
        if (currentTime - lastShotTime < 1.0f / fireRate) {
            return null; // Még cooldownon van
        }

        // Irányvektor kiszámítása
        float dirX = targetX - startX;
        float dirY = targetY - startY;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (length == 0) return null; // Ne osszunk nullával

        dirX /= length; // Normalizálás
        dirY /= length;

        lastShotTime = currentTime; // Frissítjük az utolsó lövés idejét

        // Létrehozzuk a lövedéket
        // A lövedéket elhelyezzük a játékos közepétől, és a mérete legyen kisebb
        float projX = startX + shooter.getWidth() / 2 - projectileSize / 2;
        float projY = startY + shooter.getHeight() / 2 - projectileSize / 2;

        return new Projectile(projX, projY, projectileSize, projectileSize, damage, projectileSpeed, dirX, dirY, shooter);
    }
}
