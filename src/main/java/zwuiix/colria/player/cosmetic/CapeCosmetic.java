package zwuiix.colria.player.cosmetic;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import lombok.Getter;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

@Getter
public class CapeCosmetic extends Cosmetic {
    private final boolean animated;

    CapeCosmetic(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost, boolean animated) {
        super(identifier, name, description, reference, cost);
        this.animated = animated;
    }

    @Override
    public void apply(EnginePlayer player) {
        player.setEnumEntityProperty("colria:cape", getIdentifier());
        player.sendData(player.getViewers().values().toArray(Player[]::new));
    }
}
