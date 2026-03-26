package zwuiix.colria.game.gui.sub;

import cn.nukkit.block.*;
import cn.nukkit.item.*;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.team.TeamGameParameters;
import zwuiix.colria.game.impl.tower.TowerGameParameters;
import zwuiix.colria.game.impl.towerbridge.TowerBridgeGameParameters;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static java.util.Map.entry;

public class ComponentsMenu extends SubMenu {
    private final GameSettingsGUI parent;

    public ComponentsMenu(GameSettingsGUI parent) {
        super(new ItemClay().setCustomName(parent.player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_COMPONENTS_NAME)));
        this.parent = parent;
    }

    @Override
    public void sync() {
        final List<Long> timeChoices       = List.of(5L, 8L, 10L, 12L, 15L, 20L);
        final List<Long> respawnChoices    = LongStream.rangeClosed(1, 10).boxed().toList();
        final List<Integer> difficultyChoices = List.of(0, 1, 2);

        String on = t(TranslationKeys.COMMON_ON);
        String off = t(TranslationKeys.COMMON_OFF);

        Window.fillVerticalLine(9, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(17, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        var p = parent.game.getParameters();
        Item timeLimit = new ItemCompass()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TIMELIMIT_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TIMELIMIT_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(timeChoices, p.timeLimit),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        Item respawnTime = new ItemAmethystShard()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_RESPAWNTIME_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_RESPAWNTIME_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(respawnChoices, p.respawnTime),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        Item difficulty = new ItemFireCharge()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DIFFICULTY_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DIFFICULTY_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(difficultyChoices, p.difficulty),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        Item critical = new ItemSwordWood();
        if(p.critical) critical = new ItemSwordStone();
        critical.setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_CRITICAL_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_CRITICAL_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(p.critical, on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        ItemFeather fallDamage = (ItemFeather) new ItemFeather()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_FALLDAMAGE_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_FALLDAMAGE_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(p.fallDamage, on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        ItemSteak naturalRegeneration = (ItemSteak) new ItemSteak()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_NATURALREGENERATION_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_NATURALREGENERATION_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(p.naturalGeneration, on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        ItemSwordCopper ace = (ItemSwordCopper) new ItemSwordCopper()
                .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_ACE_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_ACE_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(p.ace, on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        addCycler(timeLimit, timeChoices, () -> p.timeLimit, v -> p.timeLimit = v);
        addCycler(respawnTime, respawnChoices, () -> p.respawnTime, v -> p.respawnTime = v);
        addCycler(difficulty, difficultyChoices, () -> p.difficulty, v -> p.difficulty = v);
        addToggle(critical, () -> p.critical, v -> p.critical = v);
        addToggle(fallDamage, () -> p.fallDamage, v -> p.fallDamage = v);
        addToggle(naturalRegeneration, () -> p.naturalGeneration, v -> p.naturalGeneration = v);
        addToggle(ace, () -> p.ace, v -> p.ace = v);

        processNext();
    }

    private void processNext() {
        String on = t(TranslationKeys.COMMON_ON);
        String off = t(TranslationKeys.COMMON_OFF);

        var p = parent.game.getParameters();
        if (p instanceof TeamGameParameters teamParams) {
            List<Integer> minimumPlayersChoice = List.of(1, 2, 3);
            List<Integer> maximumPlayersChoice = List.of(5, 6, 7, 8, 9, 10);

            if (p instanceof TowerBridgeGameParameters) {
                minimumPlayersChoice = List.of(1, 2, 3);
                maximumPlayersChoice = List.of(1, 2, 3, 4);
            }

            ItemRedstone allyDamage = (ItemRedstone) new ItemRedstone()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_ALLYDAMAGE_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_ALLYDAMAGE_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(teamParams.allyDamage, on, off),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item spawnKill = new BlockHeadPlayer().toItem()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SPAWNKILL_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SPAWNKILL_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(teamParams.spawnKill, on, off),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item minPlayers = new BlockIron().toItem()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MINPLAYERS_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MINPLAYERS_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(minimumPlayersChoice, teamParams.minPlayers),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item maxPlayers = new BlockGold().toItem()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MAXPLAYERS_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MAXPLAYERS_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(maximumPlayersChoice, teamParams.maxPlayers),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            addToggle(allyDamage, () -> teamParams.allyDamage, v -> teamParams.allyDamage = v);
            addToggle(spawnKill, () -> teamParams.spawnKill, v -> teamParams.spawnKill = v);
            addCycler(minPlayers, minimumPlayersChoice, () -> teamParams.minPlayers, v -> teamParams.minPlayers = v);
            addCycler(maxPlayers, maximumPlayersChoice, () -> teamParams.maxPlayers, v -> teamParams.maxPlayers = v);
        }

        if (p instanceof TowerGameParameters towerParams) {
            final List<Integer> applesChoices = List.of(5, 10, 15, 20, 30, 40, 50, 60);
            final List<Integer> pointsChoice = List.of(3, 5, 8, 10, 12, 15, 20);
            final List<Integer> despawnChoices = List.of(10, 20, 30, 40, 50, 60);

            Item glint = new ItemBookEnchanted()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_GLINT_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_GLINT_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(towerParams.glint, on, off),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item appleGenerator = new ItemAppleGold()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_APPLEGENERATOR_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_APPLEGENERATOR_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(applesChoices, towerParams.appleGenerator),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item maxPoints = new ItemTrialKey()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MAXPOINTS_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MAXPOINTS_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(pointsChoice, towerParams.maxPoints),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item despawnBlocks = new BlockCobblestone().toItem()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNBLOCKS_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNBLOCKS_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLore(despawnChoices, towerParams.despawnBlocks),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item mark = new ItemEmerald()
                    .setCustomName(TextFormat.RESET + "§-§b§g"+ TextFormat.RESET + t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLoreEnum(choicesOf(TowerGameParameters.MarkReward.class), Map.ofEntries(
                                    entry(TowerGameParameters.MarkReward.REGEN, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_REGEN),
                                    entry(TowerGameParameters.MarkReward.APPLE, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_APPLE),
                                    entry(TowerGameParameters.MarkReward.BLOCKS, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_BLOCKS),
                                    entry(TowerGameParameters.MarkReward.REGEN_AND_APPLE, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_REGEN_AND_APPLE),
                                    entry(TowerGameParameters.MarkReward.REGEN_AND_BLOCKS, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_REGEN_AND_BLOCKS),
                                    entry(TowerGameParameters.MarkReward.APPLE_AND_BLOCKS, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_APPLE_AND_BLOCKS),
                                    entry(TowerGameParameters.MarkReward.ALL, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_ALL)
                            ), towerParams.markReward),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            Item despawn = new ItemPhantomMembrane()
                    .setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNANIMATION_NAME))
                    .setLore(
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNANIMATION_LORE),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            makeLoreEnum(choicesOf(TowerGameParameters.DespawnAnimation.class), Map.ofEntries(
                                    entry(TowerGameParameters.DespawnAnimation.INSTANT, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNANIMATION_INSTANT),
                                    entry(TowerGameParameters.DespawnAnimation.QUICK, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNANIMATION_QUICK),
                                    entry(TowerGameParameters.DespawnAnimation.PROGRESSIVE, TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DESPAWNANIMATION_PROGRESSIVE)
                            ), towerParams.despawnAnimation),
                            Glyph.hbarThick(EngineInfo.COLOR, 1),
                            t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                    );

            addToggle(glint, () -> towerParams.glint, v -> towerParams.glint = v);
            addCycler(appleGenerator, applesChoices, () -> towerParams.appleGenerator, v -> towerParams.appleGenerator = v);
            addCycler(maxPoints, pointsChoice, () -> towerParams.maxPoints, v -> towerParams.maxPoints = v);
            addCycler(mark, choicesOf(TowerGameParameters.MarkReward.class), () -> towerParams.markReward, v -> towerParams.markReward = v);

            boolean regen = towerParams.markReward.equals(TowerGameParameters.MarkReward.ALL) || towerParams.markReward.equals(TowerGameParameters.MarkReward.REGEN) || towerParams.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_APPLE) || towerParams.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_BLOCKS);
            if (regen) {
                final List<Integer> regenChoices = List.of(5, 10, 15, 20);
                Item regenItem = new BlockBrewingStand().toItem()
                        .setCustomName(TextFormat.RESET + "§b§g" + TextFormat.RESET + t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_REGEN))
                        .setLore(
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_REGEN_LORE),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                makeLore(regenChoices, towerParams.regen),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                        );
                addCycler(regenItem, regenChoices, () -> towerParams.regen, (v) -> towerParams.regen = v);
            }

            boolean apple = towerParams.markReward.equals(TowerGameParameters.MarkReward.ALL) || towerParams.markReward.equals(TowerGameParameters.MarkReward.APPLE) || towerParams.markReward.equals(TowerGameParameters.MarkReward.APPLE_AND_BLOCKS) || towerParams.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_APPLE);
            if (apple) {
                final List<Integer> appleChoices = List.of(1, 2, 3, 4, 5, 6);
                Item appleItem = new ItemApple()
                        .setCustomName(TextFormat.RESET + "§b§g" + TextFormat.RESET + t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_APPLE))
                        .setLore(
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_APPLE_LORE),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                makeLore(appleChoices, towerParams.apple),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                        );
                addCycler(appleItem, appleChoices, () -> towerParams.apple, (v) -> towerParams.apple = v);
            }

            boolean blocks = towerParams.markReward.equals(TowerGameParameters.MarkReward.ALL) || towerParams.markReward.equals(TowerGameParameters.MarkReward.BLOCKS) || towerParams.markReward.equals(TowerGameParameters.MarkReward.APPLE_AND_BLOCKS) || towerParams.markReward.equals(TowerGameParameters.MarkReward.REGEN_AND_BLOCKS);
            if (blocks) {
                final List<Integer> blocksChoices = List.of(4, 6, 8, 10, 12, 14, 16, 32);
                Item blocksItem = new BlockBricksStone().toItem()
                        .setCustomName(TextFormat.RESET + "§b§g" + TextFormat.RESET + t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_BLOCKS))
                        .setLore(
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MARKREWARD_BLOCKS_LORE),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                makeLore(blocksChoices, towerParams.blocks),
                                Glyph.hbarThick(EngineInfo.COLOR, 1),
                                t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                        );
                addCycler(blocksItem, blocksChoices, () -> towerParams.blocks, (v) -> towerParams.blocks = v);
            }

            addCycler(despawnBlocks, despawnChoices, () -> towerParams.despawnBlocks, v -> towerParams.despawnBlocks = v);
            addCycler(despawn, choicesOf(TowerGameParameters.DespawnAnimation.class), () -> towerParams.despawnAnimation, v -> towerParams.despawnAnimation = v);
        }
    }

    private String t(TranslationKeys k) { return parent.player.processTranslation(k); }

    private <E extends Enum<E>> String makeLoreEnum(List<E> choices, Map<E, TranslationKeys> dict, E current) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        for (E choice : choices) {
            sb.append("&7&8")
                    .append(Glyph.vbar(TextFormat.DARK_GRAY, 1))
                    .append(" ")
                    .append((choice == current) ? "&a&l" : "&c")
                    .append(t(dict.get(choice)))
                    .append("&r&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    private static <T> String makeLore(List<T> choices, T current) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        for (T c : choices) {
            boolean sel = c.equals(current);
            sb.append("&7&8")
                    .append(Glyph.vbar(TextFormat.DARK_GRAY, 1))
                    .append(" ")
                    .append(sel ? "&a" : "&c")
                    .append(c)
                    .append(sel ? "&r&7\n" : "&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    public static String makeLore(Boolean current, String enabled, String disabled) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        if (current) {
            sb.append("&7&8&l").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &a").append(enabled).append("&r&7\n");
            sb.append("&7&8").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &c").append(disabled).append("&r&7\n");
        } else {
            sb.append("&7&8").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &c").append(enabled).append("&r&7\n");
            sb.append("&7&8&l").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &a").append(disabled).append("&r&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    private static <E extends Enum<E>> List<E> choicesOf(Class<E> enumType) {
        return Arrays.asList(enumType.getEnumConstants());
    }

    private static <T extends Comparable<? super T>> T nextOf(List<T> choices, T current) {
        int i = Collections.binarySearch(choices, current);
        int next = (i >= 0 ? i + 1 : -i - 1) % choices.size();
        return choices.get(next);
    }

    private <T extends Comparable<? super T>> void addCycler(
            Item item, List<T> choices, Supplier<T> getter, Consumer<T> setter
    ) {
        parent.inventory.setItem(Window.nextSlot(parent.inventory), item).onClick(click -> {
            setter.accept(nextOf(choices, getter.get()));
            parent.syncContents();
        });
    }

    private void addToggle(Item icon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        parent.inventory.setItem(
                Window.nextSlot(parent.inventory),
                icon
        ).onClick(click -> {
            setter.accept(!getter.get());
            parent.syncContents();
        });
    }

    private void addToggle(Item onIcon,
                           Item offIcon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        boolean val = getter.get();
        parent.inventory.setItem(
                Window.nextSlot(parent.inventory),
                val ? onIcon : offIcon
        ).onClick(click -> {
            setter.accept(!getter.get());
            parent.syncContents();
        });
    }
}
