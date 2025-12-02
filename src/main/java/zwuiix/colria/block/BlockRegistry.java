package zwuiix.colria.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.registry.Registries;
import zwuiix.colria.Loader;

import java.util.HashMap;

public class BlockRegistry {
    private static BlockRegistry INSTANCE = new BlockRegistry();

    public static BlockRegistry getInstance() { return INSTANCE; }

    private HashMap<String, Block> blocks = new HashMap<>();

    public HashMap<String, Block> getBlocks() {
        return blocks;
    }

    public BlockRegistry() {
        INSTANCE = this;
    }

    public Block getBlock(String identifier) {
        return blocks.get(identifier);
    }

    public void register(CustomBlock block) {
        Registries.BLOCK.registerCustom(block.getClass());
        blocks.put(block.getIdentifier(), (Block) block);
    }

    public void invoke(Loader loader) {
    }
}
