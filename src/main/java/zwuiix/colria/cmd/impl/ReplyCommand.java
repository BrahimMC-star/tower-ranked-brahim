package zwuiix.colria.cmd.impl;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.MessageArgument;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class ReplyCommand extends ColriaPlayerCommand {
    public ReplyCommand() {
        super("reply", "commands.tell.description");
        setAliases(new String[]{"r"});
    }

    @Override
    public void prepare() {
        registerArgument(0, new MessageArgument("message", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        CommandSender target = player.reply;
        if(target == null) {
            player.sendMessage(TranslationKeys.TELL_NOTHING);
            return;
        }

        if(target instanceof EnginePlayer p && !p.isConnected()) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, p.getName());
            return;
        }

        if(player.getName().equalsIgnoreCase(target.getName())) {
            player.sendMessage(TranslationKeys.TELL_CANTURSELF);
            return;
        }

        if(target instanceof EnginePlayer p) p.reply = player;

        String message = Chat.clean(args.get("message").toString());
        player.sendMessage(TranslationKeys.TELL_TO, target.getName(), message);
        target.sendMessage(Translator.getInstance().autoProcess(target, TranslationKeys.TELL_FROM, player.getName(), message));
    }
}
