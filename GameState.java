package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import world.Dungeon;

public class GameState {
    private Dungeon dungeon;
    private boolean dungeonGenerated;
    private boolean bossDefeated;
    private Map<Integer, PlayerState> playerStates = new ConcurrentHashMap<>();
    private List<EnemyState> enemyStates = new ArrayList<>();
    private List<ProjectileState> projectileStates = new ArrayList<>();

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeonGenerated(boolean dungeonGenerated) {
        this.dungeonGenerated = dungeonGenerated;
    }

    public boolean isDungeonGenerated() {
        return dungeonGenerated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void addPlayerState(int playerId, PlayerState state) {
        playerStates.put(playerId, state);
    }

    public void removePlayerState(int playerId) {
        playerStates.remove(playerId);
    }

    public Map<Integer, PlayerState> getPlayerStates() {
        return playerStates;
    }

    public List<EnemyState> getEnemyStates() {
        return enemyStates;
    }

    public List<ProjectileState> getProjectileStates() {
        return projectileStates;
    }

    public String serializeGameState() {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAYERS:");
        for (PlayerState ps : playerStates.values()) {
            sb.append(ps.serialize()).append("|");
        }
        sb.append(";ENEMIES:");
        for (EnemyState es : enemyStates) {
            sb.append(es.serialize()).append("|");
        }
        sb.append(";PROJECTILES:");
        for (ProjectileState ps : projectileStates) {
            sb.append(ps.serialize()).append("|");
        }
        return sb.toString();
    }

    public void updatePlayerPosition(int playerId, float x, float y) {
        PlayerState playerState = playerStates.get(playerId);
        if (playerState != null) {
            playerState.setX(x);
            playerState.setY(y);
        }
    }

    public void updateEnemyPosition(int enemyId, float x, float y) {
        for (EnemyState enemy : enemyStates) {
            if (enemy.getEnemyId() == enemyId) {
                enemy.setX(x);
                enemy.setY(y);
                break;
            }
        }
    }
}