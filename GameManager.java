package core;

import entities.*;
import world.Dungeon;
import world.DungeonGenerator;
import input.InputHandler;
import rendering.MapRenderer;
import rendering.Texture;
import rendering.TextureLoader;
import rendering.TextRenderer;
import entities.weapons.WeaponInterface;
import entities.weapons.Weapon;
import entities.weapons.MeleeWeapon;
import entities.weapons.WeaponFactory;
import physics.CollisionManager;
import world.Tile;
import rendering.Camera;
import rendering.Sprite;
import core.GameSaveHandler;

import entities.Effect.EffectType;
import entities.Effect.PlayerEffect;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;
import java.nio.DoubleBuffer;
import java.util.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameManager {

    private long window;
    private int width = 1280;
    private int height = 720;
    private String title = "Soul Knight LWJGL";

    private Player player;
    private Enemy enemy;
    private Enemy boss;
    private Dungeon currentDungeon;
    private MapRenderer mapRenderer;
    private InputHandler inputHandler;
    private CollisionManager collisionManager;
    private Camera camera;
    private HUD hud;

    private WeaponFactory weaponFactory;

    private AbilitySelectionScreen abilitySelectionScreen;
    private UpgradeChoiceScreen upgradeScreen;
    private GameOverScreen gameOverScreen;
    private GameState currentState = GameState.ABILITY_SELECTION;

    // Path debug beállítás
    private boolean showPathDebug = true;

    // Multiplayer állapot
    private boolean isMultiplayer = false;
    private boolean isHost = false;
    private MultiplayerClient multiplayerClient;
    private String serverIp = "localhost";
    private String serverPort = "5555";
    private int myPlayerId = -1;
    private Map<Integer, Player> otherPlayers = new HashMap<>();
    private Map<Integer, PlayerState> serverPlayerStates = new HashMap<>();

    // Interpolációhoz
    private float interpolationSpeed = 5.0f;
    Random rand = new Random();

    public enum GameState {
        LOBBY,
        ABILITY_SELECTION,
        GAMEPLAY,
        UPGRADE_CHOICE,
        GAME_OVER,
        LOAD_GAME
    }

    private Texture playerIdleTexture;
    private List<Texture> walkFrames;
    private List<Texture> activeWalkFrames;
    private Map<Tile.TileType, Texture> tileTextures;
    private Texture enemyTexture;
    private Map<Integer, Texture> boxDamageTextures;
    private Map<Integer, Texture> gateAnimationTextures;

    private List<Projectile> projectiles;
    private List<Effect> effects;
    private List<PlayerEffect> playerEffects;

    private Map<Effect.EffectType, Texture> effectTextures;

    private Texture weaponCrateTexture;
    private Texture openCrateTexture;
    private Texture emptyCrateTexture;

    private float meleeCooldownTime = 0.5f;
    private float lastMeleeAttackTime = Float.NEGATIVE_INFINITY;

    private TextRenderer textRenderer;
    private Texture fontTexture;
    private Texture teleportPadTexture;

    private boolean bossDefeated = false;
    private Player.Ability playerAbility;
    private String playerName;
    private int saveIdToLoad = -1;

    // Setter a path debug beállításhoz
    public void setShowPathDebug(boolean showPathDebug) {
        this.showPathDebug = showPathDebug;
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            width = pWidth.get(0);
            height = pHeight.get(0);
        }

        inputHandler = new InputHandler(window);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);

        glfwShowWindow(window);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

        camera = new Camera(width, height);
        hud = new HUD(width, height);

        weaponFactory = new WeaponFactory();
        weaponFactory.loadWeaponSprites();

        abilitySelectionScreen = new AbilitySelectionScreen(window, width, height);
        upgradeScreen = new UpgradeChoiceScreen(window, width, height, player);
    }

    private void initGameplay(String playerName, Player.Ability ability) {
        this.playerName = playerName;
        this.playerAbility = ability;

        this.enemy = new Enemy(0, 0, 0, 0, null, 0, null, null, null, null);
        this.boss = new Boss(0, 0, 0, 0, null, 0, null, null, null, null);

        playerIdleTexture = TextureLoader.loadTexture("character1.png");
        if (playerIdleTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a character1.png textúrát.");
        }

        walkFrames = new ArrayList<>();
        Texture walk1 = TextureLoader.loadTexture("player_walk1.png");
        Texture walk2 = TextureLoader.loadTexture("player_walk2.png");
        Texture walk3 = TextureLoader.loadTexture("player_walk3.png");
        if (walk1 != null) walkFrames.add(walk1);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk1.png textúrát.");
        if (walk2 != null) walkFrames.add(walk2);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk2.png textúrát.");
        if (walk3 != null) walkFrames.add(walk3);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk3.png textúrát.");
        Sprite walkSprite = walkFrames.isEmpty() ? null : new Sprite(walkFrames, 0.1f, true);

        activeWalkFrames = new ArrayList<>();
        String abilityTextureName;
        switch (ability) {
            case SPEED:
                abilityTextureName = "Speed.png";
                break;
            case DODGE:
                abilityTextureName = "Dodge.png";
                break;
            case BLOCK:
                abilityTextureName = "Block.png";
                break;
            default:
                abilityTextureName = null;
        }
        if (abilityTextureName != null) {
            Texture abilityTexture = TextureLoader.loadTexture(abilityTextureName);
            if (abilityTexture != null) {
                activeWalkFrames.add(abilityTexture);
            } else {
                System.err.println("HIBA: Nem sikerült betölteni a " + abilityTextureName + " textúrát.");
            }
        }
        Sprite activeWalkSprite = activeWalkFrames.isEmpty() ? null : new Sprite(activeWalkFrames, 0.1f, false);

        tileTextures = new HashMap<>();
        tileTextures.put(Tile.TileType.FLOOR, TextureLoader.loadTexture("grass.png"));
        if (tileTextures.get(Tile.TileType.FLOOR) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a grass.png textúrát.");
        }
        tileTextures.put(Tile.TileType.WALL, TextureLoader.loadTexture("wall_tile.png"));
        if (tileTextures.get(Tile.TileType.WALL) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a wall_tile.png textúrát.");
        }
        tileTextures.put(Tile.TileType.GATE, TextureLoader.loadTexture("gate_texture.png"));
        if (tileTextures.get(Tile.TileType.GATE) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_texture.png textúrát.");
        }

        weaponCrateTexture = TextureLoader.loadTexture("weapon_crate_full.png");
        if (weaponCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_full.png textúrát.");
        }
        openCrateTexture = TextureLoader.loadTexture("weapon_crate_open.png");
        if (openCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_open.png textúrát.");
        }
        emptyCrateTexture = TextureLoader.loadTexture("weapon_crate_empty.png");
        if (emptyCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_empty.png textúrát.");
        }
        tileTextures.put(Tile.TileType.WEAPON_CRATE, weaponCrateTexture);

        Texture shopFloorTexture = TextureLoader.loadTexture("SHOP_FLOOR.png");
        if (shopFloorTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a SHOP_FLOOR.png textúrát.");
        }
        tileTextures.put(Tile.TileType.SHOP_FLOOR, shopFloorTexture);

        Texture boxTexture = TextureLoader.loadTexture("box.png");
        if (boxTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box.png textúrát.");
        }
        tileTextures.put(Tile.TileType.BOX, boxTexture);

        boxDamageTextures = new HashMap<>();
        boxDamageTextures.put(1, TextureLoader.loadTexture("box_cracked1.png"));
        if (boxDamageTextures.get(1) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box_cracked1.png textúrát.");
        }
        boxDamageTextures.put(2, TextureLoader.loadTexture("box_cracked2.png"));
        if (boxDamageTextures.get(2) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box_cracked2.png textúrát.");
        }

        gateAnimationTextures = new HashMap<>();
        gateAnimationTextures.put(0, TextureLoader.loadTexture("gate_anim_01.png"));
        if (gateAnimationTextures.get(0) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_anim_01.png textúrát.");
        }
        gateAnimationTextures.put(1, TextureLoader.loadTexture("gate_anim_02.png"));
        if (gateAnimationTextures.get(1) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_anim_02.png textúrát.");
        }

        enemyTexture = TextureLoader.loadTexture("enemy1.png");
        if (enemyTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a enemy1.png textúrát.");
        }

        effectTextures = new HashMap<>();
        effectTextures.put(Effect.EffectType.SPEED_BOOST, TextureLoader.loadTexture("speed_boost.png"));
        if (effectTextures.get(Effect.EffectType.SPEED_BOOST) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a speed_boost.png textúrát.");
        }
        effectTextures.put(Effect.EffectType.DAMAGE_BOOST, TextureLoader.loadTexture("damage_boost.png"));
        if (effectTextures.get(Effect.EffectType.DAMAGE_BOOST) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a damage_boost.png textúrát.");
        }
        effectTextures.put(Effect.EffectType.HEALTH_REGEN, TextureLoader.loadTexture("health_regen.png"));
        if (effectTextures.get(Effect.EffectType.HEALTH_REGEN) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a health_regen.png textúrát.");
        }

        teleportPadTexture = TextureLoader.loadTexture("teleport_pad.png");
        if (teleportPadTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a teleport_pad.png textúrát.");
        }
        tileTextures.put(Tile.TileType.TELEPORT_PAD, teleportPadTexture);

        fontTexture = TextureLoader.loadTexture("font.png");
        if (fontTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a font.png textúrát.");
        }
        textRenderer = new TextRenderer("CRIT", new java.awt.Font("Arial", java.awt.Font.BOLD, 48), java.awt.Color.WHITE);

        currentDungeon = DungeonGenerator.generateRandomDungeon(
                32,
                tileTextures,
                enemyTexture,
                boxDamageTextures,
                gateAnimationTextures,
                weaponCrateTexture,
                openCrateTexture,
                emptyCrateTexture,
                weaponFactory,
                textRenderer,
                effectTextures,
                teleportPadTexture,
                rand
        );

        player = new Player(
                currentDungeon.getPlayerSpawnX(),
                currentDungeon.getPlayerSpawnY(),
                50, 50, playerIdleTexture, window, weaponFactory, tileTextures.get(Tile.TileType.FLOOR),
                textRenderer
        );

        for (Enemy enemy : currentDungeon.getEnemies()) {
            enemy.setTargetPlayer(this.player);
            enemy.setShowPathDebug(this.showPathDebug);
        }

        player.setDungeon(currentDungeon);
        player.setSprites(walkSprite);
        player.setActiveSprites(activeWalkSprite);
        player.setWeapon(weaponFactory.createWeapon("pistol"), "pistol");
        player.setAbility(ability);
        player.setName(playerName);

        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels);

        mapRenderer = new MapRenderer();
        collisionManager = new CollisionManager(currentDungeon);

        projectiles = new ArrayList<>();
        effects = new ArrayList<>();
        playerEffects = new ArrayList<>();

        currentState = GameState.GAMEPLAY;
        glfwSetCharCallback(window, null);
        glfwSetKeyCallback(window, inputHandler.getKeyCallback());
        glfwSetMouseButtonCallback(window, inputHandler.getMouseButtonCallback());

        glEnable(GL_TEXTURE_2D);

        for (Enemy enemy : currentDungeon.getEnemies()) {
            if (enemy instanceof Boss) {
                boss = enemy.clone();
                for (Enemy normalEnemy : currentDungeon.getEnemies()) {
                    if (!(normalEnemy instanceof Boss)) {
                        this.enemy = normalEnemy.clone();
                        break;
                    }
                }
            }
        }

        upgradeScreen.setPlayer(player);
        upgradeScreen.setDungeon(enemy, boss);
    }

    // Multiplayer inicializáció
    public void initMultiplayer(String serverIp, String serverPort, boolean isHost) {
        this.isMultiplayer = true;
        this.isHost = isHost;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        try {
            multiplayerClient = new MultiplayerClient(serverIp, Integer.parseInt(serverPort));
            multiplayerClient.connect();
        } catch (Exception e) {
            System.err.println("❌ Multiplayer connection failed: " + e.getMessage());
            e.printStackTrace();
            currentState = GameState.LOBBY;
        }
    }

    // Multiplayer játék indítása
    public void startMultiplayerGame(String playerName, String ability, String serverIp, String serverPort, boolean isHost) {
        System.out.println("🎮 Starting multiplayer game...");

        // Először kapcsolódjunk
        initMultiplayer(serverIp, serverPort, isHost);

        // Majd inicializáljuk a játékot multiplayer módban
        Player.Ability playerAbility = Player.Ability.valueOf(ability);
        initGameplayMultiplayer(playerName, playerAbility);
    }

    // Multiplayer gameplay inicializáció
    private void initGameplayMultiplayer(String playerName, Player.Ability ability) {
        // Inicializáld a játékot single player módban
        initGameplay(playerName, ability);

        // Küldjük el a join üzenetet
        String joinData = player.getName() + ":" +
                player.getAbility().name() + ":" +
                showPathDebug;
        multiplayerClient.sendJoinGame(joinData);

        System.out.println("✅ Multiplayer gameplay initialized");
    }

    private void loadNextLevel() {
        cleanupForNextLevel();
        projectiles.clear();
        effects.clear();
        playerEffects.clear();

        weaponFactory.loadWeaponSprites();

        playerIdleTexture = TextureLoader.loadTexture("character1.png");
        if (playerIdleTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a character1.png textúrát.");
        }

        walkFrames = new ArrayList<>();
        Texture walk1 = TextureLoader.loadTexture("player_walk1.png");
        Texture walk2 = TextureLoader.loadTexture("player_walk2.png");
        Texture walk3 = TextureLoader.loadTexture("player_walk3.png");
        if (walk1 != null) walkFrames.add(walk1);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk1.png textúrát.");
        if (walk2 != null) walkFrames.add(walk2);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk2.png textúrát.");
        if (walk3 != null) walkFrames.add(walk3);
        else System.err.println("HIBA: Nem sikerült betölteni a player_walk3.png textúrát.");
        Sprite walkSprite = walkFrames.isEmpty() ? null : new Sprite(walkFrames, 0.1f, true);

        hud = new HUD(width, height);

        activeWalkFrames = new ArrayList<>();
        String abilityTextureName;
        switch (playerAbility) {
            case SPEED:
                abilityTextureName = "Speed.png";
                break;
            case DODGE:
                abilityTextureName = "Dodge.png";
                break;
            case BLOCK:
                abilityTextureName = "Block.png";
                break;
            default:
                abilityTextureName = null;
        }
        if (abilityTextureName != null) {
            Texture abilityTexture = TextureLoader.loadTexture(abilityTextureName);
            if (abilityTexture != null) {
                activeWalkFrames.add(abilityTexture);
            } else {
                System.err.println("HIBA: Nem sikerült betölteni a " + abilityTextureName + " textúrát.");
            }
        }
        Sprite activeWalkSprite = activeWalkFrames.isEmpty() ? null : new Sprite(activeWalkFrames, 0.1f, false);

        tileTextures = new HashMap<>();
        tileTextures.put(Tile.TileType.FLOOR, TextureLoader.loadTexture("grass.png"));
        if (tileTextures.get(Tile.TileType.FLOOR) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a grass.png textúrát.");
        }
        tileTextures.put(Tile.TileType.WALL, TextureLoader.loadTexture("wall_tile.png"));
        if (tileTextures.get(Tile.TileType.WALL) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a wall_tile.png textúrát.");
        }
        tileTextures.put(Tile.TileType.GATE, TextureLoader.loadTexture("gate_texture.png"));
        if (tileTextures.get(Tile.TileType.GATE) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_texture.png textúrát.");
        }

        weaponCrateTexture = TextureLoader.loadTexture("weapon_crate_full.png");
        if (weaponCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_full.png textúrát.");
        }
        openCrateTexture = TextureLoader.loadTexture("weapon_crate_open.png");
        if (openCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_open.png textúrát.");
        }
        emptyCrateTexture = TextureLoader.loadTexture("weapon_crate_empty.png");
        if (emptyCrateTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a weapon_crate_empty.png textúrát.");
        }
        tileTextures.put(Tile.TileType.WEAPON_CRATE, weaponCrateTexture);

        Texture shopFloorTexture = TextureLoader.loadTexture("SHOP_FLOOR.png");
        if (shopFloorTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a SHOP_FLOOR.png textúrát.");
        }
        tileTextures.put(Tile.TileType.SHOP_FLOOR, shopFloorTexture);

        Texture boxTexture = TextureLoader.loadTexture("box.png");
        if (boxTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box.png textúrát.");
        }
        tileTextures.put(Tile.TileType.BOX, boxTexture);

        boxDamageTextures = new HashMap<>();
        boxDamageTextures.put(1, TextureLoader.loadTexture("box_cracked1.png"));
        if (boxDamageTextures.get(1) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box_cracked1.png textúrát.");
        }
        boxDamageTextures.put(2, TextureLoader.loadTexture("box_cracked2.png"));
        if (boxDamageTextures.get(2) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a box_cracked2.png textúrát.");
        }

        gateAnimationTextures = new HashMap<>();
        gateAnimationTextures.put(0, TextureLoader.loadTexture("gate_anim_01.png"));
        if (gateAnimationTextures.get(0) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_anim_01.png textúrát.");
        }
        gateAnimationTextures.put(1, TextureLoader.loadTexture("gate_anim_02.png"));
        if (gateAnimationTextures.get(1) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a gate_anim_02.png textúrát.");
        }

        enemyTexture = TextureLoader.loadTexture("enemy1.png");
        if (enemyTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a enemy1.png textúrát.");
        }

        effectTextures = new HashMap<>();
        effectTextures.put(Effect.EffectType.SPEED_BOOST, TextureLoader.loadTexture("speed_boost.png"));
        if (effectTextures.get(Effect.EffectType.SPEED_BOOST) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a speed_boost.png textúrát.");
        }
        effectTextures.put(Effect.EffectType.DAMAGE_BOOST, TextureLoader.loadTexture("damage_boost.png"));
        if (effectTextures.get(Effect.EffectType.DAMAGE_BOOST) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a damage_boost.png textúrát.");
        }
        effectTextures.put(Effect.EffectType.HEALTH_REGEN, TextureLoader.loadTexture("health_regen.png"));
        if (effectTextures.get(Effect.EffectType.HEALTH_REGEN) == null) {
            System.err.println("HIBA: Nem sikerült betölteni a health_regen.png textúrát.");
        }

        teleportPadTexture = TextureLoader.loadTexture("teleport_pad.png");
        if (teleportPadTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a teleport_pad.png textúrát.");
        }
        tileTextures.put(Tile.TileType.TELEPORT_PAD, teleportPadTexture);

        fontTexture = TextureLoader.loadTexture("font.png");
        if (fontTexture == null) {
            System.err.println("HIBA: Nem sikerült betölteni a font.png textúrát.");
        }
        textRenderer = new TextRenderer("CRIT", new java.awt.Font("Arial", java.awt.Font.BOLD, 48), java.awt.Color.WHITE);

        currentDungeon = DungeonGenerator.generateRandomDungeon(
                32,
                tileTextures,
                enemyTexture,
                boxDamageTextures,
                gateAnimationTextures,
                weaponCrateTexture,
                openCrateTexture,
                emptyCrateTexture,
                weaponFactory,
                textRenderer,
                effectTextures,
                teleportPadTexture,
                rand
        );

        float spawnX = currentDungeon.getPlayerSpawnX();
        float spawnY = currentDungeon.getPlayerSpawnY();
        player.setDungeon(currentDungeon);
        player.setSprites(walkSprite);
        player.setActiveSprites(activeWalkSprite);
        player.setWeapon(weaponFactory.createWeapon("pistol"), "pistol");
        player.setAbility(this.playerAbility);
        player.setName(this.playerName);
        player.setX(spawnX);
        player.setY(spawnY);

        for (Enemy enemy : currentDungeon.getEnemies()) {
            enemy.setTargetPlayer(this.player);
            enemy.setShowPathDebug(this.showPathDebug);
        }

        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels);

        collisionManager = new CollisionManager(currentDungeon);
        mapRenderer = new MapRenderer();

        currentState = GameState.GAMEPLAY;
        bossDefeated = false;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);

        glfwSetKeyCallback(window, inputHandler.getKeyCallback());
        glfwSetMouseButtonCallback(window, inputHandler.getMouseButtonCallback());

        for (Enemy enemy : currentDungeon.getEnemies()) {
            if (enemy instanceof Boss) {
                boss = enemy;
                for (Enemy normalEnemy : currentDungeon.getEnemies()) {
                    if (!(normalEnemy instanceof Boss)) {
                        this.enemy = normalEnemy;
                        break;
                    }
                }
            }
        }

        upgradeScreen.reset();
        upgradeScreen.setPlayer(player);
        upgradeScreen.setDungeon(enemy, boss);
    }

    public void startGameFromSave(int saveId) {
        this.saveIdToLoad = saveId;
        this.currentState = GameState.LOAD_GAME;
    }

    private int getSelectedSaveIdFromSomewhere() {
        return this.saveIdToLoad;
    }

    private void loadSavedGame(int saveId) {
        try {
            System.out.println("Mentett játék betöltése: " + saveId);

            Enemy savedEnemy = GameSaveHandler.loadEnemy(saveId);
            Boss savedBoss = GameSaveHandler.loadBoss(saveId);

            if (savedEnemy != null && currentDungeon != null) {
                for (Enemy enemy : currentDungeon.getEnemies()) {
                    if (!(enemy instanceof Boss)) {
                        enemy.setHealth(savedEnemy.getHealth());
                        enemy.setDamage(savedEnemy.getAttackDamage());
                        enemy.setMoveSpeed(savedEnemy.getMoveSpeed());
                        System.out.println("Enemy adatok frissítve - HP: " + savedEnemy.getHealth());
                        break;
                    }
                }
            }

            if (savedBoss != null && currentDungeon != null) {
                for (Enemy enemy : currentDungeon.getEnemies()) {
                    if (enemy instanceof Boss) {
                        enemy.setHealth(savedBoss.getHealth());
                        enemy.setDamage(savedBoss.getAttackDamage());
                        enemy.setMoveSpeed(savedBoss.getMoveSpeed());
                        System.out.println("Boss adatok frissítve - HP: " + savedBoss.getHealth());
                        break;
                    }
                }
            }

            System.out.println("✅ Mentett játék sikeresen betöltve!");
        } catch (Exception e) {
            System.err.println("❌ Hiba a mentett játék betöltése közben: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initGameplayFromSave(int saveId) {
        try {
            System.out.println("🔄 Mentett játék inicializálása: " + saveId);

            Player savedPlayer = GameSaveHandler.loadPlayer(saveId);
            if (savedPlayer == null) {
                System.err.println("Hiba: Nem sikerült betölteni a mentett playert");
                currentState = GameState.LOBBY;
                return;
            }

            System.out.println("DEBUG - Betöltött player: " +
                    "HP=" + savedPlayer.getHealth() +
                    ", MaxHP=" + savedPlayer.getMaxHealth() +
                    ", Name=" + savedPlayer.getName());

            initGameplay(savedPlayer.getName(), savedPlayer.getAbility());

            System.out.println("DEBUG - InitGameplay után: " +
                    "HP=" + player.getHealth() +
                    ", MaxHP=" + player.getMaxHealth());

            player.setName(savedPlayer.getName());
            player.setAbility(savedPlayer.getAbility());
            player.setMaxHealth(savedPlayer.getMaxHealth());
            player.setHealth(savedPlayer.getHealth());
            player.setDamageBoost(savedPlayer.getDamageBoost());
            player.setCritChanceBoost(savedPlayer.getCritChanceBoost());

            System.out.println("DEBUG - Felülírás után: " +
                    "HP=" + player.getHealth() +
                    ", MaxHP=" + player.getMaxHealth());

            System.out.println("✅ Player statok felülírva!");

            loadSavedGame(saveId);
        } catch (Exception e) {
            System.err.println("❌ Hiba a mentett játék inicializálása közben: " + e.getMessage());
            e.printStackTrace();
            currentState = GameState.LOBBY;
        }
    }

    private void update(float deltaTime, double currentTime) {
        if (isMultiplayer && multiplayerClient != null) {
            updateMultiplayer(deltaTime);
            processServerMessages();
        }

        if (currentState == GameState.ABILITY_SELECTION) {
            abilitySelectionScreen.update();
            if (abilitySelectionScreen.isSelectionComplete()) {
                if (isMultiplayer) {
                    initGameplayMultiplayer(
                            abilitySelectionScreen.getPlayerName(),
                            abilitySelectionScreen.getSelectedAbility()
                    );
                } else {
                    initGameplay(
                            abilitySelectionScreen.getPlayerName(),
                            abilitySelectionScreen.getSelectedAbility()
                    );
                }
            }
        } else if (currentState == GameState.GAMEPLAY) {
            if (isMultiplayer) {
                updateGameplayMultiplayer(deltaTime, currentTime);
            } else {
                updateGameplaySingleplayer(deltaTime, currentTime);
            }
        } else if (currentState == GameState.UPGRADE_CHOICE) {
            upgradeScreen.update(inputHandler);
            handleUpgradeChoice();
        } else if (currentState == GameState.GAME_OVER) {
            handleGameOver();
        } else if (currentState == GameState.LOAD_GAME) {
            if (saveIdToLoad != -1) {
                initGameplayFromSave(saveIdToLoad);
                loadSavedGame(saveIdToLoad);
                saveIdToLoad = -1;
                currentState = GameState.GAMEPLAY;
            } else {
                currentState = GameState.LOBBY;
            }
        }
    }

    private void updateGameplayMultiplayer(float deltaTime, double currentTime) {
        if (!player.isAlive()) {
            currentState = GameState.GAME_OVER;
            return;
        }

        // Javítva: updateInput helyett update használata
        player.update(deltaTime, inputHandler, collisionManager, currentTime);
        sendPlayerInputToServer();

        interpolateOtherPlayers(deltaTime);

        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels);

        // Az updateEnemiesFromServer hívás javítása később történik a processServerMessages-ben
    }

    private void updateGameplaySingleplayer(float deltaTime, double currentTime) {
        if (!player.isAlive()) {
            currentState = GameState.GAME_OVER;
            return;
        }

        player.update(deltaTime, inputHandler, collisionManager, currentTime);

        int dungeonWidthPixels = currentDungeon.getWidthTiles() * currentDungeon.getTileSize();
        int dungeonHeightPixels = currentDungeon.getHeightTiles() * currentDungeon.getTileSize();
        camera.follow(player, dungeonWidthPixels, dungeonHeightPixels);

        if (!bossDefeated) {
            boolean anyBossAlive = false;
            for (Enemy enemy : currentDungeon.getEnemies()) {
                if (enemy instanceof Boss && enemy.isAlive()) {
                    anyBossAlive = true;
                    break;
                }
            }
            if (!anyBossAlive) {
                bossDefeated = true;
                int bossGridX = currentDungeon.getBossRoomGridX();
                int bossGridY = currentDungeon.getBossRoomGridY();
                int bossWidth = currentDungeon.getBossRoomWidth();
                int bossHeight = currentDungeon.getBossRoomHeight();

                int padX = bossGridX + bossWidth / 2;
                int padY = bossGridY + bossHeight / 2;

                Tile padTile = currentDungeon.getTile(padX, padY);
                if (padTile != null) {
                    padTile.setType(Tile.TileType.TELEPORT_PAD);
                    padTile.setTexture(teleportPadTexture);
                    padTile.setIsCollidable(false);
                }
            }
        }

        for (Enemy enemy : currentDungeon.getEnemies()) {
            enemy.update(deltaTime);
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update(deltaTime);

            boolean hitSomething = false;
            int tileSize = currentDungeon.getTileSize();

            int projGridX = (int) (projectile.getX() / tileSize);
            int projGridY = (int) (projectile.getY() / tileSize);

            int minCheckX = Math.max(0, projGridX - 1);
            int maxCheckX = Math.min(currentDungeon.getWidthTiles() - 1, projGridX + 1);
            int minCheckY = Math.max(0, projGridY - 1);
            int maxCheckY = Math.min(currentDungeon.getHeightTiles() - 1, projGridY + 1);

            for (int x = minCheckX; x <= maxCheckX; x++) {
                for (int y = minCheckY; y <= maxCheckY; y++) {
                    Tile tile = currentDungeon.getTiles()[x][y];
                    if (tile != null && tile.isSolid()) {
                        if (collisionManager.checkTileCollision(projectile, tile)) {
                            projectile.setAlive(false);
                            if (tile.getType() == Tile.TileType.BOX) {
                                tile.takeDamage(1);
                                if (tile.isDestroyed()) {
                                    tile.setType(Tile.TileType.FLOOR);
                                    tile.setTexture(tileTextures.get(Tile.TileType.FLOOR));
                                    createRandomEffect(tile.getX(), tile.getY());
                                } else {
                                    tile.updateTextureByHealth();
                                }
                            }
                            hitSomething = true;
                            break;
                        }
                    }
                }
                if (hitSomething) break;
            }

            for (Enemy enemy : currentDungeon.getEnemies()) {
                if (enemy.isAlive() && collisionManager.checkCollision(projectile, enemy)) {
                    float finalDamage = player.calculateFinalDamage(projectile.getDamage(), enemy);
                    enemy.takeDamage(finalDamage);
                    projectile.setAlive(false);
                    hitSomething = true;
                    break;
                }
            }

            if (!projectile.isAlive()) {
                projectileIterator.remove();
            }
        }

        if (bossDefeated && currentState == GameState.GAMEPLAY) {
            int playerGridX = (int) ((player.getX() + player.getWidth() / 2) / currentDungeon.getTileSize());
            int playerGridY = (int) ((player.getY() + player.getHeight() / 2) / currentDungeon.getTileSize());
            if (playerGridX >= 0 && playerGridX < currentDungeon.getWidthTiles() &&
                    playerGridY >= 0 && playerGridY < currentDungeon.getHeightTiles()) {
                Tile playerTile = currentDungeon.getTiles()[playerGridX][playerGridY];
                if (playerTile != null && playerTile.getType() == Tile.TileType.TELEPORT_PAD) {
                    currentState = GameState.UPGRADE_CHOICE;
                }
            }
        }

        checkEffectCollision();
        updatePlayerEffects(deltaTime);
        effects.removeIf(effect -> effect.isCollected);
        currentDungeon.getEnemies().removeIf(enemy -> !enemy.isAlive());
    }

    private void updateMultiplayer(float deltaTime) {
        if (!isMultiplayer || multiplayerClient == null) return;

        sendPlayerInputToServer();
        interpolateOtherPlayers(deltaTime);
    }

    private void sendPlayerInputToServer() {
        if (!isMultiplayer || multiplayerClient == null) return;

        String inputData = inputHandler.getMovementX() + "," +
                inputHandler.getMovementY() + "," +
                (inputHandler.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) ? "1" : "0") + "," +
                getCursorX() + "," + getCursorY();

        multiplayerClient.sendPlayerInput(inputData);
        multiplayerClient.sendPlayerPosition(player.getX(), player.getY());
    }

    private void processServerMessages() {
        if (!isMultiplayer || multiplayerClient == null) return;

        List<String> messages = multiplayerClient.getReceivedMessages();
        for (String message : messages) {
            handleServerMessage(message);
        }
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        System.out.println("🎯 Processing server message: " + command + " | " + data);
        switch (command) {
            case "PLAYER_ID":
                myPlayerId = Integer.parseInt(data);
                multiplayerClient.setPlayerId(myPlayerId);
                System.out.println("🎮 Assigned player ID: " + myPlayerId);
                break;
            case "DUNGEON_SEED":
                long seed = Long.parseLong(data);
                Random rand = new Random(seed);
                currentDungeon = DungeonGenerator.generateRandomDungeon(
                        53, tileTextures, enemyTexture, boxDamageTextures, gateAnimationTextures,
                        weaponCrateTexture, openCrateTexture, emptyCrateTexture,
                        weaponFactory, textRenderer, effectTextures, teleportPadTexture, rand
                );
                break;
            case "DUNGEON_DATA":
                String[] dungeonParts = data.split(";");
                for (String part : dungeonParts) {
                    if (part.startsWith("player_spawn:")) {
                        String[] spawnParts = part.substring(13).split(",");
                        player.setX(Float.parseFloat(spawnParts[0]));
                        player.setY(Float.parseFloat(spawnParts[1]));
                    }
                }
                break;
            case "GAME_STATE":
                updateFromGameState(data);
                break;
            case "FULL_GAME_STATE":
                updateFromFullGameState(data);
                break;
            case "PLAYER_JOINED":
                handlePlayerJoined(data);
                break;
            case "PLAYER_DISCONNECTED":
                handlePlayerDisconnected(data);
                break;
            case "PLAYER_POSITION":
                handlePlayerPositionUpdate(data);
                break;
            case "PLAYER_ACTION":
                handlePlayerAction(data);
                break;
            case "ENEMY_UPDATE":
                handleEnemyUpdate(data);
                break;
            case "PROJECTILE_UPDATE":
                handleProjectileUpdate(data);
                break;
            case "GAME_STARTING":
                System.out.println("🚀 Game starting on server!");
                currentState = GameState.GAMEPLAY;
                break;
        }
    }

    private void updateFromGameState(String gameStateData) {
        if (!isMultiplayer) return;

        try {
            String[] sections = gameStateData.split(";");
            for (String section : sections) {
                if (section.startsWith("PLAYERS:")) {
                    updatePlayersFromServer(section.substring(8));
                } else if (section.startsWith("ENEMIES:")) {
                    updateEnemiesFromServer(section.substring(8));
                } else if (section.startsWith("PROJECTILES:")) {
                    updateProjectilesFromServer(section.substring(12));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating from game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFromFullGameState(String fullGameState) {
        System.out.println("🔄 Updating from full game state");
        updateFromGameState(fullGameState);
    }

    private void updatePlayersFromServer(String playersData) {
        String[] playerEntries = playersData.split("\\|");
        for (String entry : playerEntries) {
            if (!entry.isEmpty()) {
                PlayerState playerState = PlayerState.deserialize(entry);
                if (playerState != null) {
                    serverPlayerStates.put(playerState.getPlayerId(), playerState);
                    if (playerState.getPlayerId() != myPlayerId) {
                        updateOtherPlayer(playerState);
                    } else {
                        syncOwnPlayer(playerState);
                    }
                }
            }
        }
    }

    private void updateOtherPlayer(PlayerState playerState) {
        if (!otherPlayers.containsKey(playerState.getPlayerId())) {
            Player otherPlayer = createOtherPlayer(playerState);
            otherPlayers.put(playerState.getPlayerId(), otherPlayer);
            System.out.println("👥 New player created: " + playerState.getPlayerName());
        } else {
            Player otherPlayer = otherPlayers.get(playerState.getPlayerId());
            System.out.println("DEBUG: otherPlayer class: " + otherPlayer.getClass().getName());
            otherPlayer.setTargetX(playerState.getX());
            otherPlayer.setTargetY(playerState.getY());
            otherPlayer.setHealth(playerState.getHealth());
            otherPlayer.setAlive(playerState.isAlive());
        }
    }

    private void syncOwnPlayer(PlayerState serverState) {
        player.setHealth(serverState.getHealth());
        player.setAlive(serverState.isAlive());
    }

    private void interpolateOtherPlayers(float deltaTime) {
        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.hasTargetPosition()) {
                float newX = interpolate(otherPlayer.getX(), otherPlayer.getTargetX(), interpolationSpeed * deltaTime);
                float newY = interpolate(otherPlayer.getY(), otherPlayer.getTargetY(), interpolationSpeed * deltaTime);
                otherPlayer.setX(newX);
                otherPlayer.setY(newY);
            }
        }
    }

    private float interpolate(float current, float target, float factor) {
        return current + (target - current) * Math.min(factor, 1.0f);
    }

    private Player createOtherPlayer(PlayerState playerState) {
        Player otherPlayer = new Player(
                playerState.getX(), playerState.getY(),
                50, 50, playerIdleTexture, window, weaponFactory,
                tileTextures.get(Tile.TileType.FLOOR), textRenderer
        );
        otherPlayer.setName(playerState.getPlayerName());
        otherPlayer.setTargetX(playerState.getX());
        otherPlayer.setTargetY(playerState.getY());

        try {
            Player.Ability ability = Player.Ability.valueOf(playerState.getAbility());
            otherPlayer.setAbility(ability);
        } catch (Exception e) {
            otherPlayer.setAbility(Player.Ability.SPEED);
        }

        return otherPlayer;
    }

    private void handlePlayerJoined(String data) {
        String[] parts = data.split(":");
        if (parts.length >= 3) {
            int playerId = Integer.parseInt(parts[0]);
            String playerName = parts[1];
            String ability = parts[2];

            System.out.println("🎯 Player joined: " + playerName + " (ID: " + playerId + ")");

            if (playerId != myPlayerId) {
                PlayerState playerState = new PlayerState(playerId, playerName, 100, 100, 100, 100);
                playerState.setAbility(ability);
                updateOtherPlayer(playerState);
            }
        }
    }

    private void handlePlayerDisconnected(String data) {
        int playerId = Integer.parseInt(data);
        System.out.println("🔌 Player disconnected: " + playerId);

        if (otherPlayers.containsKey(playerId)) {
            otherPlayers.remove(playerId);
        }
    }

    private void handlePlayerPositionUpdate(String data) {
        String[] parts = data.split(":");
        if (parts.length >= 4) { // Támogatjuk az isAlive adatot
            int playerId = Integer.parseInt(parts[0]);
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            boolean isAlive = Boolean.parseBoolean(parts[3]);

            if (playerId != myPlayerId && otherPlayers.containsKey(playerId)) {
                Player otherPlayer = otherPlayers.get(playerId);
                System.out.println("DEBUG: otherPlayer class: " + otherPlayer.getClass().getName());
                otherPlayer.setTargetX(x);
                otherPlayer.setTargetY(y);
                otherPlayer.setAlive(isAlive);
                System.out.println("📍 Player position updated: ID=" + playerId + ", x=" + x + ", y=" + y + ", alive=" + isAlive);
            } else if (playerId == myPlayerId) {
                System.out.println("DEBUG: player class: " + player.getClass().getName());
                player.setTargetX(x);
                player.setTargetY(y);
                player.setAlive(isAlive);
                System.out.println("📍 Own player position updated: ID=" + playerId + ", x=" + x + ", y=" + y + ", alive=" + isAlive);
            }
        }
    }

    private void handlePlayerAction(String data) {
        String[] parts = data.split(":");
        if (parts.length >= 4) {
            int playerId = Integer.parseInt(parts[0]);
            String action = parts[1];
            float x = Float.parseFloat(parts[2]);
            float y = Float.parseFloat(parts[3]);

            if (playerId != myPlayerId && otherPlayers.containsKey(playerId)) {
                Player otherPlayer = otherPlayers.get(playerId);
                switch (action) {
                    case "SHOOT":
                        WeaponInterface weapon = otherPlayer.getCurrentWeapon();
                        if (weapon instanceof Weapon) {
                            // Számoljuk ki az irányvektort a célpont alapján
                            float dx = x - otherPlayer.getX();
                            float dy = y - otherPlayer.getY();
                            float magnitude = (float) Math.sqrt(dx * dx + dy * dy);
                            float dirX = magnitude > 0 ? dx / magnitude : 0;
                            float dirY = magnitude > 0 ? dy / magnitude : 0;
                            float speed = 300.0f; // Alapértelmezett sebesség
                            float damage = weapon.getBaseDamage(); // Fegyver sebzése
                            Projectile projectile = new Projectile(
                                    otherPlayer.getX(),
                                    otherPlayer.getY(),
                                    10, // Példa szélesség
                                    10, // Példa magasság
                                    damage,
                                    speed,
                                    dirX,
                                    dirY,
                                    otherPlayer
                            );
                            projectile.setId(projectiles.size() + 1); // Egyszerű ID kiosztás
                            projectiles.add(projectile);
                            System.out.println("🔫 Player " + playerId + " shot a projectile");
                        }
                        break;
                    case "MELEE":
                        if (otherPlayer.getCurrentWeapon() instanceof MeleeWeapon) {
                            handleMeleeAttack(otherPlayer, (MeleeWeapon) otherPlayer.getCurrentWeapon(), (float) glfwGetTime());
                            System.out.println("⚔️ Player " + playerId + " performed melee attack");
                        }
                        break;
                }
            }
        }
    }

    private void handleEnemyUpdate(String data) {
        String[] parts = data.split(":");
        if (parts.length >= 5) {
            int enemyId = Integer.parseInt(parts[0]);
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float health = Float.parseFloat(parts[3]);
            boolean isAlive = Boolean.parseBoolean(parts[4]);

            for (Enemy enemy : currentDungeon.getEnemies()) {
                if (enemy.getId() == enemyId) {
                    enemy.setX(x);
                    enemy.setY(y);
                    enemy.setHealth(health);
                    enemy.setAlive(isAlive);
                    System.out.println("👹 Enemy updated: ID=" + enemyId + ", x=" + x + ", y=" + y + ", health=" + health);
                    break;
                }
            }
        }
    }

    private void updateEnemiesFromServer(String enemiesData) {
        String[] enemyEntries = enemiesData.split("\\|");
        for (String entry : enemyEntries) {
            if (!entry.isEmpty()) {
                String[] parts = entry.split(":");
                if (parts.length >= 5) {
                    int enemyId = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float health = Float.parseFloat(parts[3]);
                    boolean isAlive = Boolean.parseBoolean(parts[4]);

                    for (Enemy enemy : currentDungeon.getEnemies()) {
                        if (enemy.getId() == enemyId) {
                            enemy.setX(x);
                            enemy.setY(y);
                            enemy.setHealth(health);
                            enemy.setAlive(isAlive);
                            System.out.println("👹 Enemy synced from server: ID=" + enemyId + ", x=" + x + ", y=" + y);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateProjectilesFromServer(String projectilesData) {
        String[] projectileEntries = projectilesData.split("\\|");
        projectiles.clear(); // Szinkronizáljuk a lövedékeket, töröljük a helyi listát

        for (String entry : projectileEntries) {
            if (!entry.isEmpty()) {
                String[] parts = entry.split(":");
                if (parts.length >= 7) {
                    int projectileId = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float velocityX = Float.parseFloat(parts[3]);
                    float velocityY = Float.parseFloat(parts[4]);
                    int ownerPlayerId = Integer.parseInt(parts[5]);
                    float damage = Float.parseFloat(parts[6]);

                    // Keresük meg a tulajdonos játékost
                    Player owner = otherPlayers.getOrDefault(ownerPlayerId, player);
                    float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                    float dirX = speed > 0 ? velocityX / speed : 0;
                    float dirY = speed > 0 ? velocityY / speed : 0;

                    Projectile projectile = new Projectile(
                            x, y, 10, 10, // Példa méretek
                            damage, speed, dirX, dirY, owner
                    );
                    projectile.setId(projectileId);
                    projectiles.add(projectile);
                    System.out.println("💥 Projectile synced from server: ID=" + projectileId + ", x=" + x + ", y=" + y);
                }
            }
        }
    }

    private void handleProjectileUpdate(String data) {
        String[] parts = data.split(":");
        if (parts.length >= 7) {
            int projectileId = Integer.parseInt(parts[0]);
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float velocityX = Float.parseFloat(parts[3]);
            float velocityY = Float.parseFloat(parts[4]);
            int ownerPlayerId = Integer.parseInt(parts[5]);
            float damage = Float.parseFloat(parts[6]);

            for (Projectile projectile : projectiles) {
                if (projectile.getId() == projectileId) {
                    projectile.setX(x);
                    projectile.setY(y);
                    projectile.setAlive(true); // Szerver szerint aktív
                    System.out.println("💥 Projectile updated: ID=" + projectileId + ", x=" + x + ", y=" + y);
                    return;
                }
            }

            // Ha nem találtuk meg, új lövedéket hozunk létre
            Player owner = otherPlayers.getOrDefault(ownerPlayerId, player);
            float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            float dirX = speed > 0 ? velocityX / speed : 0;
            float dirY = speed > 0 ? velocityY / speed : 0;

            Projectile newProjectile = new Projectile(
                    x, y, 10, 10, // Példa méretek
                    damage, speed, dirX, dirY, owner
            );
            newProjectile.setId(projectileId);
            projectiles.add(newProjectile);
            System.out.println("💥 New projectile created from server: ID=" + projectileId + ", x=" + x + ", y=" + y);
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (currentState == GameState.ABILITY_SELECTION) {
            abilitySelectionScreen.render();
        } else if (currentState == GameState.GAMEPLAY) {
            camera.applyTransform();

            mapRenderer.render(currentDungeon);

            if (isMultiplayer) {
                for (Player otherPlayer : otherPlayers.values()) {
                    if (otherPlayer.isAlive()) {
                        otherPlayer.render();
                    }
                }
            }

            for (Enemy enemy : currentDungeon.getEnemies()) {
                enemy.render();
            }

            player.render();

            for (Projectile projectile : projectiles) {
                projectile.render();
            }

            for (Effect effect : effects) {
                effect.render();
            }

            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);

            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            hud.render(player, playerEffects);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
        } else if (currentState == GameState.UPGRADE_CHOICE) {
            upgradeScreen.render();
        } else if (currentState == GameState.GAME_OVER) {
            if (gameOverScreen != null) {
                gameOverScreen.render();
            }
        }
    }

    private void handleUpgradeChoice() {
        if (upgradeScreen.isChoiceMade()) {
            int choice = upgradeScreen.getSelectedOption();
            switch (choice) {
                case 0:
                    player.addHealthBoost(20f);
                    break;
                case 1:
                    player.addDamageBoost(0.1f);
                    break;
                case 2:
                    player.addCritChanceBoost(0.05f);
                    break;
            }

            if (!upgradeScreen.isGameSaved()) {
                GameSaveHandler.savePlayer(player);
                System.out.println("✓ Automatikus mentés upgrade után");
            }

            upgradeScreen.reset();
            currentState = GameState.GAMEPLAY;
            loadNextLevel();
        }
    }

    private void handleGameOver() {
        if (gameOverScreen == null) {
            gameOverScreen = new GameOverScreen(window, width, height, player, currentDungeon);
        }
        gameOverScreen.update();

        if (gameOverScreen.isSaveGame()) {
            gameOverScreen.resetSaveFlag();
            System.out.println("Játék mentése kérése feldolgozva");
        }

        if (gameOverScreen.isReturnToLobby()) {
            cleanupGameResources();
            if (gameOverScreen != null) {
                gameOverScreen.cleanup();
                gameOverScreen = null;
            }
            glfwSetWindowShouldClose(window, true);
            return;
        }
    }

    private void cleanupGameResources() {
        if (playerIdleTexture != null) {
            playerIdleTexture.delete();
            playerIdleTexture = null;
        }

        if (walkFrames != null) {
            for (Texture t : walkFrames) {
                if (t != null) t.delete();
            }
            walkFrames.clear();
        }

        if (activeWalkFrames != null) {
            for (Texture t : activeWalkFrames) {
                if (t != null) t.delete();
            }
            activeWalkFrames.clear();
        }

        if (tileTextures != null) {
            for (Texture texture : tileTextures.values()) {
                if (texture != null) texture.delete();
            }
            tileTextures.clear();
        }

        if (enemyTexture != null) {
            enemyTexture.delete();
            enemyTexture = null;
        }

        if (boxDamageTextures != null) {
            for (Texture texture : boxDamageTextures.values()) {
                if (texture != null) texture.delete();
            }
            boxDamageTextures.clear();
        }

        if (gateAnimationTextures != null) {
            for (Texture texture : gateAnimationTextures.values()) {
                if (texture != null) texture.delete();
            }
            gateAnimationTextures.clear();
        }

        if (effectTextures != null) {
            for (Texture texture : effectTextures.values()) {
                if (texture != null) texture.delete();
            }
            effectTextures.clear();
        }

        if (teleportPadTexture != null) {
            teleportPadTexture.delete();
            teleportPadTexture = null;
        }

        if (textRenderer != null) {
            textRenderer.cleanup();
            textRenderer = null;
        }

        if (fontTexture != null) {
            fontTexture.delete();
            fontTexture = null;
        }

        if (weaponFactory != null) {
            weaponFactory.cleanup();
            weaponFactory = null;
        }

        if (currentDungeon != null) {
            currentDungeon.cleanup();
            currentDungeon = null;
        }

        if (projectiles != null) {
            projectiles.clear();
        }

        if (effects != null) {
            effects.clear();
        }

        if (playerEffects != null) {
            playerEffects.clear();
        }
    }

    private void cleanupForNextLevel() {
        System.out.println("🧹 Cleanup for next level - preserving player stats...");

        if (playerIdleTexture != null) {
            playerIdleTexture.delete();
            playerIdleTexture = null;
        }

        if (walkFrames != null) {
            for (Texture t : walkFrames) {
                if (t != null) t.delete();
            }
            walkFrames.clear();
        }

        if (activeWalkFrames != null) {
            for (Texture t : activeWalkFrames) {
                if (t != null) t.delete();
            }
            activeWalkFrames.clear();
        }

        if (tileTextures != null) {
            for (Texture texture : tileTextures.values()) {
                if (texture != null) texture.delete();
            }
            tileTextures.clear();
        }

        if (enemyTexture != null) {
            enemyTexture.delete();
            enemyTexture = null;
        }

        if (boxDamageTextures != null) {
            for (Texture texture : boxDamageTextures.values()) {
                if (texture != null) texture.delete();
            }
            boxDamageTextures.clear();
        }

        if (gateAnimationTextures != null) {
            for (Texture texture : gateAnimationTextures.values()) {
                if (texture != null) texture.delete();
            }
            gateAnimationTextures.clear();
        }

        if (effectTextures != null) {
            for (Texture texture : effectTextures.values()) {
                if (texture != null) texture.delete();
            }
            effectTextures.clear();
        }

        if (teleportPadTexture != null) {
            teleportPadTexture.delete();
            teleportPadTexture = null;
        }

        if (textRenderer != null) {
            textRenderer.cleanup();
            textRenderer = null;
        }

        if (fontTexture != null) {
            fontTexture.delete();
            fontTexture = null;
        }

        if (currentDungeon != null) {
            currentDungeon.cleanup();
            currentDungeon = null;
        }

        if (projectiles != null) {
            projectiles.clear();
        }

        if (effects != null) {
            effects.clear();
        }

        if (playerEffects != null) {
            playerEffects.clear();
        }

        System.out.println("✅ Next level cleanup complete - player preserved with stats: " +
                (player != null ? player.getMaxHealth() + " HP" : "NO PLAYER"));
    }

    private void cleanup() {
        try {
            cleanupGameResources();
            if (abilitySelectionScreen != null) abilitySelectionScreen.cleanup();
            if (upgradeScreen != null) upgradeScreen.cleanup();
            if (gameOverScreen != null) gameOverScreen.cleanup();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            glfwTerminate();

            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) {
                callback.free();
            }
        } catch (Exception e) {
            System.err.println("Hiba a cleanup során: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void loop() {
        double lastTime = glfwGetTime();
        double accumulator = 0.0;
        final double frameTime = 1.0 / 60.0;

        while (!glfwWindowShouldClose(window)) {
            if (currentState == GameState.GAMEPLAY) {
                inputHandler.update();
            }
            glfwPollEvents();

            double currentTime = glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            accumulator += deltaTime;

            if (currentState == GameState.GAMEPLAY && inputHandler.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                WeaponInterface currentWeapon = player.getCurrentWeapon();

                if (currentWeapon instanceof Weapon) {
                    float cursorScreenX = (float) getCursorX();
                    float cursorScreenY = (float) getCursorY();
                    float cameraWorldX = camera.getX();
                    float cameraWorldY = camera.getY();

                    float targetWorldX = cursorScreenX + cameraWorldX;
                    float targetWorldY = cursorScreenY + cameraWorldY;

                    float projectileBaseDamage = currentWeapon.getDamage();

                    Projectile newProjectile = currentWeapon.shoot(
                            player,
                            player.getX(),
                            player.getY(),
                            targetWorldX,
                            targetWorldY,
                            (float) currentTime
                    );
                    if (newProjectile != null) {
                        newProjectile.setDamage(projectileBaseDamage);
                        projectiles.add(newProjectile);
                    }
                    player.startAttackAnimation();
                } else if (currentWeapon instanceof MeleeWeapon) {
                    float timeSinceLastAttack = (float) currentTime - lastMeleeAttackTime;

                    if (timeSinceLastAttack >= meleeCooldownTime) {
                        handleMeleeAttack(player, (MeleeWeapon) currentWeapon, (float) currentTime);
                        lastMeleeAttackTime = (float) currentTime;
                        player.startAttackAnimation();
                    }
                }
            }

            while (accumulator >= frameTime) {
                update((float) frameTime, currentTime);
                accumulator -= frameTime;
            }

            render();
            glfwSwapBuffers(window);
        }
    }

    private void handleMeleeAttack(Player player, MeleeWeapon weapon, float currentTime) {
        float timeSinceLastAttack = currentTime - lastMeleeAttackTime;

        if (timeSinceLastAttack >= meleeCooldownTime) {
            float attackRange = weapon.getRange();
            boolean hitSomething = false;

            float playerCenterX = player.getX() + player.getWidth() / 2;
            float playerCenterY = player.getY() + player.getHeight() / 2;

            List<Enemy> hitEnemies = new ArrayList<>();

            for (Enemy enemy : currentDungeon.getEnemies()) {
                float enemyCenterX = enemy.getX() + enemy.getWidth() / 2;
                float enemyCenterY = enemy.getY() + enemy.getHeight() / 2;
                float distance = (float) Math.sqrt(
                        Math.pow(playerCenterX - enemyCenterX, 2) +
                                Math.pow(playerCenterY - enemyCenterY, 2)
                );
                if (distance < attackRange && enemy.isAlive()) {
                    hitEnemies.add(enemy);
                }
            }

            for (Enemy enemy : hitEnemies) {
                float finalDamage = player.calculateFinalDamage(weapon.getDamage(), enemy);
                enemy.takeDamage(finalDamage);
            }

            int tileSize = currentDungeon.getTileSize();
            for (int x = 0; x < currentDungeon.getWidthTiles(); x++) {
                for (int y = 0; y < currentDungeon.getHeightTiles(); y++) {
                    Tile tile = currentDungeon.getTiles()[x][y];
                    if (tile != null && tile.getType() == Tile.TileType.BOX) {
                        float tileCenterX = tile.getX() + tileSize / 2;
                        float tileCenterY = tile.getY() + tileSize / 2;
                        float distance = (float) Math.sqrt(
                                Math.pow(playerCenterX - tileCenterX, 2) +
                                        Math.pow(playerCenterY - tileCenterY, 2)
                        );

                        if (distance < attackRange) {
                            tile.takeDamage(weapon.getDamage());
                            if (tile.isDestroyed()) {
                                tile.setType(Tile.TileType.FLOOR);
                                tile.setTexture(tileTextures.get(Tile.TileType.FLOOR));
                                createRandomEffect(tile.getX(), tile.getY());
                            } else {
                                tile.updateTextureByHealth();
                            }
                            hitSomething = true;
                        }
                    }
                }
            }

            if (hitSomething) {
                System.out.println("DEBUG: Közelharci támadás sikeres!");
            }

            lastMeleeAttackTime = currentTime;
        } else {
            System.out.println("DEBUG: Közelharci fegyver cooldownon van.");
        }
    }

    private void createRandomEffect(float x, float y) {
        if (Math.random() < 0.4) {
            int rand = (int) (Math.random() * 3);
            Effect.EffectType type;
            Texture texture = null;

            switch (rand) {
                case 0:
                    type = Effect.EffectType.SPEED_BOOST;
                    texture = effectTextures.get(Effect.EffectType.SPEED_BOOST);
                    break;
                case 1:
                    type = Effect.EffectType.DAMAGE_BOOST;
                    texture = effectTextures.get(Effect.EffectType.DAMAGE_BOOST);
                    break;
                case 2:
                    type = Effect.EffectType.HEALTH_REGEN;
                    texture = effectTextures.get(Effect.EffectType.HEALTH_REGEN);
                    break;
                default:
                    return;
            }
            effects.add(new Effect(x, y, 32, 32, texture, type));
        }
    }

    private void checkEffectCollision() {
        Iterator<Effect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            if (collisionManager.checkCollision(player, effect)) {
                applyEffect(effect.getType());
                effect.isCollected = true;
                iterator.remove();
            }
        }
    }

    private void applyEffect(Effect.EffectType type) {
        switch (type) {
            case SPEED_BOOST:
                player.setMoveSpeed(player.getBaseMoveSpeed() * 1.5f);
                playerEffects.add(new PlayerEffect(Effect.EffectType.SPEED_BOOST, 5.0f));
                break;
            case DAMAGE_BOOST:
                player.setDamage(player.getCurrentWeapon().getBaseDamage() * 2.0f);
                playerEffects.add(new PlayerEffect(Effect.EffectType.DAMAGE_BOOST, 8.0f));
                break;
            case HEALTH_REGEN:
                player.heal(50);
                break;
        }
    }

    private void updatePlayerEffects(float deltaTime) {
        Iterator<PlayerEffect> iterator = playerEffects.iterator();
        while (iterator.hasNext()) {
            PlayerEffect effect = iterator.next();
            effect.duration -= deltaTime;
            if (effect.duration <= 0) {
                switch (effect.type) {
                    case SPEED_BOOST:
                        player.setMoveSpeed(player.getBaseMoveSpeed());
                        break;
                    case DAMAGE_BOOST:
                        player.setDamage(player.getCurrentWeapon().getBaseDamage());
                        break;
                    default:
                        break;
                }
                iterator.remove();
            }
        }
    }

    private double getCursorX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xPos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xPos, null);
            return xPos.get(0);
        }
    }

    private double getCursorY() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer yPos = stack.mallocDouble(1);
            glfwGetCursorPos(window, null, yPos);
            return yPos.get(0);
        }
    }

    public static void main(String[] args) {
        boolean loop = true;
        while (loop) {
            new Lobby().run();
            loop = !Lobby.shouldExitGame();
        }
    }
}