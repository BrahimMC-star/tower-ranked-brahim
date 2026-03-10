package zwuiix.colria.task;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.Collections;

public class BroadcastTask extends Task {
    private int index = 0;
    private final ArrayList<BossbarData> bossbarDataList = new ArrayList<>();

    private record BossbarData(TranslationKeys key, int duration) {}

    public BroadcastTask() {
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_MEDIA, 20 * 14));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_MEDIA2, 20 * 14));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_DISCORD, 20 * 16));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_COMMUNITY, 20 * 16));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_REWARD, 20 * 10));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_REWARD2, 20 * 10));
        bossbarDataList.add(new BossbarData(TranslationKeys.BROADCAST_REWARD3, 20 * 10));

        Collections.shuffle(bossbarDataList);
    }

    @Override
    public void onRun(int i) {
        if (index >= bossbarDataList.size()) {
            index = 0;
        }
        BossbarData data = bossbarDataList.get(index);
        var key = data.key();
        var duration = data.duration();
        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
            EnginePlayer player = (EnginePlayer) p;
            player.playBossBar(player.processTranslation(key), duration);
        }
        index++;
    }
}
