package zwuiix.colria.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.registry.Registries;
import lombok.Getter;
import zwuiix.colria.Loader;

import java.util.HashMap;

@Getter
public class BlockRegistry {
    @Getter
    private static BlockRegistry instance = new BlockRegistry();

    private final HashMap<String, Block> blocks = new HashMap<>();

    public BlockRegistry() {
        instance = this;
    }

    public Block getBlock(String identifier) {
        return blocks.get(identifier);
    }

    public void register(CustomBlock block) {
        Registries.BLOCK.registerCustom(block.getClass());
        blocks.put(block.getIdentifier(), (Block) block);
    }

    public void invoke(Loader loader) {}
}
