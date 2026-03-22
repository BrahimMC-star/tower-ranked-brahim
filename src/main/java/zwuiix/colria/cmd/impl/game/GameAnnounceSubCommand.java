package zwuiix.colria.cmd.impl.game;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.discord.DiscordUtil;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameAnnounceSubCommand extends ColriaPlayerSubCommand {
    public GameAnnounceSubCommand() {
        super("announce");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if(!game.getHoster().equalsIgnoreCase(player.getName()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT_OWNER);
            return;
        }

        if(!game.getState().equals(Game.State.LOBBY)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_START_ALREADY);
            return;
        }

        if(game.isPrivate() && game.getWhitelist().isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_PRIVATE_EMPTY);
            return;
        }

        var cd = player.getCooldown("game_announce");
        if(!cd.isExpired() && !player.inAdminMode()) {
            var secs = cd.getRemainingTime() / 1000;
            long minutes = secs / 60;
            long seconds = secs % 60;

            String format = String.format("%dm%02d", minutes, seconds);
            if(minutes == 0) {
                format = String.format("%02ds", secs);
            }


            player.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_COOLDOWN, format);
            return;
        }

        if(game.isAnnounced() && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_ALREADY);
            return;
        }

        game.setAnnounced(true);
        cd.refresh(300); // 5 minutes cooldown
        player.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_SUCCESS);
        for (Lobby lobby : GameRegistry.getInstance().getLobbies().values()) {
            lobby.global(p -> {
                if(!p.isInLobby()) return;
                if(game.getBlacklist().contains(player.getName().toLowerCase())) return;
                if(game.isPrivate() && !game.getWhitelist().contains(player.getName().toLowerCase()) && !player.inAdminMode()) return;
                p.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_BROADCAST, game.getName(), game.getHoster(), game.getIdentifier());
            });
        }

        sendDiscordAnnouncement(game);
    }

    private void sendDiscordAnnouncement(Game game) {
        var guild = DiscordUtil.getGuild().orElse(null);
        if(guild == null) return;

        var channel = guild.getTextChannelById(DiscordUtil.ALERTS_CHANNEL_ID);
        if(channel == null) return;

        Container container = Container.of(
                TextDisplay.ofFormat(String.format("## Nouvelle partie de __%s__", game.getName())),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.ofFormat("**Hôte :** %s", game.getHoster()),
                TextDisplay.ofFormat("**ID :** %s", game.getIdentifier()),
                TextDisplay.ofFormat("**Type :** %s", game.isPrivate() ? "Privée" : "Publique"),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(
                        Button.secondary("join:" + game.getGameId(), "Rejoindre")
                )
        );
        channel.sendMessage("<@&" + DiscordUtil.ALERTS_CHANNEL_ID + ">").queue((message) -> {
            channel.sendMessageComponents(container).setMessageReference(message).useComponentsV2().queue();
        });
    }
}
