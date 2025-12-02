package zwuiix.colria.discord.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import zwuiix.colria.discord.DiscordAPI;
import zwuiix.colria.discord.ServerInfo;

public class PlayerChannelTask extends Task {
    @Override
    public void onRun(int i) {
        var jda = DiscordAPI.getInstance().getJda();
        var guild = jda.getGuildById(ServerInfo.GUILD_ID);

        if(guild == null)
            return;

        var voiceChannel = guild.getVoiceChannelById(ServerInfo.PLAYERS_CHANNEL_ID);
        if (voiceChannel == null)
            return;

        int playerCount = Server.getInstance().getOnlinePlayersCount();

        String currentName = voiceChannel.getName();
        String newName;
        if (!currentName.matches(".*\\d+.*"))
            return;

        newName = currentName.replaceAll("\\d+", String.valueOf(playerCount));
        if(newName.equals(currentName))
            return;

        voiceChannel.getManager().setName(newName).queue();
        Server.getInstance().getLogger().info("Updated players channel name to " + newName);
    }
}
