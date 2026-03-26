package zwuiix.colria.listener;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMace;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.AuthInputAction;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.event.PlayerItemUseEvent;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.punishment.PunishmentManager;
import zwuiix.colria.task.VPNTask;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.KeyInput;

import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent ev) {
        ev.setPlayerClass(EnginePlayer.class);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent ev) {
        EnginePlayer player = (EnginePlayer) ev.getPlayer();

        Lobby lobby = GameRegistry.getInstance().randomLobby();
        lobby.join(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        EnginePlayer player = (EnginePlayer) ev.getPlayer();

        for (int i = 0; i < 512; i++) player.sendMessage("colria:clearchat");
        player.sendMessage(
                Glyph.hbarThick(EngineInfo.COLOR, 1) + "\n" +
                player.processTranslation(TranslationKeys.PLAYER_WELCOME,
                        player.getName(),
                        "#" + player.getGame().getIdentifier(),
                        player.getPing(),
                        player.getHighestRank().getColoredName(),
                        Server.getInstance().getOnlinePlayers().size(),
                        GameRegistry.getInstance().getGames().size()
                ) + "\n" + Glyph.hbarThick(EngineInfo.COLOR, 1)
        );

        if(player.hasPermission(Permission.SUPPORTER.toString())) {
            player.sendTitle(
                    player.processTranslation(TranslationKeys.PLAYER_WELCOME_TITLE_SUPPORTER),
                    player.processTranslation(TranslationKeys.PLAYER_WELCOME_SUBTITLE_SUPPORTER),
                    10, 125, 20
            );
        }

        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(() -> {
            if(!player.isConnected()) return;
            var info = player.getPlayerDataInfo();
            var discordId = info.getDiscordId();
            if(discordId.isEmpty()) {
                player.sendMessage(TranslationKeys.PLAYER_DISCORD_LINK_PROMPT);
                player.addSound(Sound.BEACON_DEACTIVATE, 0.5f, 1.0f);
            }
        }, 20 * 5, 20 * 60 * 10, true);

        var manager = BoosterManager.getInstance();
        if(manager != null && manager.hasBossBar()) {
            manager.getBossBar().addViewer(player);
        }

        ev.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
        EnginePlayer player = (EnginePlayer) ev.getPlayer();

        Game game = player.getGame();
        if (game != null) {
            if (game.getState().equals(Game.State.LOBBY) && game.getHoster().equalsIgnoreCase(player.getName())) {
                game.stop();
            } else {
                game.removeSpectator(player);
                game.removePlayer(player);
            }

            game.cleanup(player);
        }

        player.onDisconnect();
        var info = player.getPlayerDataInfo();
        if(info != null) {
            info.setLastLogin(System.currentTimeMillis());
            info.setPlaytime(info.getPlaytime() + (System.currentTimeMillis() - player.start));
        }

        var manager = BoosterManager.getInstance();
        if(manager != null && manager.hasBossBar()) {
            manager.getBossBar().removeViewer(player);
        }

        ev.setQuitMessage("");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent ev) {
        if (ev.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        Item item = ev.getItem();

        if (ev.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
            PlayerItemUseEvent use = new PlayerItemUseEvent(p, item);
            if (use.isCancelled()) {
                ev.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
        var damager = ev.getDamager();
        if(damager instanceof EnginePlayer p) {
            p.addClick();
            p.needDisplayTitleInfo();
        }
    }

    @EventHandler
    public void onReceive(DataPacketReceiveEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        DataPacket pk = ev.getPacket();

        if (pk instanceof EmotePacket || pk instanceof LevelSoundEventPacket) {
            ev.setCancelled();
            return;
        }

        if (pk instanceof ServerboundDiagnosticsPacket diagnosticsPacket) {
            p.fps = (int) diagnosticsPacket.avgFps;
            p.needDisplayTitleInfo();
            return;
        }

        if(pk instanceof PlayerAuthInputPacket inputPacket) {
            var motion = inputPacket.getMotion();

            List<KeyInput> keys = new ArrayList<>();
            if (motion.getY() > 0) keys.add(KeyInput.W);
            else if (motion.getY() < 0) keys.add(KeyInput.S);

            if (motion.getX() > 0) keys.add(KeyInput.A);
            else if (motion.getX() < 0) keys.add(KeyInput.D);

            if(!keys.isEmpty()) p.sinceLastInput = 0;
            else p.sinceLastInput++;

            p.keys = keys;

            var inputData = inputPacket.getInputData();
            if (inputData.contains(AuthInputAction.JUMP_PRESSED_RAW)) {
                p.sinceLastDoubleJump = (p.sinceLastJump > 8)
                        ? p.sinceLastDoubleJump + 1
                        : 0;

                p.sinceLastJump = 0;
                return;
            }
            if (inputData.contains(AuthInputAction.MISSED_SWING)) {
                var itemInHand = p.getInventory().getItemInHand();
                if (itemInHand.getId() != 0 && !(itemInHand instanceof ItemMace)) {
                    itemInHand.onAttack(p, p);
                }

                p.addClick();
                p.needDisplayTitleInfo();
                return;
            }

            p.sinceLastJump++;
            p.sinceLastDoubleJump++;
        }
    }

    @EventHandler
    public void onSend(DataPacketSendEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        DataPacket pk = ev.getPacket();

        if (!p.logged && pk instanceof PlayStatusPacket s && s.status == PlayStatusPacket.PLAYER_SPAWN) {
            ev.setCancelled();

            if (EngineInfo.ANTI_VPN) {
                Server.getInstance().getScheduler().scheduleAsyncTask(new VPNTask(p.getAddress(), result -> {
                    if(result) {
                        p.close(p.processTranslation(TranslationKeys.ANTIVPN_KICK));
                        return;
                    }

                    if (PunishmentManager.getInstance().kickIfBanned(p)) return;
                    p.resync().thenAccept(v -> {
                        p.dataPacket(pk);
                        if(p.isInLobby()) p.getGame().join(p);
                    });
                }));
                return;
            }

            if (PunishmentManager.getInstance().kickIfBanned(p)) return;
            p.resync().thenAccept(v -> {
                p.dataPacket(pk);
                if(p.isInLobby()) p.getGame().join(p);
            });
        }
    }

    @EventHandler
    public void onQuery(QueryRegenerateEvent ev) {
        var server = Server.getInstance();
        var onlines = server.getOnlinePlayers().size();

        var motd = EngineInfo.NAME + " " + EngineInfo.VERSION;
        server.getNetwork().setName(motd);
        server.getNetwork().setSubName("@Zwuiix");
        ev.setServerName(motd);

        ev.setPlayerCount(onlines);
        ev.setMaxPlayerCount(onlines + 1);
        ev.setListPlugins(true);
    }

    @EventHandler
    public void onChat(PlayerChatEvent ev) {
        var manager = PunishmentManager.getInstance();
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        if (manager.isMuted(p.getName())) {
            var mute = manager.getMute(p.getName());
            p.sendMessage(TranslationKeys.PLAYER_YOU_ARE_MUTED, mute.getReason(),  mute.isPermanent() ? "Permanent" : PunishmentManager.formatDuration(mute.getRemainingTime()));

            ev.setCancelled();
            return;
        }
    }
}
