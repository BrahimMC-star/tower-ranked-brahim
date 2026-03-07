package zwuiix.colria.entity;

import cn.nukkit.entity.mob.EntityWolf;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetWolf extends EntityPetWalking {
    public EntityPetWolf(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityWolf.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.8f;
    }

    @Override
    public float getPetSpeed() { return 0.8f; }

    @Override
    public double getSpeed() { return 1.6D; }
}
