package zwuiix.colria.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.types.inventory.ContainerType;
import cn.nukkit.scheduler.TaskHandler;

final public class InventoryDispatcher {
    private final TaskHandler taskHandler;

    private final Player player;
    private final DataPacket[] packets;
    private final Runnable runnable;
    private long timemout = 40L;

    public InventoryDispatcher(Player player, DataPacket[] packets, Runnable runnable) {
        this.player = player;
        this.packets = packets;
        this.runnable = runnable;

        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(this::run, 1);
    }

    public void run() {
        if(!player.isConnected()) return;

        timemout--;
        if(timemout <= 0) {
            process();
            return;
        }

        ContainerClosePacket pk = new ContainerClosePacket();
        pk.windowId = Byte.MAX_VALUE;
        pk.type = ContainerType.CONTAINER;
        pk.wasServerInitiated = false;
        player.directDataPacket(pk);

        for(DataPacket packet : packets) {
            player.directDataPacket(packet);
        }

    }

    public void process() {
        runnable.run();
        this.taskHandler.cancel();
    }
}
