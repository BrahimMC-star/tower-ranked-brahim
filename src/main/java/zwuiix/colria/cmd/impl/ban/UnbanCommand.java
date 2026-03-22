package zwuiix.colria.cmd.impl.ban;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.punishment.ban.BanManager;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class UnbanCommand extends ColriaCommand {

    public UnbanCommand() {
        super("unban", "Unban a player");
        setPermission(Permission.STAFF_UNBAN.toString());

        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        var manager = BanManager.getInstance();
        String targetName = args.get("target").toString();

        if (!manager.isBanned(targetName)) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_UNBAN_NOTBANNED, targetName));
            return;
        }

        manager.unban(targetName);
        sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_UNBAN_SUCCESS, targetName));
        Chat.broadcast(TranslationKeys.PLAYER_COMMAND_UNBAN_BROADCAST, targetName, sender.getName());
    }
}