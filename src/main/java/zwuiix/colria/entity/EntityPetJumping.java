package zwuiix.colria.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.*;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Vector3fEntityData;
import cn.nukkit.entity.mob.EntityJumpingMob;
import cn.nukkit.entity.passive.EntityJumpingAnimal;
import cn.nukkit.entity.passive.EntityWalkingAnimal;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.FastMath;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.Pet;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import static cn.nukkit.network.protocol.SetEntityLinkPacket.TYPE_RIDE;

@Getter
@Setter
abstract public class EntityPetJumping extends EntityWalkingAnimal implements EntityRideable, EntityControllable, EntityClimateVariant, EntityPet {
    private Pet info;
    private EnginePlayer owner = null;
    private boolean saddled = false;

    public EntityPetJumping(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();

        if (namedTag.contains("variant")) {
            setVariant(Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }

        if (this.namedTag.contains("Saddle")) {
            this.setSaddled(this.namedTag.getBoolean("Saddle"));
        }
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
        this.setBothYaw(player.yaw);

        strafe *= getPetSpeed();

        double f = strafe * strafe + forward * forward;
        double friction = 0.3;

        if (f >= 1.0E-4) {
            f = Math.sqrt(f);

            if (f < 1) {
                f = 1;
            }

            f = friction / f;
            strafe *= f;
            forward *= f;
            double f1 = Math.sin(this.yaw * 0.017453292);
            double f2 = Math.cos(this.yaw * 0.017453292);
            this.motionX = (strafe * f2 - forward * f1);
            this.motionZ = (forward * f2 + strafe * f1);
        } else {
            this.motionX = 0;
            this.motionZ = 0;
        }
    }

    @Override
    protected void checkTarget() {
        if(passengers.isEmpty()) {
            originalCheckTarget();
            return;
        }


        var rider = this.getPassengers().getFirst();
        if(!(rider instanceof Player)) {
            originalCheckTarget();
            return;
        }
    }

    private void originalCheckTarget() {
        if (this.isKnockback()) {
            return;
        }

        if (this.followTarget != null && !this.followTarget.closed && this.followTarget.isAlive() && this.followTarget.canBeFollowed()) {
            return;
        }

        Vector3 target = this.target;
        if (!(target instanceof EntityCreature) || (!((EntityCreature) target).closed && !this.targetOption((EntityCreature) target, this.distanceSquared(target))) || !((Entity) target).canBeFollowed()) {
            double near = Integer.MAX_VALUE;

            for (Entity entity : this.getLevel().getEntities()) {
                if (entity == this || !(entity instanceof EntityCreature creature) || entity.closed || !this.canTarget(entity)) {
                    continue;
                }

                if (creature instanceof BaseEntity && ((BaseEntity) creature).isFriendly() == this.isFriendly()) {
                    continue;
                }

                double distance = this.distanceSquared(creature);
                if (distance > near || !this.targetOption(creature, distance)) {
                    continue;
                }
                near = distance;

                this.stayTime = 0;
                this.moveTime = 0;
                this.target = creature;
            }
        }

        if (this.target instanceof EntityCreature && !((EntityCreature) this.target).closed && ((EntityCreature) this.target).isAlive() && this.targetOption((EntityCreature) this.target, this.distanceSquared(this.target))) {
            return;
        }

        int x, z;
        if (this.stayTime > 0) {
            if (Utils.rand(1, 100) > 5) {
                return;
            }
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.target = this.add(Utils.rand() ? x : -x, Utils.rand(-20.0, 20.0) / 10, Utils.rand() ? z : -z);
        } else if (Utils.rand(1, 100) == 1) {
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.stayTime = Utils.rand(100, 200);
            this.target = this.add(Utils.rand() ? x : -x, Utils.rand(-20.0, 20.0) / 10, Utils.rand() ? z : -z);
        } else if (this.moveTime <= 0 || this.target == null) {
            x = Utils.rand(20, 100);
            z = Utils.rand(20, 100);
            this.stayTime = 0;
            this.moveTime = Utils.rand(100, 200);
            this.target = this.add(Utils.rand() ? x : -x, 0, Utils.rand() ? z : -z);
        }
    }

    protected boolean checkJump() {
        if (this.motionY == this.getGravity() * 2) {
            return this.canSwimIn(level.getBlockIdAt(NukkitMath.floorDouble(this.x), (int) this.y, NukkitMath.floorDouble(this.z)));
        } else {
            if (this.canSwimIn(level.getBlockIdAt(NukkitMath.floorDouble(this.x), (int) (this.y + 0.8), NukkitMath.floorDouble(this.z)))) {
                this.motionY = this.getGravity() * 2;
                return true;
            }
        }

        if (!this.onGround) {
            return false;
        }

        if (this.motionX > 0 || this.motionZ > 0) {
            if (this.motionY <= (this.getGravity() * 5)) {
                this.motionY = this.getGravity() * 5;
            } else {
                this.motionY += this.getGravity() * 0.25;
            }
        }

        return false;
    }

    @Override
    public Vector3 updateMove(int tickDiff) {
        if (!this.isInTickingRange()) {
            return null;
        }

        if (this.isMovement() && !isImmobile()) {
            if (this.isKnockback()) {
                this.move(this.motionX, this.motionY, this.motionZ);
                this.motionY -= this.getGravity();
                this.updateMovement();
                return null;
            }

            if (this.getServer().getSettings().world().entity().mobAi()) {
                if (this.followTarget != null && !this.followTarget.closed && this.followTarget.isAlive() && this.followTarget.canBeFollowed()) {
                    double x = this.followTarget.x - this.x;
                    double z = this.followTarget.z - this.z;

                    double diff = Math.abs(x) + Math.abs(z);
                    if (diff == 0 ||this.stayTime > 0 || this.distance(this.followTarget) <= (this.getWidth() / 2 + 0.05)) {
                        this.motionX = 0;
                        this.motionZ = 0;
                    } else {
                        if (this.isInsideOfWater()) {
                            this.motionX = this.getSpeed() * 0.05 * (x / diff);
                            this.motionZ = this.getSpeed() * 0.05 * (z / diff);
                            this.level.addParticle(new BubbleParticle(this.add(Utils.rand(-2.0, 2.0), Utils.rand(-0.5, 0), Utils.rand(-2.0, 2.0))));
                        } else {
                            this.motionX = this.getSpeed() * 0.1 * (x / diff);
                            this.motionZ = this.getSpeed() * 0.1 * (z / diff);
                        }
                    }
                    if ((this.stayTime <= 0 || Utils.rand()) && diff != 0) {
                        this.setBothYaw(FastMath.toDegrees(-FastMath.atan2(x / diff, z / diff)));
                    }
                    return this.followTarget;
                }

                Vector3 before = this.target;
                this.checkTarget();
                if (this.target instanceof EntityCreature || before != this.target) {
                    double x = this.target.x - this.x;
                    double z = this.target.z - this.z;

                    double diff = Math.abs(x) + Math.abs(z);
                    if (diff == 0 || this.stayTime > 0 || (this.distance(this.target) <= (this.getWidth() / 2 + 0.05) * nearbyDistanceMultiplier() && !this.isInsideOfWater())) {
                        this.motionX = 0;
                        this.motionZ = 0;
                    } else {
                        if (this.isInsideOfWater()) {
                            this.motionX = this.getSpeed() * 0.05 * (x / diff);
                            this.motionZ = this.getSpeed() * 0.05 * (z / diff);
                            this.level.addParticle(new BubbleParticle(this.add(Utils.rand(-2.0, 2.0), Utils.rand(-0.5, 0), Utils.rand(-2.0, 2.0))));
                        } else {
                            this.motionX = this.getSpeed() * 0.15 * (x / diff);
                            this.motionZ = this.getSpeed() * 0.15 * (z / diff);
                        }
                    }
                    if ((this.stayTime <= 0 || Utils.rand()) && diff != 0) {
                        this.setBothYaw(FastMath.toDegrees(-FastMath.atan2(x / diff, z / diff)));
                    }
                }
            }

            double dx = this.motionX;
            double dz = this.motionZ;
            boolean isJump = this.checkJump();
            if (this.stayTime > 0) {
                this.stayTime -= tickDiff;
                this.move(0, this.motionY, 0);
            } else {
                Vector2 be = new Vector2(this.x + dx, this.z + dz);
                this.move(dx, this.motionY, dz);
                Vector2 af = new Vector2(this.x, this.z);

                if ((be.x != af.x || be.y != af.y) && !isJump) {
                    this.moveTime -= 90;
                }
            }

            if (!isJump) {
                if (this.onGround) {
                    this.motionY = 0;
                } else if (this.motionY > -this.getGravity() * 4) {
                    int b = this.level.getBlockIdAt(chunk, NukkitMath.floorDouble(this.x), (int) (this.y + 0.8), NukkitMath.floorDouble(this.z));
                    if (b != Block.WATER && b != Block.STILL_WATER && b != Block.LAVA && b != Block.STILL_LAVA) {
                        this.motionY -= this.getGravity();
                    }
                } else {
                    this.motionY -= this.getGravity();
                }
            }
            this.updateMovement();
            return this.target;
        }
        return null;
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
    public boolean targetOption(EntityCreature creature, double distance) { return super.targetOption(creature, 5) && creature == this.owner && creature.distance(this) >= 16.0f; }

    @Override
    public boolean attack(EntityDamageEvent ev) { return false; }

    abstract public float getPetSpeed();
}
