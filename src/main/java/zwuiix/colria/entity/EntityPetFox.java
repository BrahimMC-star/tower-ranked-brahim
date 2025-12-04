package zwuiix.colria.entity;

import cn.nukkit.entity.passive.EntityFox;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetFox extends EntityPetWalking {
    public EntityPetFox(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityFox.NETWORK_ID;
    }

    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }

    @Override
    public float getPetSpeed() { return 0.8f; }

    @Override
    public double getSpeed() { return 1.4D; }

    @Override
    public Vector3f getSeatPosition() {
        return new Vector3f(0, 1.85001f, 0);
    }
}
