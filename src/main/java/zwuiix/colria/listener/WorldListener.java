package zwuiix.colria.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerListPacket;

import java.util.ArrayList;

public final class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSend(DataPacketSendEvent ev) {
        Player p = ev.getPlayer();
        DataPacket pk = ev.getPacket();
        if (!(pk instanceof PlayerListPacket list) || list.type != PlayerListPacket.TYPE_ADD) return;

        ArrayList<PlayerListPacket.Entry> keep = new ArrayList<>(list.entries.length);
        for (PlayerListPacket.Entry e : list.entries) {
            var target = Server.getInstance().getPlayer(e.uuid);
            if (target.isPresent() && target.get().getLevel() == p.getLevel()) {
                keep.add(e);
            }
        }

        if (keep.isEmpty()) {
            ev.setCancelled(true);
            return;
        }

        list.entries = keep.toArray(PlayerListPacket.Entry[]::new);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        syncTabFor(ev.getPlayer());
    }

    @EventHandler
    public void onWorld(EntityLevelChangeEvent ev) {
        if (!(ev.getEntity() instanceof Player p)) return;

        p.setDimension(Level.DIMENSION_NETHER);
        p.setDimension(ev.getTarget().getDimension());

        syncTabFor(p);
    }

    private static void syncTabFor(Player p) {
        for (Player o : Server.getInstance().getOnlinePlayers().values()) {
            if (o.equals(p)) {
                sendAdd(p, p);
            }

            if (o.getLevel() == p.getLevel()) {
                sendAdd(p, o);
                sendAdd(o, p);
            } else {
                sendRemove(p, o);
                sendRemove(o, p);
            }
        }
    }

    private static void sendRemove(Player to, Player about) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_REMOVE;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(about.getUniqueId())};
        to.dataPacket(pk);
    }

    private static void sendAdd(Player to, Player about) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_ADD;

        PlayerListPacket.Entry e = new PlayerListPacket.Entry(about.getUniqueId());
        e.entityId = about.getId();
        e.name = about.getName();
        e.skin = about.getSkin();
        try {
            e.xboxUserId = about.getLoginChainData() != null ? about.getLoginChainData().getXUID() : "";
        } catch (Throwable ignored) {
        }
        pk.entries = new PlayerListPacket.Entry[]{e};

        to.dataPacket(pk);
    }
}