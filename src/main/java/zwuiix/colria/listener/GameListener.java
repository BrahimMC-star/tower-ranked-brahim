package zwuiix.colria.listener;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.*;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.event.PlayerItemUseEvent;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameEvent;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.event.GamePlayerDamageEvent;
import zwuiix.colria.game.event.GamePlayerDeathEvent;
import zwuiix.colria.game.event.GamePlayerItemDropDeathEvent;
import zwuiix.colria.game.event.GamePlayerPickupEvent;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.util.Chat;

import java.util.HashSet;
import java.util.Set;

public class GameListener implements Listener {
    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent ev) {
        EnginePlayer player = (EnginePlayer) ev.getPlayer();
        DataPacket pk = ev.getPacket();

        Game game = player.getGame();
        if(game == null) {
            return;
        }

        if((pk instanceof InteractPacket interactPacket) && interactPacket.action == InteractPacket.ACTION_OPEN_INVENTORY && (player.isSurvival() || player.isAdventure()) && game.getGameInventoryType().equals(Game.GameInventory.DONKEY)) {
            UpdateEquipmentPacket packet = new UpdateEquipmentPacket();
            packet.windowId = player.getWindowId(player.getInventory());
            packet.windowType = 12;
            packet.eid = player.getId();
            packet.namedtag = new CompoundTag();
            player.dataPacket(packet);
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent ev) {
        EnginePlayer player = (EnginePlayer) ev.getPlayer();
        for (Game game : GameRegistry.getInstance().getGames().values()) {
            GamePlayer gamePlayer = game.getStartedPlayers().get(game.getName());
            if(gamePlayer != null) {
                player.setGame(game);
                game.rejoin(gamePlayer);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev) {
        Entity e = ev.getEntity();

        if(!(e instanceof EnginePlayer)) {
            if(e instanceof EntityItem) {
                var owner = Server.getInstance().getPlayerExact(((EntityItem) e).getOwner());
                if(owner != null) {
                    var p = (EnginePlayer)owner;
                    var game = p.getGame();
                    if(game != null) {
                        GamePlayer gamePlayer = p.getGamePlayer();
                        GameEvent.publish(game, new GamePlayerItemDropDeathEvent(gamePlayer, ((EntityItem) e).getItem()));
                    }
                }
            }
            return;
        }

        EnginePlayer p = (EnginePlayer)e;
        Game game = p.getGame();
        if(game == null) {
            return;
        }

        if(!game.getParameters().critical && ev.isApplicable(EntityDamageEvent.DamageModifier.CRITICAL)) {
            ev.getModifiers().remove(EntityDamageEvent.DamageModifier.CRITICAL);
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
        if(ev.isCancelled()) {
            return;
        }

        GamePlayer attacker = getAttacker(ev);

        GamePlayerDamageEvent damageEv = new GamePlayerDamageEvent(gamePlayer, attacker, ev.getCause(), ev);
        GameEvent.publish(game, damageEv);

        if(damageEv.isCancelled()) {
            ev.setCancelled();
            return;
        }

        float health = p.getHealth() - ev.getFinalDamage();
        if(health <= 1) {
            ev.setCancelled();

            GamePlayerDeathEvent event = new GamePlayerDeathEvent(gamePlayer, attacker, game.getCurrentLevel().getSpawnLocation(), ev.getCause());
            GameEvent.publish(game, event);

            if(event.isCancelled()) {
                return;
            }

            game.respawn(event.getVictim(), event.getPosition());
            e.setLastDamageCause(null);
            return;
        }

    }

    private GamePlayer getAttacker(EntityDamageEvent ev) {
        GamePlayer attacker = null;
        if(ev instanceof EntityDamageByEntityEvent ev2) {
            Entity d = ev2.getDamager();
            if(d instanceof EnginePlayer dPlayer) {
                Game dGame = dPlayer.getGame();
                if(dGame != null) {
                    GamePlayer dGamePlayer = dGame.getPlayer(dPlayer.getName());
                    if(dGamePlayer != null) {
                        attacker = dGamePlayer;
                    }
                }
            }
        }
        return attacker;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent ev) {
        EnginePlayer p = (EnginePlayer)ev.getPlayer();

        Game game = p.getGame();
        if(game == null) {
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent ev) {
        EnginePlayer p = (EnginePlayer)ev.getPlayer();

        Game game = p.getGame();
        if(game == null) {
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            var pk = new LevelEventPacket();
            pk.evid = LevelEventPacket.EVENT_PARTICLE_BLOCK_FORCE_FIELD;
            pk.x = ev.getBlock().getFloorX() + 0.5f;
            pk.y = ev.getBlock().getFloorY() + 0.5f;
            pk.z = ev.getBlock().getFloorZ() + 0.5f;
            pk.data = 0;
            p.dataPacket(pk);
            p.addSound(Sound.NOTE_BASS, 0.5f, 1.0f);

            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        EnginePlayer p = (EnginePlayer)ev.getPlayer();

        Game game = p.getGame();
        if(game == null) {
            return;
        }

        if(ev.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onUse(PlayerItemUseEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        Game game = p.getGame();
        if(game == null) {
            return;
        }

        GameEvent.publish(game, ev);
    }


    @EventHandler
    public void onConsume(PlayerItemConsumeEvent ev) {
        EnginePlayer p = (EnginePlayer)ev.getPlayer();

        Game game = p.getGame();
        if(game == null) {
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onChat(PlayerChatEvent ev) {
        EnginePlayer p = (EnginePlayer)ev.getPlayer();

        Game game = p.getGame();
        if(game == null) return;

        Set<CommandSender> recipients = new HashSet<>(game.getSpectators().values());
        if(game.getPlayer(p.getName()) != null) {
            for (GamePlayer gamePlayer : game.getPlayers().values()) {
                EnginePlayer target = gamePlayer.getNukkitPlayer();
                if(target != null) recipients.add(target);
            }
        }

        ev.setRecipients(recipients);
        ev.setMessage(Chat.clean(ev.getMessage()));
        ev.setFormat(TextFormat.colorize("&7{%0}: {%1}"));

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        Game game = p.getGame();
        if (game == null) return;

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        Game game = p.getGame();
        if (game == null) return;

        GamePlayer gamePlayer = p.getGamePlayer();
        if (gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        Game game = p.getGame();
        if (game == null) return;

        GamePlayer gamePlayer = p.getGamePlayer();
        if(game.getState().equals(Game.State.PAUSE)) {
            if(gamePlayer != null) {
                ev.setCancelled(true);
                return;
            }
        }

        if(ev.getTo().getY() <= 0 && gamePlayer != null) {
            GamePlayer attacker = getAttacker(p.getLastDamageCause());
            GamePlayerDeathEvent event = new GamePlayerDeathEvent(gamePlayer, attacker, game.getCurrentLevel().getSpawnLocation(), EntityDamageEvent.DamageCause.VOID);
            GameEvent.publish(game, event);

            if(!event.isCancelled()) {
                game.respawn(event.getVictim(), event.getPosition());
                p.setLastDamageCause(null);
                return;
            }
        }

        GameEvent.publish(game, ev);
    }

    @EventHandler
    public void onPickup(InventoryPickupItemEvent ev) {
        Inventory inventory = ev.getInventory();
        EntityItem entity = ev.getItem();

        InventoryHolder holder = inventory.getHolder();
        if(!(holder instanceof EnginePlayer p)) return;

        Game game = p.getGame();
        if(game == null) return;

        if(!game.getState().equals(Game.State.RUNNING)) {
            ev.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = p.getGamePlayer();
        if(gamePlayer == null) {
            ev.setCancelled(true);
            return;
        }

        GameEvent.publish(game, ev);

        GamePlayerPickupEvent event = new GamePlayerPickupEvent(gamePlayer, entity);
        GameEvent.publish(game, event);
        if(event.isCancelled()) {
            ev.setCancelled(true);
            return;
        }
    }
}
