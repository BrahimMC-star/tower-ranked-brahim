package zwuiix.colria.cmd.impl.mute;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.punishment.PunishmentManager;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class UnmuteCommand extends ColriaCommand {

    public UnmuteCommand() {
        super("unmute", "Unmute a player");
        setPermission(Permission.STAFF_UNMUTE.toString());

        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        var manager = PunishmentManager.getInstance();
        String targetName = args.get("target").toString();

        if (!manager.isMuted(targetName)) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_UNMUTE_NOTMUTED, targetName));
            return;
        }

        manager.unmute(targetName);
        sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_UNMUTE_SUCCESS, targetName));
        Chat.broadcast(TranslationKeys.PLAYER_COMMAND_UNMUTE_BROADCAST, targetName, sender.getName());
    }
}