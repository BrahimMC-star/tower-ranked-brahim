package zwuiix.colria.game.impl.towerbridge;

import zwuiix.colria.game.impl.team.TeamGameParameters;
import zwuiix.colria.game.impl.tower.TowerGameParameters;

public class TowerBridgeGameParameters extends TowerGameParameters
{
    public TowerBridgeGameParameters() {
        super();
        appleGenerator = 15;
        maxPoints = 5;
        maxPlayers = 4;
        minPlayers = 1;
    }
}