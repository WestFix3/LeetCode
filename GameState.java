package core;

import java.util.*;
import java.util.concurrent.*;

public class GameState {
    private boolean dungeonGenerated = false;
    private boolean bossDefeated = false;
    private int currentLevel = 1;

    // Egyszerűsített állapotok - csak a multiplayer szinkronizáláshoz szükségesek
    private Map<Integer, PlayerState> playerStates = new ConcurrentHashMap<>();
    private List<EnemyState> enemyStates = new CopyOnWriteArrayList<>();
    private List<ProjectileState> projectileStates = new CopyOnWriteArrayList<>();
    private List<EffectState> effectStates = new CopyOnWriteArrayList<>();

    // Getterek/Setterek
    public boolean isDungeonGenerated() { return dungeonGenerated; }
    public void setDungeonGenerated(boolean dungeonGenerated) { this.dungeonGenerated = dungeonGenerated; }
    public boolean isBossDefeated() { return bossDefeated; }
    public void setBossDefeated(boolean bossDefeated) { this.bossDefeated = bossDefeated; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public Map<Integer, PlayerState> getPlayerStates() { return playerStates; }
    public List<EnemyState> getEnemyStates() { return enemyStates; }
    public List<ProjectileState> getProjectileStates() { return projectileStates; }
    public List<EffectState> getEffectStates() { return effectStates; }

    public void addPlayerState(int playerId, PlayerState state) {
        playerStates.put(playerId, state);
    }

    public void removePlayerState(int playerId) {
        playerStates.remove(playerId);
    }

    public void updatePlayerPosition(int playerId, float x, float y) {
        PlayerState state = playerStates.get(playerId);
        if (state != null) {
            state.setX(x);
            state.setY(y);
        }
    }

    // Játékállapot szinkronizálás
    public String serializeGameState() {
        StringBuilder sb = new StringBuilder();

        // Játékosok
        sb.append("PLAYERS:");
        for (PlayerState player : playerStates.values()) {
            sb.append(player.serialize()).append("|");
        }
        if (!playerStates.isEmpty()) sb.setLength(sb.length() - 1); // Remove last |
        sb.append(";");

        // Ellenségek
        sb.append("ENEMIES:");
        for (EnemyState enemy : enemyStates) {
            sb.append(enemy.serialize()).append("|");
        }
        if (!enemyStates.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append(";");

        // Lövedékek
        sb.append("PROJECTILES:");
        for (ProjectileState projectile : projectileStates) {
            sb.append(projectile.serialize()).append("|");
        }
        if (!projectileStates.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append(";");

        // Effektek
        sb.append("EFFECTS:");
        for (EffectState effect : effectStates) {
            sb.append(effect.serialize()).append("|");
        }
        if (!effectStates.isEmpty()) sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}