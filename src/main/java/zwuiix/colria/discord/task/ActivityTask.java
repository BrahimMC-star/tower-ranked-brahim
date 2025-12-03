package zwuiix.colria.discord.task;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.discord.DiscordAPI;
import zwuiix.colria.game.GameRegistry;

import java.util.Comparator;

public class ActivityTask extends Task {
    private int index = 0;

    @Override
    public void onRun(int i) {
        var jda = DiscordAPI.getInstance().getJda();
        var server = Server.getInstance();

        if(jda.getStatus().equals(JDA.Status.SHUTTING_DOWN) || jda.getStatus().equals(JDA.Status.FAILED_TO_LOGIN)) {
            server.getLogger().warning("Discord bot disconnected. Attempting to reconnect...");
            return;
        }

        String protocolsRange = ProtocolInfo.SUPPORTED_PROTOCOLS.stream()
                .min(Comparator.naturalOrder())
                .map(min -> {
                    int max = ProtocolInfo.SUPPORTED_PROTOCOLS.stream()
                            .max(Comparator.naturalOrder())
                            .orElse(min);
                    return Utils.getVersionByProtocol(min) + " - " + Utils.getVersionByProtocol(max);
                })
                .orElse("n/a");

        String[] statuses = new String[] {
                Server.getInstance().getOnlinePlayers().size() + " joueurs en ligne.",
                GameRegistry.getInstance().getGames().size() + " parties en cours.",
                BoosterManager.getInstance().getCurrent() == null ?
                        "Aucun booster actif." :
                        "Booster de " + BoosterManager.getInstance().getCurrent().booster().owner() + " actif.",
                "Versions supportées " + protocolsRange,
                "Colria " + EngineInfo.VERSION,
                "Nukkit " + Server.getInstance().getApiVersion(),
        };

        index++;
        if(index >= statuses.length) index = 0;
        String status = statuses[index];
        jda.getPresence().setActivity(Activity.playing(status));
    }
}
