package zwuiix.colria.discord.cmd;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

abstract public class DiscordCommand {
    protected final String name;

    public DiscordCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract public SlashCommandData getSlashCommandData();
    abstract public void execute(SlashCommandInteractionEvent ev);
}
