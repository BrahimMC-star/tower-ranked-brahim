package zwuiix.colria.game.impl.tower;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
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
    private final Map<Block, Block> teamABlockMap, teamBBlockMap;
    private final Closure<Integer> updatePercentage;
    private final Runnable finish;

    private static class BlockChange {
        Vector3f position;
        Block newBlock;
    }

    public TowerMapProcessor(int size, Level level, Vector3 spawnA, Vector3 spawnB,
                             Map<Block, Block> teamABlockMap,
                             Map<Block, Block> teamBBlockMap,
                             Closure<Integer> updatePercentage,
                             Runnable finish) {
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
        System.out.println("[TowerMapProcessor] Starting scan phase...");
        int halfSize = size / 2;
        int maxHeight = 80;

        List<BlockChange> changes = new ArrayList<>();
        AtomicInteger scanProgress = new AtomicInteger(0);

        var plugin = InventoryHooker.getInstance().getPlugin();
        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int x = -halfSize, y = 0, z = -halfSize;

            @Override
            public void onRun(int tick) {
                int chunk = 16;
                int processed = 0;

                while (processed < chunk) {
                    if (y >= maxHeight) {
                        y = 0;
                        x++;
                        if (x >= halfSize) {
                            System.out.println("[TowerMapProcessor] Scan complete. Total blocks to replace: " + changes.size());
                            runReplacement(changes, plugin);
                            cancel();
                            return;
                        }
                    }

                    while (z < halfSize && processed < chunk) {
                        System.out.println("[TowerMapProcessor] Scanning block at offset (" + x + ", " + y + ", " + z + ")...");

                        Vector3 posA = spawnA.add(x, y, z);
                        Block currentA = level.getBlock(posA, true);
                        for (Map.Entry<Block, Block> entry : teamABlockMap.entrySet()) {
                            Block key = entry.getKey();
                            if (currentA.getId() == key.getId() && currentA.getDamage() == key.getDamage()) {
                                BlockChange bc = new BlockChange();
                                bc.position = new Vector3f((float) posA.x, (float) posA.y, (float) posA.z);
                                bc.newBlock = entry.getValue();
                                changes.add(bc);
                            }
                        }

                        Vector3 posB = spawnB.add(x, y, z);
                        Block currentB = level.getBlock(posB, true);
                        for (Map.Entry<Block, Block> entry : teamBBlockMap.entrySet()) {
                            Block key = entry.getKey();
                            if (currentB.getId() == key.getId() && currentB.getDamage() == key.getDamage()) {
                                BlockChange bc = new BlockChange();
                                bc.position = new Vector3f((float) posB.x, (float) posB.y, (float) posB.z);
                                bc.newBlock = entry.getValue();
                                changes.add(bc);
                            }
                        }

                        z++;
                        processed++;
                        scanProgress.incrementAndGet();
                    }

                    if (z >= halfSize) {
                        z = -halfSize;
                        y++;
                    }
                }

                int percent = Math.min(50, (int) ((scanProgress.get() / (float) (size * size * maxHeight)) * 50));
                updatePercentage.execute(percent);
                if(percent != 0) System.out.println("[TowerMapProcessor] Scan progress: " + percent + "%");
            }
        }, 1);
    }

    private void runReplacement(List<BlockChange> changes, Plugin plugin) {
        System.out.println("[TowerMapProcessor] Starting replacement phase...");
        AtomicInteger processed = new AtomicInteger(0);
        int chunkSize = 16;

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            int index = 0;

            @Override
            public void onRun(int tick) {
                for (int i = 0; i < chunkSize && index < changes.size(); i++, index++) {
                    BlockChange bc = changes.get(index);
                    Vector3 pos = new Vector3(bc.position.x, bc.position.y, bc.position.z);
                    level.setBlock(pos, bc.newBlock);
                    processed.incrementAndGet();
                }

                int percent = 50 + (int) ((processed.get() / (float) changes.size()) * 50);
                updatePercentage.execute(percent);
                System.out.println("[TowerMapProcessor] Replacement progress: " + percent + "%");

                if (index >= changes.size()) {
                    System.out.println("[TowerMapProcessor] Replacement complete. Tower map is ready!");
                    finish.run();
                    cancel();
                }
            }
        }, 1);
    }
}