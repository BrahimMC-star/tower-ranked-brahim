package zwuiix.colria.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.PacketViolationWarningPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.Plugin;
import lombok.Getter;

public class InventoryHooker implements Listener {
    @Getter
    private static final InventoryHooker instance = new InventoryHooker();

    public static void register(Plugin p) {
        if(instance.plugin != null) {
            throw new  IllegalStateException("Plugin " + p.getName() + " already registered!");
        }

        instance.plugin = p;
        instance.registerEvent();
    }

    private Plugin plugin;

    public InventoryHooker() {
        EntityDefinition definition = EntityManager.get().getDefinition(DummyEntity.definition.getIdentifier());
        if(definition == null) {
            EntityManager.get().registerDefinition(DummyEntity.definition);
        }
    }

    public void registerEvent() {
        Server.getInstance().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        InventoryManager manager = InventoryManager.getInstance();
        VirtualInventory inventory = manager.getInventory(player);
        if(inventory != null) {
            inventory.close(player);
        }
    }

    @EventHandler
    private void onDataPacketReceive(DataPacketReceiveEvent ev) {
        DataPacket pk = ev.getPacket();
        Player player = ev.getPlayer();

        InventoryManager manager = InventoryManager.getInstance();

        if((pk instanceof ContainerClosePacket closePacket) && closePacket.windowId == Byte.MIN_VALUE) {
            VirtualInventory inventory =  manager.getInventory(player);
            if(inventory != null) {
                inventory.onClose(player);
                player.directDataPacket(pk);
                ev.setCancelled(true);
            }
            return;
        }

        if((pk instanceof InventoryTransactionPacket transactionPacket)) {
            if(transactionPacket.transactionType != InventoryTransactionPacket.TYPE_NORMAL) return;
            if(transactionPacket.actions.length > 100) {
                player.kick("disconnectionScreen.badPacket");
                return;
            }

            VirtualInventory inventory =  manager.getInventory(player);
            if(inventory != null && inventory.isViewer(player) && !processTransaction(player, transactionPacket.actions)) {
                ev.setCancelled(true);
                player.setNeedSendInventory(true);

                inventory =  manager.getInventory(player);
                if(inventory != null) {
                    inventory.syncContents();
                }
            }
        }

        if(pk instanceof PacketViolationWarningPacket violation) {
            ev.setCancelled(true);

            if(!violation.type.equals(PacketViolationWarningPacket.PacketViolationType.UNKNOWN)) return;
            if(!violation.severity.equals(PacketViolationWarningPacket.PacketViolationSeverity.UNKNOWN)) return;
            if(violation.packetId != 156) return;

            InventoryDispatcher dispatcher = manager.getDispatcher(player);
            if(dispatcher != null) {
                dispatcher.process();
                manager.resetDispatcher(player);
            }

        }
    }

    private boolean processTransaction(Player player, NetworkInventoryAction[] actions) {
        InventoryManager manager = InventoryManager.getInstance();

        for (NetworkInventoryAction action : actions) {
            VirtualInventory inventory =  manager.getInventory(player);
            if(inventory != null && inventory.isViewer(player) && action.windowId == Byte.MIN_VALUE) {
                inventory.onClick(player, action.inventorySlot);
                return false;
            }
        }

        VirtualInventory inventory =  manager.getInventory(player);
        if(inventory != null) {
            inventory.syncContents();
        }

        return true;
    }
}
