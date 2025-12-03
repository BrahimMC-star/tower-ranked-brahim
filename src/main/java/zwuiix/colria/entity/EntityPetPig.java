package zwuiix.colria.entity;

import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetPig extends EntityPet {
    public EntityPetPig(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public Vector3f getSeatPosition() {
        return new Vector3f(0, 1.85001f, 0);
    }

    @Override
    public int getNetworkId() {
        return EntityPig.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 0.9f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 0.9f;
    }
}
