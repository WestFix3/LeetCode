package core;

public class EnemyState {
    private int enemyId;
    private String enemyType;
    private float x, y;
    private float health;
    private float maxHealth;
    private boolean isAlive = true;

    public EnemyState(int enemyId, String enemyType, float x, float y, float health, float maxHealth) {
        this.enemyId = enemyId;
        this.enemyType = enemyType;
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    // Getterek/Setterek
    public int getEnemyId() { return enemyId; }
    public void setEnemyId(int enemyId) { this.enemyId = enemyId; }
    public String getEnemyType() { return enemyType; }
    public void setEnemyType(String enemyType) { this.enemyType = enemyType; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }
    public float getMaxHealth() { return maxHealth; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = maxHealth; }
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { this.isAlive = alive; }

    public String serialize() {
        return String.format("%d,%s,%.2f,%.2f,%.1f,%.1f,%b",
                enemyId, enemyType, x, y, health, maxHealth, isAlive);
    }
}