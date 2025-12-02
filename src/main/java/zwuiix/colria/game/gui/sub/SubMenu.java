package zwuiix.colria.game.gui.sub;

import cn.nukkit.item.Item;

abstract public class SubMenu {
    private Item reference;

    public SubMenu(Item reference) {
        this.reference = reference;
    }

    public Item getReference() {
        return reference;
    }

    abstract public void sync();
}
