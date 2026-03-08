package zwuiix.colria.game.impl.tower;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.scheduler.Task;
import org.apache.commons.collections4.Closure;
import zwuiix.colria.inventory.InventoryHooker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TowerMapProcessor {
    private final int size;
    private final Level level;
    private final Vector3 spawnA;
    private final Vector3 spawnB;

    private final Map<Block, Block> teamABlockMap;
    private final Map<Block, Block> teamBBlockMap;

    private final Closure<Integer> updatePercentage;
    private final Runnable finish;

    private static class BlockChange {
        Vector3f position;
        Block newBlock;
    }

    public TowerMapProcessor(int size, Level level, Vector3 spawnA, Vector3 spawnB,
                             Map<Block, Block> teamABlockMap,
                             Map<Block, Block> teamBBlockMap,
                             Closure<Integer> updatePercentage, Runnable finish) {
        this.size = size;
        this.level = level;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
        this.teamABlockMap = teamABlockMap;
        this.teamBBlockMap = teamBBlockMap;
        this.updatePercentage = updatePercentage;
        this.finish = finish;
    }

    public void run() {
        int halfSize = size / 2;

        List<BlockChange> changes = new ArrayList<>();

        for (int x = -halfSize; x < halfSize; x++) {
            for (int z = -halfSize; z < halfSize; z++) {
                Vector3 posA = spawnA.add(x, 0, z);
                Block currentA = level.getBlock(posA);

                for (Map.Entry<Block, Block> entry : teamABlockMap.entrySet()) {
                    Block key = entry.getKey();
                    if (currentA.getId() == key.getId() && currentA.getDamage() == key.getDamage()) {
                        BlockChange bc = new BlockChange();
                        bc.position = new Vector3f((float) posA.x, (float) posA.y, (float) posA.z);
                        bc.newBlock = entry.getValue();
                        changes.add(bc);
                    }
                }

                Vector3 posB = spawnB.add(x, 0, z);
                Block currentB = level.getBlock(posB);

                for (Map.Entry<Block, Block> entry : teamBBlockMap.entrySet()) {
                    Block key = entry.getKey();
                    if (currentB.getId() == key.getId() && currentB.getDamage() == key.getDamage()) {
                        BlockChange bc = new BlockChange();
                        bc.position = new Vector3f((float) posB.x, (float) posB.y, (float) posB.z);
                        bc.newBlock = entry.getValue();
                        changes.add(bc);
                    }
                }
            }
        }

        int chunkSize = 16;
        AtomicInteger processed = new AtomicInteger(0);
        var plugin = InventoryHooker.getInstance().getPlugin();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int index = 0;

            @Override
            public void onRun(int currentTick) {
                for (int i = 0; i < chunkSize && index < changes.size(); i++, index++) {
                    BlockChange bc = changes.get(index);
                    Vector3 pos = new Vector3(bc.position.x, bc.position.y, bc.position.z);
                    level.setBlock(pos, bc.newBlock);
                    processed.incrementAndGet();
                }

                int percent = (int) ((processed.get() / (float) changes.size()) * 100);
                updatePercentage.execute(percent);

                if (index >= changes.size()) {
                    finish.run();
                    cancel();
                }
            }
        }, 1);
    }
}
