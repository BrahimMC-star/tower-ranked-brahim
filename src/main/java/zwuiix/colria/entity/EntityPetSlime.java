package zwuiix.colria.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntitySlime;
import cn.nukkit.level.Sound;
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
    public double getSpeed() { return 1.2D; }

    @Override
    public Vector3f getSeatPosition() {
        return switch (size) {
            case 1 -> new Vector3f(0, 1.2f, 0);
            case 2 -> new Vector3f(0, 1.6f, 0);
            case 3 -> new Vector3f(0, 2.2f, 0);
            default -> new Vector3f(0, 2f, 0);
        };
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.size = Utils.rand(1, 3);
        this.setScale(0.51f + size * 0.51f);
    }

    @Override
    public void attackEntity(Entity entity) {}

    @Override
    public void resetFallDistance() {
        super.resetFallDistance();
        this.getLevel().addSound(this, Sound.FALL_SLIME, 0.5f, 1f);
    }
}
