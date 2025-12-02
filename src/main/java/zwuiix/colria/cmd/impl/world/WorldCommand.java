package zwuiix.colria.cmd.impl.world;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Fs;
import zwuiix.colria.util.Glyph;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WorldCommand extends ColriaCommand {
    public WorldCommand()  {
        super("world", "Manage world");
    }

    @Override
    public void prepare() {
        setPermission(Permission.WORLD_MANAGE.toString());
        registerSubCommand(new WorldLoadSubCommand());
        registerSubCommand(new WorldTeleportSubCommand());
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        List<String> worlds = Fs.getFolders(Path.of(Server.getInstance().getFilePath(), "worlds"));

        sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.WORLD_LIST));
        if(worlds.isEmpty()) {
            sender.sendMessage(Glyph.vbar(TextFormat.GRAY, 1) + Translator.getInstance().autoProcess(sender, TranslationKeys.WORLD_LIST_EMPTY));
            return;
        }

        for (String world : worlds) {
            Level level = sender.getServer().getLevelByName(world);
            if(level == null) {
                sender.sendMessage(Glyph.vbar(TextFormat.DARK_GRAY, 1) + " §b" + Translator.getInstance().autoProcess(sender, TranslationKeys.WORLD_LIST_FORMAT_UNLOADED, world));
                continue;
            }

            sender.sendMessage(Glyph.vbar(TextFormat.DARK_GRAY, 1) + " §b" + Translator.getInstance().autoProcess(sender, TranslationKeys.WORLD_LIST_FORMAT_LOADED, world, level.getEntities().length, level.getChunks().size()));
        }
    }
}
