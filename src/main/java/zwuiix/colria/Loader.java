package zwuiix.colria;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import zwuiix.colria.block.BlockRegistry;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.cmd.CommandRegistry;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.*;
import zwuiix.colria.discord.DiscordAPI;
import zwuiix.colria.discord.DiscordToken;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.inventory.InventoryHooker;
import zwuiix.colria.item.ItemRegistry;
import zwuiix.colria.listener.EventListener;
import zwuiix.colria.listener.GameListener;
import zwuiix.colria.listener.WorldListener;
import zwuiix.colria.permission.PermissionRegistry;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.task.BroadcastTask;
import zwuiix.colria.translator.LanguageRegistry;
import zwuiix.colria.util.Fs;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Loader extends PluginBase {
    @Override
    public void onLoad() {
        List<String> worlds = Fs.getFolders(Path.of(Server.getInstance().getFilePath(), "worlds"));
        for (String world : worlds) {
            if(world.contains("_")) {
                Fs.deleteDirectory(Path.of(Server.getInstance().getFilePath(), world));
            }
        }

        getServer().loadLevel("lobby");
        getServer().loadLevel("waiting");

        ItemRegistry itemRegistry = ItemRegistry.getInstance();
        itemRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + itemRegistry.getItems().size() + " custom items."));

        BlockRegistry blockRegistry = BlockRegistry.getInstance();
        blockRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + blockRegistry.getBlocks().size() + " custom blocks."));
    }

    @Override
    public void onEnable() {
        String protocols = ProtocolInfo.SUPPORTED_PROTOCOLS.stream()
                .sorted()
                .map(Utils::getVersionByProtocol)
                .collect(Collectors.joining(", "));

        getLogger().info("Loading Colria Plugin...");
        getLogger().info(TextFormat.colorize("&bEngine running in " + EngineInfo.VERSION));
        getLogger().info(TextFormat.colorize('&',
                "&bWith Minecraft Protocols: &8" + protocols));

        InventoryHooker.register(this);
        getLogger().notice(TextFormat.colorize("&bInitialized InventoryHooker."));

        DataBase dataBase = new DataBase(Path.of(getDataFolder().toString(), "colria.db").toString());
        dataBase.write(PlayerDataDao.class, PlayerDataDao::init);
        dataBase.write(PlayerRankDao.class, PlayerRankDao::init);
        dataBase.write(PlayerParticleDao.class, PlayerParticleDao::init);
        dataBase.write(PlayerCosmeticDao.class, PlayerCosmeticDao::init);
        dataBase.write(PlayerPetDao.class, PlayerPetDao::init);
        dataBase.write(PlayerCooldownDao.class, PlayerCooldownDao::init);

        getLogger().info(TextFormat.colorize("&bDatabase initialized."));

        RankRegistry rankRegistry = RankRegistry.getInstance();
        rankRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + rankRegistry.getRanks().size() + " ranks."));

        GameRegistry gameRegistry = GameRegistry.getInstance();
        gameRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + gameRegistry.getGameModes().size() + " gamemodes."));

        PermissionRegistry permissionRegistry = PermissionRegistry.getInstance();
        permissionRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + permissionRegistry.getPermissions().size() + " permissions."));

        LanguageRegistry languageRegistry = LanguageRegistry.getInstance();
        languageRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + languageRegistry.getLanguages().size() + " languages."));

        ParticleRegistry particleRegistry = ParticleRegistry.getInstance();
        particleRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + particleRegistry.getParticles().size() + " particles."));

        CosmeticRegistry cosmeticRegistry = CosmeticRegistry.getInstance();
        cosmeticRegistry.invoke(this);
        getLogger().info(TextFormat.colorize("&bLoaded " + cosmeticRegistry.getCosmetics().size() + " cosmetics."));

        BoosterManager boosterManager = new BoosterManager(this);
        getLogger().info(TextFormat.colorize("&bBooster Manager initialized."));

        CommandRegistry commandRegistry =  CommandRegistry.getInstance();
        commandRegistry.injects();
        getLogger().info(TextFormat.colorize("&bLoaded " + commandRegistry.getCommands().size() + " commands."));

        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);

        getServer().getScheduler().scheduleRepeatingTask(new BroadcastTask(), 20 * 60);

        DiscordAPI discordAPI = new DiscordAPI(DiscordToken.TOKEN);
        discordAPI.connect();
    }

    @Override
    public void onDisable() {
        DiscordAPI discordAPI = DiscordAPI.getInstance();
        if (discordAPI != null) discordAPI.disconnect();

        DataBase db = DataBase.getInstance();
        if (db != null) db.close();

        BoosterManager boosterManager = BoosterManager.getInstance();
        if (boosterManager != null) boosterManager.close();

        for (Game game : GameRegistry.getInstance().getLobbies().values()) game.stop();
        for (Game game : GameRegistry.getInstance().getGames().values()) game.stop();
    }
}
