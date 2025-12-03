package zwuiix.colria.entity;

import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPetPig extends EntityPet implements EntityClimateVariant {
    public EntityPetPig(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
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
