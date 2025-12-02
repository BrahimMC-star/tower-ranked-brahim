package zwuiix.colria.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import zwuiix.colria.discord.cmd.DiscordCommandRegistry;

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
}
