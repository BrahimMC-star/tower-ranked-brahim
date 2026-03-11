package zwuiix.colria.game.impl.lobby.item;

import cn.nukkit.Player;
import cn.nukkit.item.StringItemBase;
import cn.nukkit.math.Vector3;
import zwuiix.colria.gui.SettingsGUI;
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
