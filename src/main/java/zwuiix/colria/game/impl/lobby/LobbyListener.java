package zwuiix.colria.game.impl.lobby;

import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.GameEvent;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.rank.Rank;
import zwuiix.colria.util.Chat;

import java.util.HashSet;

public record LobbyListener(Lobby game) {
    public LobbyListener(Lobby game) {
        this.game = game;

        GameEvent.subscribe(game, PlayerChatEvent.class, this::onChat);
    }

    private void onChat(PlayerChatEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        if(!p.isInLobby()) return;

        ev.setRecipients(new HashSet<>(p.getLevel().getPlayers().values()));

        Rank rank = p.getHighestRank();
        String color = rank.getColor();

        ev.setMessage(TextFormat.colorize(color) + Chat.clean(ev.getMessage()));
        if(!p.isOp()) ev.setMessage(TextFormat.clean(ev.getMessage()));

        if(rank.isDefault()) {
            ev.setFormat(TextFormat.colorize("&7" + p.getName() + " " + EngineInfo.SUFFIX + " &r&7" + ev.getMessage()));
        } else {
            ev.setFormat(TextFormat.colorize("&8[&r" + rank.getColoredName() + "&8] &r" + color + p.getName() + " " + EngineInfo.SUFFIX + " &r" + ev.getMessage()));
        }
    }
}
