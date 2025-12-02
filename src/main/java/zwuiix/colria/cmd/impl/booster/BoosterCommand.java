package zwuiix.colria.cmd.impl.booster;

import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoosterCommand extends ColriaPlayerCommand {
    public BoosterCommand() {
        super("booster", "commands.booster.description");
    }

    @Override
    public void prepare() {
        registerSubCommand(new BoosterQueueSubCommand());

        registerSubCommand(new BoosterShowSubCommand());
        registerSubCommand(new BoosterAddSubCommand());
        registerSubCommand(new BoosterRemoveSubCommand());
        registerSubCommand(new BoosterSetSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var manager = BoosterManager.getInstance();
        var current = manager.getCurrent();
        var queues = manager.getQueue();

        if(current == null && !queues.isEmpty()) {
            var next = queues.getFirst();
            player.sendMessage(TranslationKeys.BOOSTER_QUEUE_STARTING, next.getType(), next.getOwner(), queues.size());
            return;
        }

        if(current == null) {
            player.sendMessage(TranslationKeys.BOOSTER_QUEUE_EMPTY_CURRENT);
            return;
        }

        if(queues.isEmpty()) {
            player.sendMessage(TranslationKeys.BOOSTER_QUEUE_EMPTY, current.booster().getType(), current.booster().getOwner());
            return;
        }

        var owners = new HashMap<String, Integer>();
        for(var booster : queues) {
            owners.put(booster.getOwner(), owners.getOrDefault(booster.getOwner(), 0) + 1);
        }

        var ownerList = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : owners.entrySet()) {
            String k = entry.getKey();
            Integer v = entry.getValue();

            ownerList.add(v > 1 ? "x" + v + " " + k : k);
        }

        player.sendMessage(TranslationKeys.BOOSTER_QUEUE,
                current.booster().getType(),
                current.booster().getOwner(),
                manager.getQueue().size(),
                String.join("&r&7, ", ownerList)
        );
    }
}
