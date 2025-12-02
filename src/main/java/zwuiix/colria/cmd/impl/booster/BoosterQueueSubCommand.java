package zwuiix.colria.cmd.impl.booster;

import zwuiix.colria.booster.Booster;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

public class BoosterQueueSubCommand extends ColriaPlayerSubCommand {
    public BoosterQueueSubCommand() {
        super("queue");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var manager = BoosterManager.getInstance();
        var dataInfo = player.getPlayerDataInfo();

        if(dataInfo.getBooster() <= 0) {
            player.sendMessage(TranslationKeys.BOOSTER_NOTENOUGH);
            return;
        }

        dataInfo.decreaseBooster(1);
        long delay = 0;

        var current = manager.getCurrent();
        if(current != null)
            delay += manager.getRemaining();

        var queues = manager.getQueue();
        if(!queues.isEmpty())
            delay += queues.size() * BoosterManager.START_TIME;

        Duration d = Duration.ofSeconds(delay);
        long hours   = d.toHours();
        int minutes  = d.toMinutesPart();
        int seconds  = d.toSecondsPart();

        player.sendMessage(TranslationKeys.BOOSTER_ACTIVATED, "Booster", String.format("%02dh %02dm %02ds", hours, minutes, seconds));
        manager.addBooster(new Booster(
                "Booster",
                player.getName(),
                200,
                60 * 60,
                new ArrayList<>()
        ));
    }
}
