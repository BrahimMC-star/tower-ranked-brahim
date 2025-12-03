package zwuiix.colria.player.cosmetic;

import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;
import zwuiix.colria.entity.EntityPet;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

public class Pet {
    @Getter
    private final Class<? extends EntityPet> entityClass;
    @Getter
    private final String identifier;
    @Getter
    private final TranslationKeys name;
    @Getter
    private final TranslationKeys description;
    private final Item reference;
    @Getter
    private final long cost;

    public Pet(Class<? extends EntityPet> entityClass, String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost) {
        this.entityClass = entityClass;
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.cost = cost;
    }

    public Item getReference() { return reference.clone(); }

    public void spawn(EnginePlayer player) {
        try {
            EntityPet pet = entityClass.getDeclaredConstructor(FullChunk.class, CompoundTag.class).newInstance(player.getChunk(), null);
            pet.setInfo(this);
            pet.setOwner(player);
            pet.setTarget(player);
            pet.setNameTagVisible(true);
            pet.spawnToAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
