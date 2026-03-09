package zwuiix.colria.player;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.GUIDataPickItemPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.types.DisplaySlot;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.utils.Identifier;
import cn.nukkit.utils.TextFormat;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.*;
import zwuiix.colria.event.PlayerKnockbackEvent;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameEvent;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.cooldown.Cooldown;
import zwuiix.colria.player.cosmetic.Cosmetic;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.player.cosmetic.Pet;
import zwuiix.colria.player.particle.Particle;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.rank.Rank;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.Language;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.KeyInput;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnginePlayer extends Player {
    public boolean logged = false;
    public long start = System.currentTimeMillis();

    public long sinceLastJump = 0;
    public long sinceLastDoubleJump = 0;
    public long sinceLastInput = 0;
    public List<KeyInput> keys = new ArrayList<>();

    @Getter
    @Setter
    private PlayerDataInfo playerDataInfo = null;
    @Getter
    private final ArrayList<Rank> ranks = new ArrayList<>();
    @Getter
    private final ArrayList<Particle> particles = new ArrayList<>();
    @Getter
    private final ArrayList<Cosmetic> cosmetics = new ArrayList<>();
    @Getter
    private final ArrayList<Pet> pets = new ArrayList<>();
    @Getter
    private final LinkedHashMap<String, Cooldown> cooldowns = new LinkedHashMap<>();

    @Setter
    @Getter
    private Game game = null;
    public CommandSender reply = null;
    public PlayerScoreboard scoreboard = null;
    @Getter
    private final HashMap<String, Ticker> tickers = new HashMap<>();
    @Setter
    private boolean inAdminMode = false;

    private int tickSinceSendTitle = 0;

    public int cps = 0;
    public int fps = 0;

    public EnginePlayer(SourceInterface interfaz, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, clientID, socketAddress);
    }

    public boolean isFlying() {
        return getAdventureSettings().get(AdventureSettings.Type.FLYING);
    }

    public void setFlying(boolean flying) {
        getAdventureSettings().set(AdventureSettings.Type.FLYING, flying);
    }

    public <T extends GamePlayer> T getGamePlayer() {
        return (game == null) ? null : (T) game.getPlayer(getName()); // unchecked cast
    }

    public String getXUID() {
        return getLoginChainData().getXUID();
    }

    public boolean isInLobby() {
        return game != null && game instanceof Lobby;
    }

    public boolean hasRank(String rankName) {
        for(Rank rank : ranks) {
            if(rank.getName().equalsIgnoreCase(rankName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRank(int rankId) {
        for(Rank rank : ranks) {
            if(rank.getId() == rankId) {
                return true;
            }
        }
        return false;
    }

    public Rank getHighestRank() {
        Rank highest = RankRegistry.getInstance().getDefaultRank();
        for(Rank rank : ranks) {
            if(highest == null || rank.getId() > highest.getId()) {
                highest = rank;
            }
        }
        return highest;
    }

    public void addRank(Rank rank) {
        if(!ranks.contains(rank)) {
            ranks.add(rank);
        }
    }

    public boolean hasParticle(Particle particle) {
        return particles.contains(particle);
    }

    public boolean hasParticle(String identifier) {
        for(Particle particle : particles) {
            if(particle.getIdentifier().equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void addParticle(Particle particle) {
        if(!particles.contains(particle)) {
            particles.add(particle);
        }
    }

    public void removeParticle(Particle particle) {
        particles.remove(particle);
    }

    public boolean hasCosmetic(Cosmetic cosmetic) {
        return cosmetics.contains(cosmetic);
    }

    public boolean hasCosmetic(String identifier) {
        for(Cosmetic cosmetic : cosmetics) {
            if(cosmetic.getIdentifier().equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void addCosmetic(Cosmetic cosmetic) {
        if(!cosmetics.contains(cosmetic)) {
            cosmetics.add(cosmetic);
        }
    }

    public void removeCosmetic(Cosmetic cosmetic) {
        cosmetics.remove(cosmetic);
    }

    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }

    public boolean hasPet(String identifier) {
        for(Pet pet : pets) {
            if(pet.getIdentifier().equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void addPet(Pet pet) {
        if(!pets.contains(pet)) {
            pets.add(pet);
        }
    }

    public void removePet(Pet pet) {
        pets.remove(pet);
    }

    public Cooldown getCooldown(String identifier) {
        if(!cooldowns.containsKey(identifier)) {
            var cd = new Cooldown(getXUID(), identifier, 0);
            cooldowns.put(identifier, cd);
            return cd;
        }

        Cooldown cd = cooldowns.get(identifier);
        if(cd.isExpired()) {
            cooldowns.remove(identifier);
            var newCd = new Cooldown(getXUID(), identifier, 0);
            cooldowns.put(identifier, newCd);
            return newCd;
        }

        return cd;
    }

    public CompletableFuture<Void> resync() {
        this.ranks.clear();
        long start = System.currentTimeMillis();
        var db = DataBase.getInstance();
        String xuid = getXUID();
        String name = getName();

        record Agg(List<Integer> ranks, List<String> particles, List<String> cosmetics, List<String> pets, Map<String, Cooldown> cooldows) {}

        return db.write(PlayerDataDao.class, (Function<PlayerDataDao, PlayerDataInfo>) dao -> dao.getOrCreate(xuid, name))
                .thenCompose(info -> {
                    this.playerDataInfo = info;

                    var ranks = db.query(PlayerRankDao.class, dao -> dao.listByXuid(xuid));
                    var particles = db.query(PlayerParticleDao.class, dao -> dao.listByXuid(xuid));
                    var cosmetics = db.query(PlayerCosmeticDao.class, dao -> dao.listByXuid(xuid));
                    var pets = db.query(PlayerPetDao.class, dao -> dao.listByXuid(xuid));
                    var cooldowns = db.query(PlayerCooldownDao.class, dao -> dao.listByXuid(xuid));

                    return CompletableFuture.allOf(ranks, particles, cosmetics)
                            .thenApply(v -> new Agg(
                                    ranks.join(),
                                    particles.join(),
                                    cosmetics.join(),
                                    pets.join(),
                                    cooldowns.join()
                            ));
                })
                .thenAccept(agg -> {
                    for (Integer id : agg.ranks()) {
                        RankRegistry.getInstance().getRank(id).ifPresent(this::addRank);
                    }
                    syncRanks();

                    cooldowns.clear();
                    for (Cooldown cd : agg.cooldows.values()) {
                        if(!cd.isExpired()) {
                            this.cooldowns.put(cd.getAction(), cd);
                        }
                    }

                    particles.clear();
                    for (String pid : agg.particles()) {
                        var p = ParticleRegistry.getInstance().getParticle(pid);
                        if (p != null) {
                            addParticle(p);
                        }
                    }

                    cosmetics.clear();
                    for (String cid : agg.cosmetics()) {
                        var p = CosmeticRegistry.getInstance().getCosmetic(cid);
                        if (p != null) {
                            addCosmetic(p);
                        }

                        var cape = CosmeticRegistry.getInstance().getCape(cid);
                        if(cape != null) {
                            addCosmetic(cape);
                        }
                    }

                    pets.clear();
                    for (String petId : agg.pets()) {
                        var pet = CosmeticRegistry.getInstance().getPet(petId);
                        if (pet != null) {
                            addPet(pet);
                        }
                    }

                    var selId = this.playerDataInfo.getParticle();
                    if(selId != null) {
                        var selected = ParticleRegistry.getInstance().getParticle(selId);
                        if (selected != null && hasParticle(selected) && isInLobby()) {
                            if (selected.isFlying()) {
                                setAllowFlight(true);
                                setFlying(true);
                            } else if (isSurvival() || isAdventure()) {
                                setAllowFlight(false);
                                setFlying(false);
                            }
                        }
                    }

                    if(!logged) {
                        long elapsed = System.currentTimeMillis() - start;
                        sendGuiDataText(TranslationKeys.PLAYER_RESYNC, elapsed);
                    }
                    this.logged = true;
                })
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    public void syncRanks() {
        if(isInLobby()) {
            var rank = getHighestRank();
            if (rank.isDefault()) {
                setNameTag(rank.getColor() + getName());
            } else setNameTag(rank.getColoredName() + " " + getName());
        }

        getEffectivePermissions().clear();
        for(Rank rank : ranks) {
            for(String perm : rank.getPermissions()) {
                getEffectivePermissions().put(perm, new PermissionAttachmentInfo(this, perm, null, true));
            }
        }

        recalculatePermissions();
    }

    public void updateCape(String cape) {
        playerDataInfo.setCape(cape);
        setEnumEntityProperty("colria:cape", cape);

        var c = CosmeticRegistry.getInstance().getCape(cape);
        if(c != null && hasCosmetic(cape)) {
            c.apply(this);
        }

        sendData(this);
        sendData(getViewers().values().toArray(Player[]::new));
    }

    @Override
    protected void doFirstSpawn() {
        super.doFirstSpawn();
        scoreboard = new PlayerScoreboard(this);
        scoreboard.addViewer(this, DisplaySlot.SIDEBAR);

        addTicker("scoreboard", i -> {
            if(!isInLobby()) return; // Only update scoreboard in lobby
            if(i % 20 != 0) return; // Update every second

            ArrayList<String> lines = new ArrayList<>();
            lines.add(EngineInfo.VBAR_DEFAULT + processTranslation(TranslationKeys.PLAYER_LOBBY_SCOREBOARD_PLAYERS, getServer().getOnlinePlayersCount()));
            lines.add(EngineInfo.VBAR_DEFAULT + processTranslation(TranslationKeys.PLAYER_LOBBY_SCOREBOARD_CURRENT,  "#" + getGame().getIdentifier()));
            lines.add(TextFormat.MINECOIN_GOLD.toString());
            lines.add(EngineInfo.VBAR_DEFAULT + "§r§b§l" + getName());
            lines.add(EngineInfo.VBAR_DEFAULT + processTranslation(TranslationKeys.PLAYER_LOBBY_SCOREBOARD_RANK, getHighestRank().getColoredName()));
            lines.add(EngineInfo.VBAR_DEFAULT + processTranslation(TranslationKeys.PLAYER_LOBBY_SCOREBOARD_SHARDS, getPlayerDataInfo().getShards()));

            scoreboard.title("colria");
            scoreboard.updates(lines);
        });
    }

    public void onDisconnect() {

    }

    public String processTranslation(String key, Object... args) {
        return Translator.getInstance().process(Language.fromLangCode(getLanguageCode()), key, args);
    }

    public String processTranslation(TranslationKeys translatable, Object ...args) {
        return processTranslation(translatable.toString(), args);
    }

    public void sendMessage(TranslationKeys translatable, Object ...params) {
        super.sendMessage(processTranslation(translatable, params));
    }

    public void sendActionBar(TranslationKeys translatable, Object ...params) {
        super.sendActionBar(processTranslation(translatable, params));
    }

    public void sendGuiDataText(TranslationKeys translatable, Object ...params) {
        GUIDataPickItemPacket pk = new GUIDataPickItemPacket();
        pk.description = processTranslation(translatable, params);
        pk.itemEffects = "";
        pk.hotbarSlot = 0;
        this.dataPacket(pk);
    }

    public void addTicker(String key, Consumer<Integer> consumer) {
        addTicker(key, consumer, 1);
    }

    public void addTicker(String key, Consumer<Integer> consumer, int interval) {
        tickers.put(key, new Ticker(interval, consumer));
    }

    public void removeTicker(String key) {
        if(!tickers.containsKey(key)) return;
        tickers.remove(key);
    }

    public void sudo(String command) {
        getServer().dispatchCommand(this, command);
    }

    public void addSound(Sound sound, float volume, float pitch) {
        addSound(sound.getSound(), volume, pitch);
    }

    public void addSound(String sound, float volume, float pitch) {
        Preconditions.checkArgument(volume >= 0 && volume <= 1, "Sound volume must be between 0 and 1");
        Preconditions.checkArgument(pitch >= 0, "Sound pitch must be higher than 0");

        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound;
        packet.volume = volume;
        packet.pitch = pitch;
        packet.x = this.getFloorX();
        packet.y = this.getFloorY();
        packet.z = this.getFloorZ();

        this.dataPacket(packet);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_CHESTED, true);
        this.dataProperties
                .putByte(DATA_CONTAINER_TYPE, 12)
                .putInt(DATA_CONTAINER_BASE_SIZE, 0);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        boolean updated = super.onUpdate(currentTick);

        tickSinceSendTitle--;
        if (playerDataInfo != null && updated) {
            if (!tickers.isEmpty()) {
                var snapshot = new java.util.ArrayList<>(tickers.values());
                for (Ticker entry : snapshot) {
                    if (entry.interval != 0 && (currentTick % entry.interval) == 0) {
                        entry.ticker().accept(currentTick);
                    }
                }
            }

            if ((currentTick % 6_000) == 0) {
                resync();
            }

            if ((currentTick % 10) == 0 && scoreboard != null) {
                scoreboard.sync();
            }

            var identifier = this.getPlayerDataInfo().getParticle();
            var canShowParticle = isInLobby() || (game != null && game.getState().equals(Game.State.LOBBY));
            if (identifier != null && canShowParticle && this.riding == null) {
                var particle = ParticleRegistry.getInstance().getParticle(identifier);
                if (particle != null && hasParticle(identifier)) {
                    if (particle.isFlying()) setAllowFlight(true);
                    if (particle.isFlying()
                            && this.isFlying()
                            && (isSurvival() || isAdventure())
                            && sinceLastInput > 20 * 10
                            && particle.getCost() <= 7500) {
                        setMotion(new Vector3(0, -0.05, 0));
                    }

                    particle.run(this, currentTick);
                }
            }
        }

        return updated;
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z) {
        this.knockBack(attacker, damage, x, z, 0.4);
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
        double f = Math.sqrt((x * x) + (z * z));
        if (f <= 0) {
            return;
        }

        var ev = new PlayerKnockbackEvent(this, base, base);
        ev.call();

        var game = this.getGame();
        if(game != null) GameEvent.publish(game, ev);

        f = 1 / f;

        var motionX = this.motionX / 2;
        var motionY = this.motionY / 2;
        var motionZ = this.motionZ / 2;
        motionX += x * f * ev.getStrengthXZ();
        motionY += ev.getStrengthY();
        motionZ += z * f * ev.getStrengthXZ();

        this.setMotion(new Vector3(motionX, motionY, motionZ));
        this.resetFallDistance();

        this.knockBackTime = 10;
    }

    @Nullable
    @Override
    public Identifier getIdentifier() {
        return Identifier.tryParse("minecraft:player");
    }

    public boolean inAdminMode() {
        return inAdminMode;
    }

    private record Ticker(int interval, Consumer<Integer> ticker) {}

    public void needDisplayTitleInfo()
    {
        if (!logged) return;
        if (tickSinceSendTitle > 0) return;

        StringBuilder title = new StringBuilder();
        StringBuilder subtitle = new StringBuilder();

        var settings = getPlayerDataInfo().getSettings();
        boolean needFPS = (boolean) settings.getOrDefault("fps", "enabled", true) && fps > 0;
        boolean needCPS = (boolean) settings.getOrDefault("cps", "enabled", false);
        boolean needPing = (boolean) settings.getOrDefault("ping", "enabled", false);

        if (needFPS) title.append(fps);
        if (needCPS) subtitle.append(cps).append('\n');
        if (needPing) subtitle.append(getPing()).append('\n');

        if (title.isEmpty() && subtitle.isEmpty()) return;
        System.out.println("[" + title + "]" + "-" + subtitle);
        super.sendTitle("§s§e§t§t§i§n§g§s§r[" + title + "]", subtitle.toString(), 0, 40, 0);
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        super.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        tickSinceSendTitle = (fadeIn + stay + fadeOut) + 20;
    }
}
