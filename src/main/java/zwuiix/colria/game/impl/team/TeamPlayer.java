package zwuiix.colria.game.impl.team;

import zwuiix.colria.game.Game;
import zwuiix.colria.game.GamePlayer;

public class TeamPlayer extends GamePlayer {
    private Team team;

    public TeamPlayer(String username, Game game, Team team) {
        super(username, game);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
