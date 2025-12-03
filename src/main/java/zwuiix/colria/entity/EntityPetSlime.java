package zwuiix.colria.entity;

import cn.nukkit.entity.mob.EntitySlime;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityPetSlime extends EntityPetJumping {
    public EntityPetSlime(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    protected int size;

    @Override
    public int getNetworkId() {
        return EntitySlime.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getHeight() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getLength() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getPetSpeed() { return 0.8f; }

    @Override
    public Vector3f getSeatPosition() {
        return new Vector3f(0, 1.85001f, 0);
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.size = Utils.rand(6, 12);
        this.setScale(0.51f + size * 0.51f);
    }
}
