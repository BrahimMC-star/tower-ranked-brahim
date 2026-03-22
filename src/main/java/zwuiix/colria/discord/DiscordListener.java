package zwuiix.colria.discord;

import cn.nukkit.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import zwuiix.colria.discord.cmd.DiscordCommandRegistry;
import zwuiix.colria.util.DB;

public class DiscordListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var commandName = event.getName();
        var commandOpt = DiscordCommandRegistry.getInstance().getCommand(commandName);
        if (commandOpt.isEmpty()) {
            event.reply("Unknown command.").setEphemeral(true).queue();
            return;
        }

        var command = commandOpt.get();
        command.execute(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null || guild.getId().equals(DiscordUtil.GUILD_ID)) {
            event.reply(DiscordUtil.WARN + "Vous ne pouvez pas interagir avec ce bouton.").setEphemeral(true).queue();
            return;
        }

        var member = event.getMember();

        var button = event.getButton();
        var uniqueId = button.getCustomId();
        if (uniqueId != null && uniqueId.contains("join:")) {
            var gameId = uniqueId.split(":")[1];
            DB.getPlayerDataInfoFromDiscordId(member.getId()).then((playerData) -> {
                if (playerData == null) {
                    event.reply(DiscordUtil.ERROR + "Aucun compte lié trouvé.").setEphemeral(true).queue();
                    return;
                }

                var username = playerData.getName();
                var player = Server.getInstance().getPlayer(username);

                player.chat("/game join " + gameId);
                event.reply(DiscordUtil.SUCCESS + "Tentative de rejoindre la partie...").setEphemeral(true).queue();
            }).onCatch(throwable -> {
                event.reply(DiscordUtil.ERROR + "Votre compte n'est pas lié ou une erreur s'est produite.").setEphemeral(true).queue();
            });
        }
    }
}
