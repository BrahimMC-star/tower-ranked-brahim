package zwuiix.colria.entity;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.entity.mob.EntitySnowGolem;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;

import java.util.HashMap;

public class EntityPetSnowman extends EntityPetWalking {
    private HashMap<String, TaskHandler> snowLayers = new HashMap<>();

    public EntityPetSnowman(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntitySnowGolem.NETWORK_ID;
    }

    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public float getPetSpeed() { return 1.3f; }

    @Override
    public boolean onUpdate(int currentTick) {
        var onUpdate = super.onUpdate(currentTick);
        if(onUpdate && this.isOnGround()) {
            Block block = this.level.getBlock(this);
            if(block instanceof BlockAir) {
                this.level.setBlock(block, Block.get(Block.SNOW_LAYER), true, true);
            }

            String key = block.getX() + "," + block.getY() + "," + block.getZ();
            if(snowLayers.containsKey(key)) {
                this.snowLayers.get(key).cancel();
            }

            snowLayers.put(key, this.server.getScheduler().scheduleDelayedTask(() -> {
                Block b = level.getBlock(new Vector3(block.getX(), block.getY(), block.getZ()));
                if(b.getId() == Block.SNOW_LAYER) {
                    this.level.setBlock(block, Block.get(Block.AIR), true, true);
                }
            }, 20 * 8));
        }
        return onUpdate;
    }

    @Override
    public void close() {
        super.close();
        for(TaskHandler task : snowLayers.values()) {
            task.cancel();
        }
        snowLayers.clear();
    }
}
