package zwuiix.colria.cmd.impl.link;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.IntegerArgument;
import zwuiix.colria.discord.DiscordAPI;
import zwuiix.colria.link.LinkManager;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class LinkCommand extends ColriaPlayerCommand {
    public LinkCommand() {
        super("link", "commands.link.description");
    }

    @Override
    public void prepare() {
        registerArgument(0, new IntegerArgument("code", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var code = args.get("code").toString();
        var manager = LinkManager.getInstance();

        var id = player.getPlayerDataInfo().getDiscordId();
        if(!id.isEmpty()) {
            DiscordAPI.getInstance().getJda().retrieveUserById(id).queue(
                    user -> player.sendMessage(TranslationKeys.PLAYER_COMMAND_LINK_ALREADYLINKED, user.getEffectiveName()),
                    throwable -> player.sendMessage(TranslationKeys.PLAYER_COMMAND_LINK_ALREADYLINKED, "N/A")
            );
            return;
        }

        if(!manager.exists(code)) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_LINK_INVALID_CODE, code);
            return;
        }

        var info = manager.fromCode(code);
        DiscordAPI.getInstance().getJda().retrieveUserById(info.discordId()).queue(user -> {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_LINK_SUCCESS, user.getEffectiveName());
            player.getPlayerDataInfo().setDiscordId(info.discordId());
            manager.remove(code);

            info.hook().editOriginal("Votre compte a été lié avec succès au compte minecraft : " + player.getUsername()).queue();
            user.openPrivateChannel().queue(channel -> channel.sendMessage("Votre compte a été lié avec succès au compte xbox : " + player.getUsername()).queue());
        }, throwable -> {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_LINK_NOTFOUND);
        });
    }
}
