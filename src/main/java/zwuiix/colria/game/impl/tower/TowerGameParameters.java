package zwuiix.colria.game.impl.tower;

import zwuiix.colria.game.impl.team.TeamGameParameters;

public class TowerGameParameters extends TeamGameParameters {
    public boolean glint = false;
    public int appleGenerator = 30;
    public int maxPoints = 10;
    public int despawnBlocks = 10;
    public MarkReward markReward = MarkReward.ALL;
    public int regen = 20;
    public int apple = 1;
    public int blocks = 16;
    public DespawnAnimation despawnAnimation = DespawnAnimation.QUICK;

    public enum MarkReward {
        REGEN,
        APPLE,
        BLOCKS,
        REGEN_AND_APPLE,
        REGEN_AND_BLOCKS,
        APPLE_AND_BLOCKS,
        ALL,
    }

    public enum DespawnAnimation {
        INSTANT,
        QUICK,
        PROGRESSIVE,
    }
}