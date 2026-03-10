package zwuiix.colria.util;

import cn.nukkit.bossbar.BossBarColor;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.BossEventPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.types.EntityLink;
import lombok.Getter;
import zwuiix.colria.player.EnginePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBar {
    @Getter
    private String text;
    private final HashMap<UUID, EnginePlayer> viewers = new HashMap<>();
    @Getter
    private BossBarColor color;
    private final Attribute attribute = Attribute.getAttribute(Attribute.MAX_HEALTH);

    private long eId;

    public BossBar() {
        this.text = "";
        this.color = BossBarColor.PURPLE;
        this.attribute.setMaxValue(100.0f);
        this.attribute.setValue(100.0f);
        this.eId = Entity.entityCount++;
    }

    public BossBar updateColor(BossBarColor color) {
        this.color = color;
        syncColor();
        return this;
    }

    public void updateText(String text) {
        this.text = text;
        for (EnginePlayer player : viewers.values()) {
            syncText(player, text);
        }
    }

    public void setPercentage(float percentage) {
        if (percentage < 0.0f) percentage = 0.0f;
        if (percentage > 1.0f) percentage = 1.0f;

        this.attribute.setValue(this.attribute.getMaxValue() * percentage);
        syncPercentage();
    }

    public float getPercentage() {
        return attribute.getValue() / attribute.getMaxValue();
    }

    public Map<UUID, EnginePlayer> getViewers() {
        return viewers;
    }

    public void addViewer(EnginePlayer player) {
        addViewer(player, false);
    }

    public void addViewer(EnginePlayer player, boolean force) {
        if(viewers.containsKey(player.getUniqueId()))
            return;

        if (!force) {
            var settings = player.getPlayerDataInfo().getSettings();
            if (!(Boolean) settings.getOrDefault("boss_bar", "enabled", true)) {
                return;
            }
        }

        viewers.put(player.getUniqueId(), player);
        show(player);
        syncColor(player, this.color);
        syncPercentage(player, getPercentage());
        syncText(player, this.text);
    }

    public void removeViewers() {
        for (EnginePlayer player : viewers.values()) {
            hide(player);
        }
        viewers.clear();
    }

    public void removeViewer(EnginePlayer player) {
        if(!viewers.containsKey(player.getUniqueId()))
            return;

        viewers.remove(player.getUniqueId());
        hide(player);
    }

    public void syncPercentage() {
        for (EnginePlayer player : viewers.values()) {
            syncPercentage(player, getPercentage());
        }
    }

    public void syncPercentage(EnginePlayer player, float perc) {
        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = eId;
        pk.type = BossEventPacket.TYPE_HEALTH_PERCENT;
        pk.healthPercent = perc;
        player.dataPacket(pk);
    }

    public void syncText(EnginePlayer player, String text) {
        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = eId;
        pk.type = BossEventPacket.TYPE_TITLE;
        pk.title = text;
        pk.filteredTitle = text;
        player.dataPacket(pk);
    }

    public void syncColor() {
        for (EnginePlayer player : viewers.values()) {
            syncColor(player, this.color);
        }
    }

    public void syncColor(EnginePlayer player, BossBarColor color) {
        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = eId;
        pk.type = BossEventPacket.TYPE_TEXTURE;
        pk.color = color.ordinal();
        player.dataPacket(pk);
    }

    public void show() {
        for (EnginePlayer player : viewers.values()) {
            show(player);
        }
    }

    public void show(EnginePlayer player) {
        EntityMetadata metadata = new EntityMetadata();
        metadata.putBoolean(Entity.DATA_FLAG_INVISIBLE, true);
        metadata.putFloat(Entity.DATA_BOUNDING_BOX_WIDTH, 0.0f);
        metadata.putFloat(Entity.DATA_BOUNDING_BOX_HEIGHT, 0.0f);
        metadata.putFloat(Entity.DATA_SCALE, 0.0f);

        var addEntityPacket = new AddEntityPacket();
        addEntityPacket.entityUniqueId = eId;
        addEntityPacket.entityRuntimeId = eId;
        addEntityPacket.id = "minecraft:chicken";
        addEntityPacket.x = (float) player.x;
        addEntityPacket.y = (float) player.y;
        addEntityPacket.z = (float) player.z;
        addEntityPacket.speedX = 0;
        addEntityPacket.speedY = 0;
        addEntityPacket.speedZ = 0;
        addEntityPacket.yaw = 0;
        addEntityPacket.pitch = 0;
        addEntityPacket.headYaw = 0;
        addEntityPacket.bodyYaw = 0;
        addEntityPacket.metadata = metadata;
        addEntityPacket.links = new EntityLink[]{new EntityLink(player.getId(), eId, EntityLink.TYPE_RIDER, true, true, 0)};
        player.dataPacket(addEntityPacket);

        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = eId;
        pk.type = BossEventPacket.TYPE_SHOW;
        pk.title = this.text;
        pk.healthPercent = this.getPercentage();
        pk.filteredTitle = this.text;
        pk.color = color.ordinal();
        player.dataPacket(pk);
    }

    public void hide() {
        for (EnginePlayer player : viewers.values()) {
            hide(player);
        }
        viewers.clear();
    }

    public void hide(EnginePlayer player) {
        BossEventPacket pk = new BossEventPacket();
        pk.bossEid = eId;
        pk.type = BossEventPacket.TYPE_HIDE;
        player.dataPacket(pk);

        var removeEntityPacket = new RemoveEntityPacket();
        removeEntityPacket.eid = eId;
        player.dataPacket(removeEntityPacket);
    }
}
