package zwuiix.colria.player.cosmetic;

import cn.nukkit.item.Item;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

abstract public class Cosmetic {
    private final String identifier;
    private final TranslationKeys name;
    private final TranslationKeys description;
    private final Item reference;
    private final long cost;

    Cosmetic(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.cost = cost;
    }

    public String getIdentifier() { return identifier; }
    public TranslationKeys getName() { return name; }
    public TranslationKeys getDescription() { return description; }
    public Item getReference() { return reference.clone(); }
    public long getCost() { return cost; }

    abstract public void apply(EnginePlayer player);
}
