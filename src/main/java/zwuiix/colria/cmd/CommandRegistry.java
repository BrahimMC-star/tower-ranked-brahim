package zwuiix.colria.cmd;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.SimpleCommandMap;
import lombok.Getter;
import zwuiix.colria.cmd.impl.*;
import zwuiix.colria.cmd.impl.ban.BanCommand;
import zwuiix.colria.cmd.impl.ban.UnbanCommand;
import zwuiix.colria.cmd.impl.booster.BoosterCommand;
import zwuiix.colria.cmd.impl.booster.ThanksCommand;
import zwuiix.colria.cmd.impl.capes.CapesCommand;
import zwuiix.colria.cmd.impl.cosmetic.CosmeticsCommand;
import zwuiix.colria.cmd.impl.debug.DebugCommand;
import zwuiix.colria.cmd.impl.game.GameCommand;
import zwuiix.colria.cmd.impl.game.admin.GameAdminCommand;
import zwuiix.colria.cmd.impl.link.LinkCommand;
import zwuiix.colria.cmd.impl.link.UnLinkCommand;
import zwuiix.colria.cmd.impl.mute.MuteCommand;
import zwuiix.colria.cmd.impl.mute.UnmuteCommand;
import zwuiix.colria.cmd.impl.particle.ParticlesCommand;
import zwuiix.colria.cmd.impl.rank.RanksCommand;
import zwuiix.colria.cmd.impl.shard.ShardCommand;
import zwuiix.colria.cmd.impl.world.WorldCommand;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommandRegistry {
    @Getter
    private static final CommandRegistry instance = new CommandRegistry();

    private final HashMap<String, ColriaCommand> commands = new HashMap<>();

    public CommandRegistry() {
        register(new StatusCommand());
        register(new DebugCommand());
        register(new WorldCommand());
        register(new TellCommand());
        register(new ReplyCommand());
        register(new IgnoreCommand());
        register(new UnIgnoreCommand());
        register(new LatencyCommand());

        register(new HubCommand());
        register(new SpawnCommand());
        register(new SettingsCommand());
        register(new StatsCommand());

        register(new RanksCommand());
        register(new ShardCommand());
        register(new ParticlesCommand());
        register(new CosmeticsCommand());
        register(new CapesCommand());

        register(new BoosterCommand());
        register(new ThanksCommand());

        register(new GameCommand());
        register(new GameAdminCommand());

        register(new LinkCommand());
        register(new UnLinkCommand());

        register(new BanCommand());
        register(new UnbanCommand());
        register(new MuteCommand());
        register(new UnmuteCommand());
    }

    public void register(ColriaCommand command) {
        commands.put(command.getName(), command);
    }

    public void unregister(ColriaCommand command) {
        commands.remove(command.getName());
    }

    public void injects() {
        SimpleCommandMap map = Server.getInstance().getCommandMap();
        map.unregister(
                "tell", "ban", "ban-ip", "banlist", "pardon", "pardon-ip",
                "list", "kick", "kill", "xp", "world", "genworld", "spawn", "defaultgamemode",
                "say", "me", "particle", "spawnpoint", "title", "transfer", "seed", "playsound", "stopsound",
                "hud", "camerashake", "camera", "inputpermission", "ability", "scoreboard", "playanimation", "tag", "tellraw", "titleraw",
                "testfor", "testforblock", "testforblocks", "clearspawnpoint", "execute", "tickingarea"
        );

        Map<String, Command> currentCommands = map.getCommands();

        for (ColriaCommand command : commands.values()) {
            Command cmd = currentCommands.get(command.getName());
            if(cmd != null) {
                currentCommands.put(command.getName(), command);
            }

            map.register("colria", command);
        }
    }
}
