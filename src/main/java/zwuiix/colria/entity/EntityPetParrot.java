package zwuiix.colria.entity;

import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.passive.EntityParrot;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityPetParrot extends EntityPetFlying {

    private static final int[] VARIANTS = {0, 1, 2, 3, 4};

    public EntityPetParrot(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityParrot.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 0.9f;
    }

    @Override
    public float getPetSpeed() { return 0.6f; }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setDataProperty(new IntEntityData(DATA_VARIANT, getRandomVariant()));
    }

    private static int getRandomVariant() {
        return VARIANTS[Utils.rand(0, VARIANTS.length - 1)];
    }
}
