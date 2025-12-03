package zwuiix.colria.discord;

import cn.nukkit.Server;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import zwuiix.colria.discord.cmd.DiscordCommandRegistry;
import zwuiix.colria.discord.task.ActivityTask;
import zwuiix.colria.discord.task.PlayerChannelTask;

import java.util.Arrays;

public class DiscordAPI {
    private static DiscordAPI INSTANCE;

    public static DiscordAPI getInstance() {
        return INSTANCE;
    }

    @Getter
    private final JDA jda;

    public DiscordAPI(String token) {
        INSTANCE = this;
        this.jda = JDABuilder.createDefault(token)
                .setEnableShutdownHook(false)
                .enableIntents(Arrays.stream(GatewayIntent.values()).toList())
                .addEventListeners(new DiscordListener())
                .build();
    }

    public void connect() {
        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        this.jda.setAutoReconnect(true);
        this.jda.getPresence().setStatus(OnlineStatus.IDLE);
        Server.getInstance().getLogger().info("Discord bot connected as " + this.jda.getSelfUser().getAsTag());

        DiscordCommandRegistry registry = DiscordCommandRegistry.getInstance();
        registry.injects(this.jda);
        Server.getInstance().getLogger().info("Registered " + registry.getCommands().size() + " commands.");

        var server = Server.getInstance();
        var scheduler = server.getScheduler();

        scheduler.scheduleRepeatingTask(new ActivityTask(), 20 * 15);
        scheduler.scheduleRepeatingTask(new PlayerChannelTask(), 20 * 60);
    }

    public void disconnect() {
        try {
            this.jda.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
