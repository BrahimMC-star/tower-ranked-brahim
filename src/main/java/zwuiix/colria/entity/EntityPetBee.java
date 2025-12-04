package zwuiix.colria.entity;

import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.passive.EntityBee;
import cn.nukkit.entity.passive.EntityParrot;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityPetBee extends EntityPetFlying {
    public EntityPetBee(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityBee.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.275f;
        }
        return 0.55f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.25f;
        }
        return 0.5f;
    }

    @Override
    public float getPetSpeed() { return 0.6f; }
}
