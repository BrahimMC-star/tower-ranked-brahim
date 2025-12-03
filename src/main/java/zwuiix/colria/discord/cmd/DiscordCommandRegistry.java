package zwuiix.colria.discord.cmd;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import zwuiix.colria.discord.cmd.impl.LinkCommand;
import zwuiix.colria.discord.cmd.impl.ProfilCommand;

import java.util.LinkedHashMap;
import java.util.Optional;

@Getter
public class DiscordCommandRegistry {
    private static final DiscordCommandRegistry INSTANCE = new DiscordCommandRegistry();
    public static DiscordCommandRegistry getInstance() {
        return INSTANCE;
    }

    private final LinkedHashMap<String, DiscordCommand> commands = new LinkedHashMap<>();
    private DiscordCommandRegistry() {
        register(new LinkCommand());
        register(new ProfilCommand());
    }

    public Optional<DiscordCommand> getCommand(String command) {
        return Optional.ofNullable(commands.get(command.toLowerCase()));
    }

    public void register(DiscordCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public void injects(JDA jda) {
        CommandListUpdateAction commands = jda.updateCommands();
        for (DiscordCommand command : this.commands.values()) {
            commands = commands.addCommands(command.getSlashCommandData());
        }

        commands.queue();
    }
}
