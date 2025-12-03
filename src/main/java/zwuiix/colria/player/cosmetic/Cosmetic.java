package zwuiix.colria.player.cosmetic;

import cn.nukkit.item.Item;
import lombok.Getter;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

abstract public class Cosmetic {
    @Getter
    private final String identifier;
    @Getter
    private final TranslationKeys name;
    @Getter
    private final TranslationKeys description;
    private final Item reference;
    @Getter
    private final long cost;

    Cosmetic(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.cost = cost;
    }

    public Item getReference() { return reference.clone(); }

    abstract public void apply(EnginePlayer player);
}
