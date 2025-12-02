package zwuiix.colria.game.impl.team;

import cn.nukkit.block.BlockConcrete;
import cn.nukkit.item.data.DyeColor;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.translator.TranslationKeys;

import java.util.List;
import java.util.Map;

public final class TeamColor {
    private TeamColor() {}

    private static Team C(int meta, TranslationKeys key, DyeColor dye, TextFormat fmt) {
        return new Team(key, new BlockConcrete(meta).toItem(), dye, fmt.toString());
    }

    public static final Team WHITE       = C(0,  TranslationKeys.PLAYER_GAME_TEAM_WHITE,       DyeColor.WHITE,      TextFormat.WHITE);
    public static final Team LIGHT_GRAY  = C(8,  TranslationKeys.PLAYER_GAME_TEAM_LIGHT_GRAY,  DyeColor.LIGHT_GRAY, TextFormat.GRAY);
    public static final Team GRAY        = C(7,  TranslationKeys.PLAYER_GAME_TEAM_GRAY,        DyeColor.GRAY,       TextFormat.DARK_GRAY);
    public static final Team BLACK       = C(15, TranslationKeys.PLAYER_GAME_TEAM_BLACK,       DyeColor.BLACK,      TextFormat.BLACK);
    public static final Team BROWN       = C(12, TranslationKeys.PLAYER_GAME_TEAM_BROWN,       DyeColor.BROWN,      TextFormat.MATERIAL_COPPER);
    public static final Team RED         = C(14, TranslationKeys.PLAYER_GAME_TEAM_RED,         DyeColor.RED,        TextFormat.RED);
    public static final Team ORANGE      = C(1,  TranslationKeys.PLAYER_GAME_TEAM_ORANGE,      DyeColor.ORANGE,     TextFormat.GOLD);
    public static final Team YELLOW      = C(4,  TranslationKeys.PLAYER_GAME_TEAM_YELLOW,      DyeColor.YELLOW,     TextFormat.YELLOW);
    public static final Team LIME        = C(5,  TranslationKeys.PLAYER_GAME_TEAM_LIME,        DyeColor.LIME,       TextFormat.GREEN);
    public static final Team GREEN       = C(13, TranslationKeys.PLAYER_GAME_TEAM_GREEN,       DyeColor.GREEN,      TextFormat.DARK_GREEN);
    public static final Team CYAN        = C(9,  TranslationKeys.PLAYER_GAME_TEAM_CYAN,        DyeColor.CYAN,       TextFormat.AQUA);
    public static final Team LIGHT_BLUE  = C(3,  TranslationKeys.PLAYER_GAME_TEAM_LIGHT_BLUE,  DyeColor.LIGHT_BLUE, TextFormat.BLUE);
    public static final Team BLUE        = C(11, TranslationKeys.PLAYER_GAME_TEAM_BLUE,        DyeColor.BLUE,       TextFormat.DARK_BLUE);
    public static final Team PURPLE      = C(10, TranslationKeys.PLAYER_GAME_TEAM_PURPLE,      DyeColor.PURPLE,     TextFormat.MATERIAL_AMETHYST);
    public static final Team MAGENTA     = C(2,  TranslationKeys.PLAYER_GAME_TEAM_MAGENTA,     DyeColor.MAGENTA,    TextFormat.DARK_PURPLE);
    public static final Team PINK        = C(6,  TranslationKeys.PLAYER_GAME_TEAM_PINK,        DyeColor.PINK,       TextFormat.LIGHT_PURPLE);

    public static final List<Team> ALL = List.of(
            WHITE, LIGHT_GRAY, GRAY, BLACK, BROWN, RED, ORANGE, YELLOW,
            LIME, GREEN, CYAN, LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK
    );

    public static final Map<Team, Team> OPPOSITE = Map.ofEntries(
            Map.entry(WHITE, BLACK),        Map.entry(BLACK, WHITE),
            Map.entry(LIGHT_GRAY, GRAY),    Map.entry(GRAY, LIGHT_GRAY),
            Map.entry(BROWN, LIGHT_BLUE),   Map.entry(LIGHT_BLUE, BROWN),
            Map.entry(RED, CYAN),           Map.entry(CYAN, RED),
            Map.entry(ORANGE, BLUE),        Map.entry(BLUE, ORANGE),
            Map.entry(YELLOW, PURPLE),      Map.entry(PURPLE, YELLOW),
            Map.entry(LIME, MAGENTA),       Map.entry(MAGENTA, LIME),
            Map.entry(GREEN, PINK),         Map.entry(PINK, GREEN)
    );

    public static Team oppositeOf(Team team) {
        return OPPOSITE.getOrDefault(team, team);
    }
}
