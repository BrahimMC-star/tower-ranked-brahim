package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.arguments.MessageArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class TellCommand extends ColriaCommand {
    public TellCommand() {
        super("tell", "commands.tell.description");
        setAliases(new String[]{"w", "msg"});
    }

    @Override
    public void prepare() {
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new MessageArgument("message", false));
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        String name = args.get("target").toString();

        EnginePlayer target = (EnginePlayer) Server.getInstance().getPlayerExact(name);
        if(target == null) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_CANTFIND, name));
            return;
        }

        if(sender.getName().equalsIgnoreCase(target.getName())) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.TELL_CANTURSELF));
            return;
        }

        target.reply = sender;
        String message = Chat.clean(args.get("message").toString());
        sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.TELL_TO, name, message));
        target.sendMessage(TranslationKeys.TELL_FROM, sender.getName(), message);
    }
}
