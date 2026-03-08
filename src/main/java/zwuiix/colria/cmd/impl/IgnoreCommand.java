package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.MessageArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class IgnoreCommand extends ColriaPlayerCommand {
    public IgnoreCommand() {
        super("ignore", "commands.ignore.description");
        setAliases(new String[]{"blocked"});
    }

    @Override
    public void prepare() {
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        String name = args.get("target").toString();

        EnginePlayer target = (EnginePlayer) Server.getInstance().getPlayerExact(name);
        if(target == null) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, name);
            return;
        }

        if(player.getName().equalsIgnoreCase(target.getName())) {
            player.sendMessage(TranslationKeys.TELL_IGNORE_CANTURSELF);
            return;
        }

        var settings = player.getPlayerDataInfo().getSettings();
        boolean isIgnoring = (boolean) settings.getOrDefault("ignores", target.getName().toLowerCase(), false);
        if (isIgnoring) {
            player.sendMessage(TranslationKeys.TELL_IGNORE_ALREADY, target.getName());
            return;
        }

        settings.set("ignores", target.getName().toLowerCase(), true);
        player.getPlayerDataInfo().setSettings(settings);
        player.sendMessage(TranslationKeys.TELL_IGNORE_ALREADY, target.getName());
    }
}
