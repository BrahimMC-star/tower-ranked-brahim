package zwuiix.colria.game.impl.lobby;

import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameKit;
import zwuiix.colria.game.GameLevelGenerator;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.kit.LobbyKit;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;

public class Lobby extends Game {
    public Lobby() {
        super("Lobby", null);
        this.identifier = String.valueOf(GameRegistry.getInstance().getLobbies().size() + 1);
        setPrivate(false);
        setWaitingLevel(new GameLevelGenerator("lobby").create(getIdentifier()));
        setGameLevel(getWaitingLevel());

        setState(State.LOBBY);
        new LobbyListener(this);
    }

    @Override
    public void prepare() {}

    @Override
    public void join(EnginePlayer player) {
        super.join(player);

        if(player.logged) {
            player.syncRanks();
            player.updateCape(player.getPlayerDataInfo().getCape());

            for (String identifier : player.getPlayerDataInfo().getCosmetics()) {
                if(!player.hasCosmetic(identifier)) continue;

                var cosmetic = CosmeticRegistry.getInstance().getCosmetic(identifier);
                if(cosmetic != null) {
                    cosmetic.apply(player);
                }
            }
        }
    }

    @Override
    public GameKit getLobbyKit() {
        return LobbyKit.INSTANCE;
    }
}
