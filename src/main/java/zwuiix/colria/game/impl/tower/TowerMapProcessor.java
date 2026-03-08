package zwuiix.colria.game.impl.tower;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.Task;
import org.apache.commons.collections4.Closure;
import zwuiix.colria.inventory.InventoryHooker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TowerMapProcessor {

    private final int size;
    private final Level level;
    private final Vector3 spawnA, spawnB;

    private final Map<Integer, Block> teamABlocks;
    private final Map<Integer, Block> teamBBlocks;

    private final Closure<Integer> updatePercentage;
    private final Runnable finish;

    private static final int HEIGHT = 80;

    private static class BlockChange {
        int x, y, z;
        Block block;
    }

    public TowerMapProcessor(
            int size,
            Level level,
            Vector3 spawnA,
            Vector3 spawnB,
            Map<Block, Block> teamABlockMap,
            Map<Block, Block> teamBBlockMap,
            Closure<Integer> updatePercentage,
            Runnable finish
    ) {
        this.size = size;
        this.level = level;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
        this.updatePercentage = updatePercentage;
        this.finish = finish;

        this.teamABlocks = convert(teamABlockMap);
        this.teamBBlocks = convert(teamBBlockMap);
    }

    private Map<Integer, Block> convert(Map<Block, Block> map) {
        Map<Integer, Block> result = new HashMap<>();

        for (Map.Entry<Block, Block> entry : map.entrySet()) {
            Block key = entry.getKey();
            int k = (key.getId() << 8) | key.getDamage();
            result.put(k, entry.getValue());
        }

        return result;
    }

    public void run() {
        System.out.println("[TowerMapProcessor] Start scanning");

        int half = size / 2;

        int spawnAX = spawnA.getFloorX();
        int spawnAY = spawnA.getFloorY();
        int spawnAZ = spawnA.getFloorZ();

        int spawnBX = spawnB.getFloorX();
        int spawnBY = spawnB.getFloorY();
        int spawnBZ = spawnB.getFloorZ();

        List<BlockChange> changes = new ArrayList<>();

        int totalBlocks = size * size * HEIGHT * 2;

        int scanTicks = 200;
        int blocksPerTick = Math.max(1000, totalBlocks / scanTicks);

        AtomicInteger scanned = new AtomicInteger();

        Plugin plugin = InventoryHooker.getInstance().getPlugin();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int x = -half;
            int y = 0;
            int z = -half;

            @Override
            public void onRun(int tick) {
                int processed = 0;
                while (processed < blocksPerTick) {
                    if (x >= half) {
                        System.out.println("[TowerMapProcessor] Scan finished. Found " + changes.size() + " blocks");
                        runReplacement(changes, plugin);
                        cancel();
                        return;
                    }

                    int ax = spawnAX + x;
                    int ay = spawnAY + y;
                    int az = spawnAZ + z;

                    Block blockA = level.getBlock(ax, ay, az, true);
                    int keyA = (blockA.getId() << 8) | blockA.getDamage();

                    System.out.println("[TowerMapProcessor] Scanning block at (" + ax + ", " + ay + ", " + az + ") - ID: " + blockA.getId() + ", Damage: " + blockA.getDamage());

                    Block replaceA = teamABlocks.get(keyA);
                    if (replaceA != null) {
                        BlockChange bc = new BlockChange();
                        bc.x = ax;
                        bc.y = ay;
                        bc.z = az;
                        bc.block = replaceA;

                        changes.add(bc);
                    }

                    int bx = spawnBX + x;
                    int by = spawnBY + y;
                    int bz = spawnBZ + z;

                    Block blockB = level.getBlock(bx, by, bz, true);
                    int keyB = (blockB.getId() << 8) | blockB.getDamage();

                    System.out.println("[TowerMapProcessor] Scanning block at (" + bx + ", " + by + ", " + bz + ") - ID: " + blockB.getId() + ", Damage: " + blockB.getDamage());

                    Block replaceB = teamBBlocks.get(keyB);
                    if (replaceB != null) {
                        BlockChange bc = new BlockChange();
                        bc.x = bx;
                        bc.y = by;
                        bc.z = bz;
                        bc.block = replaceB;

                        changes.add(bc);
                    }

                    processed++;
                    scanned.incrementAndGet();

                    z++;
                    if (z >= half) {
                        z = -half;
                        y++;

                        if (y >= HEIGHT) {
                            y = 0;
                            x++;
                        }
                    }
                }

                int percent = (int)((scanned.get() / (float) totalBlocks) * 50);

                updatePercentage.execute(percent);

                if (tick % 20 == 0) {
                    System.out.println("[TowerMapProcessor] Scan progress: " + percent + "%");
                }
            }
        }, 1);
    }

    private void runReplacement(List<BlockChange> changes, Plugin plugin) {
        System.out.println("[TowerMapProcessor] Start replacement");

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

                    index++;
                    count++;

                    processed.incrementAndGet();
                }

                int percent = 50 + (int)((processed.get() / (float) total) * 50);
                updatePercentage.execute(percent);

                if (tick % 20 == 0) {
                    System.out.println("[TowerMapProcessor] Replace progress: " + percent + "%");
                }

                if (index >= total) {
                    System.out.println("[TowerMapProcessor] Map ready");
                    finish.run();
                    cancel();
                }
            }

        }, 1);
    }
}