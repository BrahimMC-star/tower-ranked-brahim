package zwuiix.colria.discord.cmd.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import zwuiix.colria.discord.cmd.DiscordCommand;
import zwuiix.colria.link.LinkManager;

public class LinkCommand extends DiscordCommand {
    public LinkCommand() { super("link"); }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash(name, "Links a user's account.")
                .setNameLocalization(DiscordLocale.FRENCH, "lier")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Lie le compte d'un utilisateur.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent ev) {
        ev.deferReply().setEphemeral(true).queue();
        var user = ev.getUser();
        var manager = LinkManager.getInstance();

        if(manager.is(user.getId())) {
            var code = manager.getCode(user.getId());
            ev.getHook().editOriginal("Votre code est : `" + code + "`").queue();
        } else {
            var code = manager.generateCode(user.getId());
            manager.add(code, new LinkManager.LinkInfo(user.getId(), ev.getHook()));
            ev.getHook().editOriginal("Votre code est : `" + code + "`").queue();
        }
    }
}
