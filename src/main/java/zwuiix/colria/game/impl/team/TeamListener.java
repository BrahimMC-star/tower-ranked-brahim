package zwuiix.colria.game.impl.team;

import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.GameEvent;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.event.GamePlayerDeathEvent;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.HashSet;
import java.util.Set;

public record TeamListener(TeamGame game) {
    public TeamListener(TeamGame game) {
        this.game = game;

        GameEvent.subscribe(game, GamePlayerDeathEvent.class, this::onGamePlayerDeath);
        GameEvent.subscribe(game, EntityDamageByEntityEvent.class, this::onDamage);
        GameEvent.subscribe(game, PlayerChatEvent.class, this::onChat);
    }

    private void onGamePlayerDeath(GamePlayerDeathEvent ev) {
        TeamPlayer victim = (TeamPlayer) ev.getVictim();

        EnginePlayer p = victim.getNukkitPlayer();
        if(p == null) return;

        var params = (TeamGameParameters) game.getParameters();
        if(!params.spawnKill) {
            p.noDamageTicks = 20 * 5;
        }
    }

    private void onDamage(EntityDamageByEntityEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getEntity();
        TeamPlayer teamPlayer = p.getGamePlayer();
        if (teamPlayer == null) return;

        Entity damager = ev.getDamager();
        if (!(damager instanceof EnginePlayer d)) return;

        TeamPlayer damagerPlayer = d.getGamePlayer();
        if (damagerPlayer == null) return;

        TeamGameParameters params = (TeamGameParameters) game.getParameters();
        if (teamPlayer.getTeam().equals(damagerPlayer.getTeam()) && !params.allyDamage) {
            ev.setCancelled(true);
            return;
        }

        if(!params.spawnKill && p.noDamageTicks > 0) {
            p.sendMessage(TranslationKeys.PLAYER8_GAME_TEAM_SPAWNKILL, p.noDamageTicks / 20);
            ev.setCancelled(true);
            return;
        }
    }

    private void onChat(PlayerChatEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        TeamPlayer teamPlayer = p.getGamePlayer();
        if (teamPlayer == null) return;
        ev.setCancelled();

        if(ev.getMessage().startsWith("@")) {
            for (CommandSender recipient : ev.getRecipients()) {
                if (!(recipient instanceof EnginePlayer target)) continue;
                var color = teamPlayer.getTeam().color();
                target.sendMessage(TextFormat.colorize("&8[&3Global&8][" + color +  target.processTranslation(teamPlayer.getTeam().name()) + "&8] " + color + p.getName() + ": &r" + ev.getMessage()));
            }
        } else {
            Set<CommandSender> recipients = new HashSet<>(game.getSpectators().values());
            for (GamePlayer gamePlayer : game.getPlayers().values()) {
                var tp = (TeamPlayer) gamePlayer;
                if(tp.getTeam().equals(teamPlayer.getTeam())) {
                    EnginePlayer target = gamePlayer.getNukkitPlayer();
                    if(target != null) recipients.add(target);
                }
            }

            for (CommandSender recipient : recipients) {
                if (!(recipient instanceof EnginePlayer target)) continue;
                var color = teamPlayer.getTeam().color();
                target.sendMessage(TextFormat.colorize("&8[" + color +  target.processTranslation(teamPlayer.getTeam().name()) + "&8] " + color + p.getName() + ": &r" + ev.getMessage()));
            }
        }
    }

}
