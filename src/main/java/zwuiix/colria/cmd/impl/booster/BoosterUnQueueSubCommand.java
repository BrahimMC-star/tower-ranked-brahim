package zwuiix.colria.cmd.impl.booster;

import zwuiix.colria.booster.Booster;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class BoosterUnQueueSubCommand extends ColriaPlayerSubCommand {
    public BoosterUnQueueSubCommand() {
        super("unqueue");
    }

    @Override
    public void prepare() {
        setPermission(Permission.BOOSTER_MANAGE.toString());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var manager = BoosterManager.getInstance();
        var current = manager.getCurrent();

        if(current == null) {
            player.sendMessage(TranslationKeys.BOOSTER_QUEUE_EMPTY);
            return;
        }

        manager.setCurrent(new Booster.Current(current.booster(), current.start(), current.start()));
        player.sendMessage(TranslationKeys.BOOSTER_QUEUE_UNQUEUE, current.booster().owner());
    }
}
