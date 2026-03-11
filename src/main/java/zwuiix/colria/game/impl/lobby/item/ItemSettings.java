package zwuiix.colria.game.impl.lobby.item;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.StringItemBase;
import cn.nukkit.math.Vector3;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.gui.settings.SettingsGUI;
import zwuiix.colria.player.EnginePlayer;

public class ItemSettings extends StringItemBase {
    private SettingsGUI gui = null;

    public ItemSettings() {
        super(INK_SAC, "Settings");
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer) player;
        if(p.isInLobby()) {
            if(gui == null) {
                gui = new SettingsGUI(p);
            }

            gui.send();
        }
        return true;
    }
}
