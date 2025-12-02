package zwuiix.colria.cmd.impl.link;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class UnLinkCommand extends ColriaPlayerCommand {
    public UnLinkCommand() {
        super("unlink", "commands.unlink.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.LINK_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(target);
        if(targetPlayer == null) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target);
            return;
        }

        var id = targetPlayer.getPlayerDataInfo().getDiscordId();
        if(id.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_UNLINK_NOTLINKED, targetPlayer.getUsername());
            return;
        }

        targetPlayer.getPlayerDataInfo().setDiscordId("");
        player.sendMessage(TranslationKeys.PLAYER_COMMAND_UNLINK_SUCCESS, targetPlayer.getUsername());
    }
}
