package zwuiix.colria.entity;

import cn.nukkit.entity.mob.EntityRavager;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetRavager extends EntityPetWalking {
    public EntityPetRavager(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityRavager.NETWORK_ID;
    }

    @Override
    public float getHeight() {
        return 2.2f;
    }

    @Override
    public float getWidth() {
        return 1.95f;
    }

    @Override
    public float getPetSpeed() { return 0.6f; }

    @Override
    public double getSpeed() { return 1.1D; }

    @Override
    public Vector3f getSeatPosition() {
        return new Vector3f(0, 1.85001f, 0);
    }
}
