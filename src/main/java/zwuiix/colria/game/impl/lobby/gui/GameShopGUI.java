package zwuiix.colria.game.impl.lobby.gui;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.sub.*;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

import java.util.ArrayList;

public class GameShopGUI {
    public EnginePlayer player;

    public VirtualInventory inventory;

    public int state = 0;
    private final ArrayList<SubMenu> subMenus = new ArrayList<>();

    public GameShopGUI(EnginePlayer player) {
        this.player = player;
        inventory = new EntityInventory(54, player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_TITLE));

        subMenus.add(new ShopHomeMenu(this, player));
        subMenus.add(new RanksMenu(this, player));
        subMenus.add(new CosmeticsMenu(this, player));
        subMenus.add(new ParticlesMenu(this, player));
        subMenus.add(new CapesMenu(this, player));
        subMenus.add(new PetsMenu(this, player));

        syncContents();
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void back() {
        state = 0;
        syncContents();
    }

    public void syncContents() {
        Item glass = new BlockCopperBars().toItem().setCustomName("§r");
        inventory.setItem(1, glass);
        Window.fillSecondLine(inventory, glass);

        if(!subMenus.isEmpty()) {
            SubMenu subMenu = subMenus.get(state);
            if(subMenu != null) {
                for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, Item.AIR_ITEM, false);
                subMenu.sync();
            }
        }
    }
}
