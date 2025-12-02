package zwuiix.colria.player.particle;

import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

public class WindDashParticle extends Particle{
    public WindDashParticle(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost, boolean flying) {
        super(identifier, name, description, reference, cost, flying);
    }

    @Override
    public void run(EnginePlayer player, int currentTick) {
        if(player.sinceLastDoubleJump <= 1){
            var cd = player.getCooldown("particle.wind_dash");
            if(!cd.isExpired())
                return;

            cd.refresh(500L);
            player.getLevel().addLevelSoundEvent(player.add(0, 1), LevelSoundEventPacket.SOUND_WIND_CHARGE_BURST);
            player.getLevel().addParticle(new GenericParticle(player.getEyePosition(), GenericParticle.TYPE_WIND_EXPLOSION));

            Vector3 motion = player.getDirectionVector().multiply(1.5);
            if(player.keys.isEmpty()) {
                motion.x = 0;
                motion.z = 0;
            }

            motion.y = 0.5;
            player.setMotion(motion);
        }
    }
}
