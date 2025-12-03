package zwuiix.colria.player.cosmetic;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
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
    private final boolean saddled;
    @Getter
    private final long cost;

    public Pet(Class<? extends EntityPet> entityClass, String identifier, TranslationKeys name, TranslationKeys description, Item reference, boolean saddled, long cost) {
        this.entityClass = entityClass;
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.saddled = saddled;
        this.cost = cost;
    }

    public Item getReference() { return reference.clone(); }

    public void spawn(EnginePlayer player) {
        var source = player.getPosition().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1);
        CompoundTag nbt = new CompoundTag().putList(new ListTag<DoubleTag>("Pos").add(new DoubleTag("", source.x)).add(new DoubleTag("", source.y)).add(new DoubleTag("", source.z)))
                .putList(new ListTag<DoubleTag>("Motion").add(new DoubleTag("", 0)).add(new DoubleTag("", 0)).add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation").add(new FloatTag("", source instanceof Location ? (float) ((Location) source).yaw : 0))
                        .add(new FloatTag("", source instanceof Location ? (float) ((Location) source).pitch : 0)));

        try {
            var constructor = entityClass.getDeclaredConstructor(FullChunk.class, CompoundTag.class);
            EntityPet pet = constructor.newInstance(player.getChunk(), nbt);
            pet.setInfo(this);
            pet.setOwner(player);
            pet.setTarget(player);
            pet.spawnToAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
