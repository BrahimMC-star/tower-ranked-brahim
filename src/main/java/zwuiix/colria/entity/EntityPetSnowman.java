package zwuiix.colria.entity;

import cn.nukkit.entity.mob.EntitySnowGolem;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetSnowman extends EntityPetWalking {
    public EntityPetSnowman(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntitySnowGolem.NETWORK_ID;
    }

    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public float getPetSpeed() { return 1.3f; }
}
