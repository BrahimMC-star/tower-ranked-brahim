package zwuiix.colria.cmd.impl.booster;

import cn.nukkit.Server;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class ThanksCommand extends ColriaPlayerCommand {
    public ThanksCommand() {
        super("thanks", "commands.thanks.description");
        setAliases(new String[]{"merci"});
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var manager = BoosterManager.getInstance();
        var current = manager.getCurrent();

        if(current == null) {
            player.sendMessage(TranslationKeys.BOOSTER_NONE);
            return;
        }

        var booster = current.booster();
        var claims = booster.getClaims();

        if(claims.contains(player.getName().toLowerCase())) {
            player.sendMessage(TranslationKeys.BOOSTER_THANKS_ALREADY, booster.getOwner());
            return;
        }

        if(booster.getOwner().equalsIgnoreCase(player.getName())) {
            player.sendMessage(TranslationKeys.BOOSTER_THANKS_SELF);
            return;
        }

        claims.add(player.getName().toLowerCase());

        int reward = 6 + (int)(Math.random() * ((50 - 6) + 1));
        long shards = (long) ((long)reward * (booster.getMultiplier() / 100));

        player.getPlayerDataInfo().increaseShards(shards);
        player.sendMessage(TranslationKeys.BOOSTER_CLAIM, booster.getOwner(), shards);

        DB.getPlayerDataInfo(booster.getOwner()).then(info -> {
            var s = (long) (shards * 1.5f);
            info.increaseShards(s);

            var p = Server.getInstance().getPlayerExact(booster.getOwner());
            if(p instanceof  EnginePlayer e) {
                e.sendMessage(TranslationKeys.BOOSTER_THANKS, player.getName(), s);
            }
        });
    }
}
