package zwuiix.colria.game.impl.team;


import cn.nukkit.math.Vector3;

import javax.annotation.Nullable;

public record TeamSpawnPoint(Vector3 first, Vector3 second, @Nullable Float fyaw, @Nullable Float syaw)
{
}