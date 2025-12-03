package zwuiix.colria.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.*;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Vector3fEntityData;
import cn.nukkit.entity.mob.EntityZombiePigman;
import cn.nukkit.entity.passive.EntityWalkingAnimal;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.Pet;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static cn.nukkit.network.protocol.SetEntityLinkPacket.TYPE_RIDE;

@Getter
@Setter
abstract public class EntityPet extends EntityWalkingAnimal implements EntityRideable, EntityControllable, EntityClimateVariant {
    private Pet info;
    private EnginePlayer owner = null;
    private boolean saddled = false;

    public EntityPet(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();

        if (namedTag.contains("variant")) {
            setVariant(EntityClimateVariant.Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }

        if (this.namedTag.contains("Saddle")) {
            this.setSaddled(this.namedTag.getBoolean("Saddle"));
        }
    }

    @Override
    public boolean isFeedItem(Item item) {
        return true;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        EnginePlayer p = (EnginePlayer) player;
        if(this.owner != p) {
            p.sendMessage(TranslationKeys.PET_CANT_MOUNT, this.owner.getName());
            return false;
        }
        
        if (this.isSaddled() && this.passengers.isEmpty() && !this.isBaby() && !player.isSneaking()) {
            if (player.riding == null) {
                this.mountEntity(player);
            }
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public int getKillExperience() { return 0; }

    @Override
    public boolean mountEntity(Entity entity, byte mode) {
        Objects.requireNonNull(entity, "The target of the mounting entity can't be null");
        if (entity instanceof Player player && player.isSleeping()) {
            return false;
        }

        if (entity.riding != null) {
            dismountEntity(entity);
            this.motionX = 0;
            this.motionZ = 0;
            this.stayTime = 20;
        } else {
            if (isPassenger(entity)) {
                return false;
            }

            broadcastLinkPacket(entity, TYPE_RIDE);

            entity.riding = this;
            entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);
            entity.setDataProperty(new Vector3fEntityData(DATA_RIDER_SEAT_POSITION, this.getSeatPosition()));
            entity.setDataProperty(new FloatEntityData(DATA_RIDER_MAX_ROTATION, 181));
            passengers.add(entity);
        }

        return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        Iterator<Entity> linkedIterator = this.passengers.iterator();

        while (linkedIterator.hasNext()) {
            Entity linked = linkedIterator.next();

            if (!linked.isAlive()) {
                if (linked.riding == this) {
                    linked.riding = null;
                }

                linkedIterator.remove();
            }
        }

        var onUpdate = super.onUpdate(currentTick);
        if(onUpdate) {
            if (this.owner == null || !this.owner.isOnline()) {
                this.close();
                return false;
            }

            if(this.owner.getLevel().getId() != this.getLevel().getId()) {
                this.close();
                return false;
            }

            if(currentTick % 20 == 0) {
                for (Player player : getViewers().values()) {
                    EnginePlayer p = (EnginePlayer) player;
                    var name = p.processTranslation(this.info.getName());

                    this.sendData(p, new EntityMetadata().putString(Entity.DATA_NAMETAG, p.processTranslation(TranslationKeys.PET_NAMETAG, name, this.owner.getName())));
                }
            }
        }

        return onUpdate;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("Saddle", this.isSaddled());
    }

    public void setSaddled(boolean saddled) {
        this.saddled = saddled;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SADDLED, saddled);
    }

    @Override
    public void onPlayerInput(Player player, double strafe, double forward) {
        this.stayTime = 0;
        this.moveTime = 10;
        this.setPitch(player.pitch);
        this.setBothYaw(player.yaw);

        double speedFactor = 2.5 * this.getSpeed();

        Vector2 directionPlane = this.getDirectionPlane();
        double x = directionPlane.getX() / speedFactor;
        double z = directionPlane.getY() / speedFactor;

        if (forward == 1) {
            this.motionX += x;
            this.motionZ += z;
        } else if (forward == -1) {
            this.motionX -= x;
            this.motionZ -= z;
        }

        if (strafe == 1) {
            this.motionX += -z;
            this.motionZ += x;
        } else if (strafe == -1) {
            this.motionX += z;
            this.motionZ += -x;
        }

        if (forward != 0 && strafe != 0) {
            this.motionX *= 1.0 / Math.sqrt(2.0);
            this.motionZ *= 1.0 / Math.sqrt(2.0);
        }

        this.move(this.motionX, this.motionY, this.motionZ);
        this.updateMovement();
        this.broadcastMovement();
    }

    @Override
    protected void checkTarget() {
        if(passengers.isEmpty()) {
            super.checkTarget();
            return;
        }


        var rider = this.getPassengers().getFirst();
        if(!(rider instanceof Player)) {
            super.checkTarget();
            return;
        }
    }

    @Override
    public boolean canDespawn() { return false; }

    @Override
    public void updatePassengers() {
        if (this.passengers.isEmpty()) {
            return;
        }

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            if (!passenger.isAlive() || this.isInsideOfWater()) {
                dismountEntity(passenger);
                continue;
            }

            updatePassengerPosition(passenger);
        }
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);

        if (this.saddled && this.isAlive() && !this.passengers.isEmpty()) {
            UpdateAttributesPacket pk = new UpdateAttributesPacket();
            int max = this.getMaxHealth();
            pk.entries = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(max).setValue(this.health < max ? this.health : max)};
            pk.entityId = this.id;
            Server.broadcastPacket(this.getViewers().values(), pk);
        }
    }

    @Override
    public boolean canTarget(Entity entity) { return entity == this.owner; }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) { return super.targetOption(creature, 5) && creature == this.owner && creature.distance(this) >= 2.0f; }

    @Override
    public boolean attack(EntityDamageEvent ev) { return false; }

    abstract public float getPetSpeed();
}
