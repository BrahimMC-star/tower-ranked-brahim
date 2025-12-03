package zwuiix.colria.discord.cmd;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Getter
abstract public class DiscordCommand {
    protected final String name;

    public DiscordCommand(String name) {
        this.name = name;
    }

    abstract public SlashCommandData getSlashCommandData();
    abstract public void execute(SlashCommandInteractionEvent ev);
}
