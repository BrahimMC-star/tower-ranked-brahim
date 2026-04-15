package zwuiix.colria.game.impl.tower;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockBricks;
import cn.nukkit.block.BlockEndPortal;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemAppleGold;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelEventPacket;
import zwuiix.colria.event.PlayerKnockbackEvent;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameEvent;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.event.GamePlayerDamageEvent;
import zwuiix.colria.game.event.GamePlayerDeathEvent;
import zwuiix.colria.game.event.GamePlayerItemDropDeathEvent;
import zwuiix.colria.game.event.GamePlayerPickupEvent;
import zwuiix.colria.game.impl.team.Team;
import zwuiix.colria.game.impl.team.TeamSpawnPoint;
import zwuiix.colria.game.impl.towerbridge.TowerBridgeGame;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public record TowerListener(TowerGame game) {
    public TowerListener(TowerGame game) {
        this.game = game;

        GameEvent.subscribe(game, GamePlayerDamageEvent.class, this::onAttack);
        GameEvent.subscribe(game, GamePlayerDeathEvent.class, this::onGamePlayerDeath);
        GameEvent.subscribe(game, BlockBreakEvent.class, this::onBlockBreak);
        GameEvent.subscribe(game, BlockPlaceEvent.class, this::onBlockPlace);
        GameEvent.subscribe(game, PlayerInteractEvent.class, this::onInteract);
        GameEvent.subscribe(game, PlayerMoveEvent.class, this::onMove);
        GameEvent.subscribe(game, PlayerItemConsumeEvent.class, this::onConsume);
        GameEvent.subscribe(game, PlayerKnockbackEvent.class, this::onKnockback);
        GameEvent.subscribe(game, GamePlayerItemDropDeathEvent.class, this::onItemDropDeath);
    }

    private void onAttack(GamePlayerDamageEvent ev) {
        TowerPlayer victim = (TowerPlayer) ev.getVictim();
        TowerPlayer attacker = (TowerPlayer) ev.getAttacker();
        EntityDamageEvent nukkit = ev.getNukkitEvent();

        if(attacker != null) {
            attacker.hits++;
            attacker.inflictedDamages += nukkit.getFinalDamage();
            victim.hit(attacker, nukkit.getFinalDamage());

            if(nukkit.isApplicable(EntityDamageEvent.DamageModifier.CRITICAL)) {
                attacker.crits++;
            }
        }

        victim.receivedDamages += nukkit.getFinalDamage();
    }

    private void onGamePlayerDeath(GamePlayerDeathEvent ev) {
        TowerPlayer victim = (TowerPlayer) ev.getVictim();
        TowerPlayer attacker = (TowerPlayer) ev.getAttacker();

        victim.deaths++;
        victim.killStreaks = 0;

        ItemAppleGold apple = new ItemAppleGold();
        apple.setCount(0);

        EnginePlayer p = victim.getNukkitPlayer();
        if (p != null) {
            PlayerInventory inv = p.getInventory();

            for (int slot = 0; slot < inv.getContents().size(); slot++) {
                Item item = inv.getContents().get(slot);
                if (item instanceof ItemAppleGold) {
                    apple.setCount(apple.getCount() + item.getCount());
                    item.setCount(0);
                }
            }
        }

        if (attacker != null) {
            attacker.kills++;
            attacker.killStreaks++;
            if(attacker.killStreaks > attacker.bestKillStreaks) {
                attacker.bestKillStreaks = attacker.killStreaks;
            }

            EnginePlayer p2 = attacker.getNukkitPlayer();
            PlayerInventory inv2 = p2.getInventory();
            inv2.addItem(apple);

            p2.addSound("random.orb", 0.5f, 1.0f);

            List<TowerPlayer> assists = victim.resolveAssists(attacker);
            if (assists.isEmpty()) {
                game.broadcast(TranslationKeys.PLAYER_GAME_TOWER_DEATH_BY_ATTACKER, victim.getTeam().color() + victim.getUsername(), attacker.getTeam().color() + attacker.getUsername());
            } else {
                var joins = assists.stream().map(t -> t.getTeam().color() + t.getUsername()).collect(Collectors.joining(", "));
                game.broadcast(TranslationKeys.PLAYER_GAME_TOWER_DEATH_BY_ATTACKER_ASSISTS, victim.getTeam().color() + victim.getUsername(), attacker.getTeam().color() + attacker.getUsername(), joins);
            }
        } else if(ev.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            game.broadcast(TranslationKeys.PLAYER_GAME_TOWER_DEATH_VOID, victim.getTeam().color() + victim.getUsername());
        } else game.broadcast(TranslationKeys.PLAYER_GAME_TOWER_DEATH, victim.getTeam().color() + victim.getUsername());

        TeamSpawnPoint spawnPoint = game.getSpawnPoint();
        ev.setPosition(Position.fromObject(victim.getTeam().equals(game.getTeamA()) ? spawnPoint.first() : spawnPoint.second(), game.getCurrentLevel()));
    }

    public void onBlockBreak(BlockBreakEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        if(p.isCreative()) return;

        Block block = ev.getBlock();

        TowerGame game = (TowerGame) p.getGame();
        if (game == null) return;

        if (!(block instanceof BlockBricks)) {
            ev.setCancelled(true);
        } else {
            p.getInventory().addItem(block.toItem());
            game.getTask().removeTicker(block.getFloorX() + ":" + block.getFloorY() + ":" + block.getFloorZ());

            TowerPlayer player = p.getGamePlayer();
            player.blockBreak++;
        }

        ev.setDrops(new Item[]{});
    }

    public void onBlockPlace(BlockPlaceEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        if (p.isCreative()) return;

        TowerGame game = (TowerGame) p.getGame();
        if (game == null) return;

        Block block = ev.getBlock();
        if(block instanceof BlockBricks) {
            final Position pos = block.getLocation();
            final int x = pos.getFloorX(), y = pos.getFloorY(), z = pos.getFloorZ();

            var spawns = game.getSpawnPoint();
            Vector3 sA = spawns.first(), sB = spawns.second();
            double dA = sA.distanceSquared(pos), dB = sB.distanceSquared(pos);
            int refY = (dA <= dB ? sA.getFloorY() : sB.getFloorY());

            if (y >= refY + (game instanceof TowerBridgeGame ? 0 : 13)) {
                p.sendMessage(TranslationKeys.PLAYER_GAME_TOWER_MAXHEIGHT);
                ev.setCancelled(true);
                return;
            }
            if (y < refY - 7) {
                p.sendMessage(TranslationKeys.PLAYER_GAME_TOWER_MINHEIGHT);
                ev.setCancelled(true);
                return;
            }

            if (isNearPortalAbove(game, x, y, z, 1, 5)) {
                ev.setCancelled(true);
                return;
            }

            TowerGameParameters params = (TowerGameParameters) game.getParameters();
            String key = block.getFloorX() + ":" + block.getFloorY() + ":" + block.getFloorZ();
            final int life = Math.max(1, params.despawnBlocks) * 20;
            final int cx = pos.getFloorX() >> 4, cz = pos.getFloorZ() >> 4;

            final AtomicInteger elapsed = new AtomicInteger(0);
            game.getTask().addTicker(key, (i) -> {
                int e = elapsed.getAndIncrement();

                switch (params.despawnAnimation) {
                    case PROGRESSIVE -> {
                        int remaining = Math.max(1, life - e);
                        startBreakingFixed(game, pos, cx, cz, remaining);
                    }
                    case QUICK -> {
                        int q = Math.max(1, life / 4);
                        int qStart = Math.max(0, life - q);

                        if (e >= qStart) {
                            int remaining = Math.max(1, life - e);
                            startBreakingFixed(game, pos, cx, cz, remaining);
                        }
                    }
                    default -> { }
                }

                if (e + 1 >= life) {
                    stopBreaking(game, pos, cx, cz);
                    game.getTask().removeTicker(key);
                    game.getGameLevel().setBlock(pos, new BlockAir(), true);
                }
            });

            TowerPlayer player = p.getGamePlayer();
            player.blockPlace++;
        }
    }

    private static boolean isNearPortalAbove(TowerGame game, int x, int y, int z, int radius, int yUpRange) {
        final var level = game.getGameLevel();
        for (int dy = 0; dy <= yUpRange; dy++) {
            int yy = y - dy;
            if (yy < 0) break;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = level.getBlock(new Vector3(x + dx, yy, z + dz));
                    if (b instanceof BlockEndPortal) return true;
                }
            }
        }
        return false;
    }

    private static void startBreakingFixed(TowerGame game, Position pos, int cx, int cz, int ticks) {
        int step = Math.max(1, 65535 / Math.max(1, ticks));
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
        pk.x = (float) pos.x; pk.y = (float) pos.y; pk.z = (float) pos.z;
        pk.data = step;
        game.getGameLevel().addChunkPacket(cx, cz, pk);
    }

    private static void stopBreaking(TowerGame game, Position pos, int cx, int cz) {
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK;
        pk.x = (float) pos.x; pk.y = (float) pos.y; pk.z = (float) pos.z;
        pk.data = 0;
        game.getGameLevel().addChunkPacket(cx, cz, pk);
    }

    public void onInteract(PlayerInteractEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        Block block = ev.getBlock();

        TowerGame game = (TowerGame) p.getGame();
        if (game == null) return;

        if (ev.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && !(block instanceof BlockBricks)) {
            ev.setCancelled(true);
        }
    }

    public void onMove(PlayerMoveEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        TowerGame game = (TowerGame) p.getGame();
        if (game == null)
            return;

        TowerPlayer gamePlayer = p.getGamePlayer();
        if (gamePlayer == null)
            return;

        Position to = ev.getTo();
        if (to.getLevel() != game.getGameLevel()) return;

        final Position feet = Position.fromObject(to.floor(), game.getGameLevel());
        final Block block = game.getGameLevel().getBlock(feet);
        if (!(block instanceof BlockEndPortal)) return;

        var spawns = game.getSpawnPoint();
        final Vector3 spawnA = spawns.first(), spawnB = spawns.second();
        final Team teamA = game.getTeamA(), teamB = game.getTeamB();
        final Team myTeam = gamePlayer.getTeam();

        final Team goalCamp = (spawnA.distanceSquared(feet) <= spawnB.distanceSquared(feet)) ? teamA : teamB;
        final Vector3 mySpawn = myTeam.equals(teamA) ? spawnA : spawnB;
        if (!goalCamp.equals(myTeam)) {
            int points = myTeam.equals(teamA) ? ++game.getTowerPoints().first : ++game.getTowerPoints().second;
            gamePlayer.points++;

            game.global(player -> player.sendMessage(
                    TranslationKeys.PLAYER_GAME_TOWER_MARK,
                    myTeam.color() + p.getName(),
                    myTeam.color() + player.processTranslation(myTeam.name()),
                    myTeam.color() + points
            ));
            game.broadcast(myTeam.color() + "+1", myTeam.color() + p.getName());
            game.global((player) -> player.addSound("tower.score", 1.0f, 1.0f));

            TowerGameParameters params = (TowerGameParameters) game.getParameters();
            boolean hasRegen = params.markReward.equals(TowerGameParameters.MarkReward.ALL) || params.markReward.equals(TowerGameParameters.MarkReward.REGEN) || params.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_APPLE) || params.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_BLOCKS);
            boolean hasApple = params.markReward.equals(TowerGameParameters.MarkReward.ALL) || params.markReward.equals(TowerGameParameters.MarkReward.APPLE) || params.markReward.equals(TowerGameParameters.MarkReward.APPLE_AND_BLOCKS) || params.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_APPLE);
            boolean hasBlocks = params.markReward.equals(TowerGameParameters.MarkReward.ALL) || params.markReward.equals(TowerGameParameters.MarkReward.BLOCKS) || params.markReward.equals(TowerGameParameters.MarkReward.APPLE_AND_BLOCKS) || params.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_BLOCKS);

            if(hasRegen) {
                p.setHealth(p.getHealth() + params.regen);
                game.getGameLevel().addParticle(new HeartParticle(mySpawn, 1));
            }

            if(hasApple) {
                Item apple = new ItemAppleGold();
                apple.setCount(params.apple);
                p.getInventory().addItem(apple);
            }

            if(hasBlocks) {
                Item bricks = new BlockBricks().toItem();
                bricks.setCount(params.blocks);
                p.getInventory().addItem(bricks);
            }
        }

        game.teleport(gamePlayer, Position.fromObject(mySpawn, game.getGameLevel()));
    }

    private void onConsume(PlayerItemConsumeEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();
        Item item = ev.getItem();

        TowerGame game = (TowerGame) p.getGame();
        if (game == null)
            return;

        TowerPlayer player = p.getGamePlayer();
        if(player == null)
            return;

        if(!(item instanceof ItemAppleGold)) {
            ev.setCancelled(true);
            return;
        }

        player.appleEaten++;
    }

    private void onPickup(GamePlayerPickupEvent ev) {
        TowerPlayer player = (TowerPlayer) ev.getPlayer();
        EntityItem entityItem = ev.getEntityItem();

        TowerGame game = (TowerGame) player.getGame();
        if (game == null) return;

        String owner = entityItem.getOwner();
        Item item = entityItem.getItem();
        if(owner != null) {
            TowerPlayer ownerPlayer = (TowerPlayer) game.getPlayer(owner);
            if(item instanceof ItemAppleGold && ownerPlayer != null) {
                if(player.getTeam().equals(ownerPlayer.getTeam())) {
                    ownerPlayer.appleShared += item.getCount();
                } else ownerPlayer.appleSharedEnemies += item.getCount();
            }
        }
    }

    private void onKnockback(PlayerKnockbackEvent ev) {
        EnginePlayer p = (EnginePlayer) ev.getPlayer();

        TowerGame game = (TowerGame) p.getGame();
        if (game == null)
            return;

        TowerPlayer gamePlayer = p.getGamePlayer();
        if (gamePlayer == null)
            return;

        ev.setStrengthXZ(0.43D);
        ev.setStrengthY(0.42D);
    }

    private void onItemDropDeath(GamePlayerItemDropDeathEvent ev) {
        TowerPlayer player = (TowerPlayer) ev.getPlayer();
        Item item = ev.getItem();

        if(!(item instanceof ItemAppleGold)) return;

        var cause = player.getNukkitPlayer().getLastDamageCause();
        if(!(cause instanceof EntityDamageByEntityEvent ev2)) return;

        Entity d = ev2.getDamager();
        if(!(d instanceof EnginePlayer dPlayer)) return;

        Game dGame = dPlayer.getGame();
        if(dGame == null) return;

        GamePlayer dGamePlayer = dGame.getPlayer(dPlayer.getName());
        if(dGamePlayer == null) return;

        dPlayer.getInventory().addItem(item);
    }
}
