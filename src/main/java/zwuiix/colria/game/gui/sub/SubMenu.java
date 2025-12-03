package zwuiix.colria.game.gui.sub;

import cn.nukkit.item.Item;
import lombok.Getter;

@Getter
abstract public class SubMenu {
    private final Item reference;

    public SubMenu(Item reference) {
        this.reference = reference;
    }

    abstract public void sync();
}
