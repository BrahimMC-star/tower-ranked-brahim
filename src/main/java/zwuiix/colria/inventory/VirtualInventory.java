package zwuiix.colria.inventory;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.ContainerType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract public class VirtualInventory {
    @Getter
    protected String name;
    protected String prefix;
    @Getter
    protected int size;

    protected final ArrayList<Player> viewers = new ArrayList<>();
    @Getter
    protected Item[] contents;

    protected Consumer<InventoryClick> onClick = null;
    protected Consumer<Player> onClose = null;
    protected Map<Integer, Consumer<InventoryClick>> slotClickHandlers = new HashMap<>();

    public VirtualInventory(int size) {
        this(size, "Chest");
    }

    public VirtualInventory(int size, String name) {
        this.size = size;
        this.name = name;
        int rows = size / 9 + (size % 9 == 0 ? 0 : 1);
        int scroll = rows > 6 ? 1 : 0;
        int length = Math.min(rows, 6);
        this.prefix = "§" + length + "§" + scroll + "§r§r§r§r§r§r§r§r§r§r";

        contents = new Item[size];
    }

    public Item getItem(int slot) {
        Item item = contents[slot];
        if(item == null) {
            return Item.AIR_ITEM;
        }

        return item;
    }

    public SlotSetResult setItem(int slot, Item item) {
        return setItem(slot, item, true);
    }

    public SlotSetResult setItem(int slot, Item item, boolean sync) {
        if(slot < 0 || slot >= size) {
            return null;
        }

        if(item == null) {
            item = Item.AIR_ITEM;
        }

        contents[slot] = item;
        if(sync) syncSlot(slot);
        slotClickHandlers.remove(slot);
        return new SlotSetResult(this, slot);
    }

    public void open(Player player) {
        open(player, name);
    }

    public void open(Player player, String name) {
        if(viewers.contains(player)) {
            close(player);
        }

        InventoryManager invManager = InventoryManager.getInstance();
        invManager.setDispatcher(player, new InventoryDispatcher(player, sendInventory(player, name), this::syncContents));

        for (DataPacket packet : sendInventory(player, name)) {
            player.directDataPacket(packet);
        }

        viewers.add(player);
        invManager.setInventory(player, this);

        syncContents();
    }

    public void close(Player player) {
        if(!viewers.contains(player)) return;

        ContainerClosePacket closePacket = new ContainerClosePacket();
        closePacket.windowId = Byte.MAX_VALUE;
        closePacket.type = ContainerType.CONTAINER;
        closePacket.wasServerInitiated = true;
        player.directDataPacket(closePacket);
        onClose(player);
    }

    public void onClose(Player player) {
        if(!viewers.contains(player)) return;
        if(onClose != null) onClose.accept(player);
        viewers.remove(player);

        for (DataPacket packet : removeInventory(player)) {
            player.directDataPacket(packet);
        }

        InventoryManager.getInstance().resetInventory(player);
    }

    public void syncSlot(int slot) {
        var pk = new InventorySlotPacket();
        pk.inventoryId = Byte.MIN_VALUE;
        pk.containerNameData = new FullContainerName(getSlotType(), null);
        pk.item = getItem(slot);

        viewers.forEach((viewer) -> {
            if(!viewer.isConnected()) {
                viewers.remove(viewer);
                return;
            }

            viewer.directDataPacket(pk);
        });
    }

    public void syncContents() {
        var pk = new InventoryContentPacket();
        pk.inventoryId = Byte.MIN_VALUE;
        pk.containerNameData = new FullContainerName(getSlotType(), null);
        pk.storageItem = Item.AIR_ITEM;
        pk.slots = contents;

        viewers.forEach((viewer) -> {
            if(!viewer.isConnected()) {
                viewers.remove(viewer);
                return;
            }

            viewer.directDataPacket(pk);
        });
    }

    public boolean isViewer(Player player) {
        return viewers.contains(player);
    }

    public void onClick(Player player, int slot) {
        if(slot < 0 || slot >= size) return;
        if(!viewers.contains(player)) return;

        var event = new InventoryClick(this, player, slot, getItem(slot));
        if(slotClickHandlers.containsKey(slot)) slotClickHandlers.get(slot).accept(event);

        if(onClick != null)
            onClick.accept(event);
    }

    public record SlotSetResult(VirtualInventory parent, int slot) {
        public void onClick(Consumer<InventoryClick> onClick) {
                parent.slotClickHandlers.put(slot, onClick);
        }
    }


    abstract protected ContainerSlotType getSlotType();
    abstract protected DataPacket[] sendInventory(Player player, String name);
    abstract protected DataPacket[] removeInventory(Player player);
}
