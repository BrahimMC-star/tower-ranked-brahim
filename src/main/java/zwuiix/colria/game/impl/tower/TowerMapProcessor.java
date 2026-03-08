package zwuiix.colria.game.impl.tower;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
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

    private static final int HEIGHT = 128;

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
        Plugin plugin = InventoryHooker.getInstance().getPlugin();

        int half = size / 2;

        int spawnAX = spawnA.getFloorX();
        int spawnAZ = spawnA.getFloorZ();

        int spawnBX = spawnB.getFloorX();
        int spawnBZ = spawnB.getFloorZ();

        int minChunkX = Math.min(spawnAX - half, spawnBX - half) >> 4;
        int maxChunkX = Math.max(spawnAX + half, spawnBX + half) >> 4;

        int minChunkZ = Math.min(spawnAZ - half, spawnBZ - half) >> 4;
        int maxChunkZ = Math.max(spawnAZ + half, spawnBZ + half) >> 4;

        List<BlockChange> changes = new ArrayList<>();

        int totalChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        AtomicInteger scannedChunks = new AtomicInteger();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {

            int chunkX = minChunkX;
            int chunkZ = minChunkZ;

            @Override
            public void onRun(int tick) {
                if (chunkX > maxChunkX) {
                    System.out.println("[TowerMapProcessor] Scan finished. Found " + changes.size() + " blocks");
                    runReplacement(changes, plugin);
                    cancel();
                    return;
                }

                level.loadChunk(chunkX, chunkZ);
                FullChunk chunk = level.getChunk(chunkX, chunkZ);
                if (chunk != null) {
                    for (int y = 0; y < HEIGHT; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                int id = chunk.getBlockId(x, y, z);
                                if (id == 0) continue;

                                int meta = chunk.getBlockData(x, y, z);
                                int key = (id << 8) | meta;

                                System.out.println("-> (" + chunkX + ", " + chunkZ + ") pos (" + x + ", " + y + ", " + z + ") id: " + id + " meta: " + meta);

                                int worldX = (chunkX << 4) + x;
                                int worldZ = (chunkZ << 4) + z;

                                Block replaceA = teamABlocks.get(key);
                                if (replaceA != null) {
                                    BlockChange bc = new BlockChange();
                                    bc.x = worldX;
                                    bc.y = y;
                                    bc.z = worldZ;
                                    bc.block = replaceA;

                                    changes.add(bc);
                                }

                                Block replaceB = teamBBlocks.get(key);
                                if (replaceB != null) {
                                    BlockChange bc = new BlockChange();
                                    bc.x = worldX;
                                    bc.y = y;
                                    bc.z = worldZ;
                                    bc.block = replaceB;

                                    changes.add(bc);
                                }
                            }
                        }
                    }
                }

                scannedChunks.incrementAndGet();

                int percent = (int)((scannedChunks.get() / (float) totalChunks) * 50);
                updatePercentage.execute(percent);

                if (tick % 20 == 0) {
                    System.out.println("[TowerMapProcessor] Scan progress: " + percent + "%");
                }

                chunkZ++;
                if (chunkZ > maxChunkZ) {
                    chunkZ = minChunkZ;
                    chunkX++;
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