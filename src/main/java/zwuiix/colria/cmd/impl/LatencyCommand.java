package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class LatencyCommand extends ColriaPlayerCommand {
    public LatencyCommand() {
        super("latency", "Show player latency");
        setAliases(new String[]{"ping", "ms"});
    }

    @Override
    public void prepare() {
        registerArgument(0, new TargetArgument("target", true));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        if(!args.containsKey("target")) {
            player.sendMessage(TranslationKeys.LATENCY_SELF, player.getPing());
            return;
        }

        String name = args.get("target").toString();
        EnginePlayer target = (EnginePlayer) Server.getInstance().getPlayerExact(name);
        if(target == null) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, name);
            return;
        }

        player.sendMessage(TranslationKeys.LATENCY_OTHER, target.getName(), target.getPing());
    }
}
