package zwuiix.colria.game.impl.team.gui;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemEnderEye;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.impl.team.Team;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.game.impl.team.TeamGameParameters;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Glyph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TeamSelectorGUI {
    private final TeamGame game;
    private final VirtualInventory inventory;

    public TeamSelectorGUI(TeamGame game) {
        this.game = game;
        this.inventory = new EntityInventory(9);
        this.syncContents();
    }

    public void syncContents() {
        final Team teamA = game.getTeamA();
        final Team teamB = game.getTeamB();

        final List<String> names1 = new ArrayList<>();
        final List<String> names2 = new ArrayList<>();
        final ArrayList<String> lores1 = new ArrayList<>();
        final ArrayList<String> lores2 = new ArrayList<>();

        for (Map.Entry<EnginePlayer, Team> entry : game.getTeams().entrySet()) {
            final Player player = entry.getKey();
            final Team team = entry.getValue();

            if (team.equals(teamA)) {
                names1.add(player.getName());
                lores1.add(TextFormat.RESET + Glyph.vbar(TextFormat.DARK_GRAY, 1) + " " + team.color() + player.getName());
            } else if (team.equals(teamB)) {
                names2.add(player.getName());
                lores2.add(TextFormat.RESET + Glyph.vbar(TextFormat.DARK_GRAY, 1) + " " + team.color() + player.getName());
            }
        }

        TeamGameParameters parameters = (TeamGameParameters) game.getParameters();
        final int max = parameters.maxPlayers;

        Item t1item = teamA.reference().clone().setCustomName(
                TextFormat.RESET + teamA.color() + Translator.getInstance().autoProcess(null, teamA.name())
                        + " §8(" + names1.size() + "/" + max + ")"
        );
        t1item.setLore(lores1.toArray(String[]::new));
        if (names1.size() >= max) t1item.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        Item t2item = teamB.reference().clone().setCustomName(
                TextFormat.RESET + teamB.color() + Translator.getInstance().autoProcess(null, teamB.name())
                        + " §8(" + names2.size() + "/" + max + ")"
        );
        t2item.setLore(lores2.toArray(String[]::new));
        if (names2.size() >= max) t2item.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        this.inventory.setItem(0, t1item).onClick(click -> {
            EnginePlayer p = (EnginePlayer) click.player();

            if (isFull(names1.size(), max) && !isAlreadyIn(game, p, teamA)) {
                return;
            }
            toggleTeam(game, p, teamA);
            syncContents();
        });

        this.inventory.setItem(1, t2item).onClick(click -> {
            EnginePlayer p = (EnginePlayer) click.player();
            if (isFull(names2.size(), max) && !isAlreadyIn(game, p, teamB)) {
                return;
            }
            toggleTeam(game, p, teamB);
            syncContents();
        });

        ItemEnderEye randomEye = new ItemEnderEye();

        if (isFull(names1.size(), max) && isFull(names2.size(), max)) {
            randomEye.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        }

        this.inventory.setItem(8, randomEye).onClick(click -> {
            EnginePlayer p = (EnginePlayer) click.player();

            int c1 = countTeam(game, teamA);
            int c2 = countTeam(game, teamB);

            Team chosen = pickRandomAvailable(teamA, teamB, c1, c2, max);
            if (chosen == null) {
                return;
            }

            game.getTeams().put(p, chosen);
            syncContents();
        });
    }

    public void send(EnginePlayer player) {
        syncContents();
        this.inventory.open(player, player.processTranslation(TranslationKeys.PLAYER_GAME_TEAM_SELECT));
    }

    private static boolean isFull(int count, int max) {
        return count >= max;
    }

    private static boolean isAlreadyIn(TeamGame game, EnginePlayer p, Team t) {
        Team cur = game.getTeams().get(p);
        return t.equals(cur);
    }

    private static void toggleTeam(TeamGame game, EnginePlayer p, Team team) {
        HashMap<EnginePlayer, Team> map = game.getTeams();
        Team current = map.get(p);
        if (team.equals(current)) {
            map.remove(p);
        } else {
            map.put(p, team);
        }
    }

    private static int countTeam(TeamGame game, Team team) {
        int c = 0;
        for (Team t : game.getTeams().values()) {
            if (team.equals(t)) c++;
        }
        return c;
    }

    private static Team pickRandomAvailable(Team t1, Team t2, int c1, int c2, int max) {
        boolean f1 = c1 >= max, f2 = c2 >= max;
        if (f1 && f2) return null;
        if (f1) return t2;
        if (f2) return t1;
        return ThreadLocalRandom.current().nextBoolean() ? t1 : t2;
    }
}
