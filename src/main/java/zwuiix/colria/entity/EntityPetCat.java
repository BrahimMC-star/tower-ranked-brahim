package zwuiix.colria.entity;

import cn.nukkit.entity.passive.EntityCat;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetCat extends EntityPetWalking {
    public EntityPetCat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityCat.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.3f;
        }
        return 0.6f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.35f;
        }
        return 0.7f;
    }

    @Override
    public float getPetSpeed() { return 0.8f; }

    @Override
    public double getSpeed() { return 1.8D; }

    @Override
    public Vector3f getSeatPosition() {
        return new Vector3f(0, 1.85001f, 0);
    }
}
