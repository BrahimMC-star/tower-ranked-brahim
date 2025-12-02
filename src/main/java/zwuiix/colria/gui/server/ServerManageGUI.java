package zwuiix.colria.gui.server;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBreezeRod;
import cn.nukkit.item.material.ItemTypes;
import cn.nukkit.settings.ServerSettings;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.gui.sub.ComponentsMenu;
import zwuiix.colria.game.item.enchant.DummyEnchantment;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServerManageGUI {
    private EnginePlayer player;
    public VirtualInventory inventory;

    public ServerManageGUI(EnginePlayer player) {
        this.player = player;
        this.inventory = new EntityInventory(45, player.processTranslation(TranslationKeys.PLAYER_SERVER_MANAGE_GUI_TITLE));
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Server server = player.getServer();
        ServerSettings settings = server.getSettings();

        Window.fillVerticalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(8, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(36, inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        String on = t(TranslationKeys.COMMON_ON);
        String off = t(TranslationKeys.COMMON_OFF);

        Item fastpacket = new ItemBreezeRod()
                .setCustomName(t(TranslationKeys.PLAYER_SERVER_MANAGE_GUI_FASTPACKET_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SERVER_MANAGE_GUI_FASTPACKET_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(settings.player().fastestPacketHandling(), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        Item tickless = ItemTypes.REDSTONE_TORCH.createItem()
                .setCustomName(t(TranslationKeys.PLAYER_SERVER_MANAGE_GUI_TICKLESS_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SERVER_MANAGE_GUI_TICKLESS_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore(List.of(20.0f, 100.0f), server.getMaxTick()),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        if(settings.player().fastestPacketHandling()) fastpacket.addEnchantment(new DummyEnchantment());
        if(server.getMaxTick() == 100.0f) tickless.addEnchantment(new DummyEnchantment());

        addToggle(10, fastpacket, () -> settings.player().fastestPacketHandling(), (v) -> settings.player().fastestPacketHandling(v));
        addCycler(11, tickless, List.of(20.0f, 100.0f), server::getMaxTick, server::setMaxTick);
    }

    private String t(TranslationKeys k) { return player.processTranslation(k); }

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
        return ComponentsMenu.makeLore(current, enabled, disabled);
    }

    private static <T extends Comparable<? super T>> T nextOf(List<T> choices, T current) {
        int i = Collections.binarySearch(choices, current);
        int next = (i >= 0 ? i + 1 : -i - 1) % choices.size();
        return choices.get(next);
    }

    private <T extends Comparable<? super T>> void addCycler(
            int slot, Item item, List<T> choices, Supplier<T> getter, Consumer<T> setter
    ) {
        inventory.setItem(slot, item).onClick(click -> {
            setter.accept(nextOf(choices, getter.get()));
            syncContents();
        });
    }

    private void addToggle(int slot, Item icon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        inventory.setItem(
                slot,
                icon
        ).onClick(click -> {
            setter.accept(!getter.get());
            syncContents();
        });
    }

    private void addToggle(int slot, Item onIcon,
                           Item offIcon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        boolean val = getter.get();
        inventory.setItem(
                slot,
                val ? onIcon : offIcon
        ).onClick(click -> {
            setter.accept(!getter.get());
            syncContents();
        });
    }
}
