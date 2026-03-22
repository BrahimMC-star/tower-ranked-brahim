package zwuiix.colria.game.impl.tower;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.Plugin;
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
    private final Vector3 spawnA, spawnB;

    private final Map<BlockKey, Block> teamABlocks;
    private final Map<BlockKey, Block> teamBBlocks;

    private final Closure<Integer> updatePercentage;
    private final Runnable finish;

    private static final int MIN_Y = -10;
    private static final int MAX_Y = 80;

    private static class BlockChange {
        int x, y, z;
        Block block;
    }

    public record BlockKey(int id, int meta, Class<? extends Block> type) {
        public static BlockKey of(Block block) {
            return new BlockKey(block.getId(), block.getDamage(), block.getClass());
        }
    }

    public TowerMapProcessor(
            int size,
            Level level,
            Vector3 spawnA,
            Vector3 spawnB,
            Map<BlockKey, Block> teamABlocks,
            Map<BlockKey, Block> teamBBlocks,
            Closure<Integer> updatePercentage,
            Runnable finish
    ) {
        this.size = size;
        this.level = level;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
        this.teamABlocks = teamABlocks;
        this.teamBBlocks = teamBBlocks;
        this.updatePercentage = updatePercentage;
        this.finish = finish;

    }
    public void run() {
        int half = size / 2;
        int spawnAX = spawnA.getFloorX();
        int spawnAZ = spawnA.getFloorZ();
        int spawnBX = spawnB.getFloorX();
        int spawnBZ = spawnB.getFloorZ();

        List<BlockChange> changes = new ArrayList<>();
        int totalBlocks = size * size * (MAX_Y - MIN_Y + 1) * 2;
        int scanTicks = 50;
        int blocksPerTick = Math.max(1000, totalBlocks / scanTicks);
        AtomicInteger scanned = new AtomicInteger();
        Plugin plugin = InventoryHooker.getInstance().getPlugin();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int x = -half;
            int z = -half;
            int y = MIN_Y;

            @Override
            public void onRun(int tick) {
                int processed = 0;

                while (processed < blocksPerTick) {
                    if (x >= half) {
                        runReplacement(changes, plugin);
                        cancel();
                        return;
                    }

                    int ax = spawnAX + x;
                    int az = spawnAZ + z;
                    int bx = spawnBX + x;
                    int bz = spawnBZ + z;

                    Block blockA = level.getBlock(ax, y, az, true);
                    Block replacementA = teamABlocks.get(BlockKey.of(blockA));
                    if (replacementA != null) {
                        BlockChange bc = new BlockChange();
                        bc.x = ax; bc.y = y; bc.z = az; bc.block = replacementA;
                        changes.add(bc);
                    }

                    Block blockB = level.getBlock(bx, y, bz, true);
                    Block replacementB = teamBBlocks.get(BlockKey.of(blockB));
                    if (replacementB != null) {
                        BlockChange bc = new BlockChange();
                        bc.x = bx; bc.y = y; bc.z = bz; bc.block = replacementB;
                        changes.add(bc);
                    }

                    processed++;
                    scanned.incrementAndGet();

                    y++;
                    if (y > MAX_Y) { y = MIN_Y; z++; }
                    if (z >= half) { z = -half; x++; }
                }

                int percent = (int)((scanned.get() / (float) totalBlocks) * 50);
                updatePercentage.execute(percent);
            }
        }, 1);
    }

    private void runReplacement(List<BlockChange> changes, Plugin plugin) {
        int total = changes.size();
        int replaceTicks = 100;
        int perTick = Math.max(50, total / replaceTicks);
        AtomicInteger processed = new AtomicInteger();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int index = 0;

            @Override
            public void onRun(int tick) {
                int count = 0;
                while (count < perTick && index < total) {
                    BlockChange bc = changes.get(index);
                    level.setBlock(bc.x, bc.y, bc.z, bc.block, true, true);
                    index++; count++; processed.incrementAndGet();
                }

                int percent = 50 + (int)((processed.get() / (float) total) * 50);
                updatePercentage.execute(percent);

                if (index >= total) {
                    finish.run();
                    cancel();
                }
            }
        }, 1);
    }
}