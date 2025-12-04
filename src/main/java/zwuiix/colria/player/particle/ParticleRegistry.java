package zwuiix.colria.player.particle;

import cn.nukkit.block.*;
import cn.nukkit.item.*;
import lombok.Getter;
import zwuiix.colria.Loader;
import zwuiix.colria.translator.TranslationKeys;

import java.util.HashMap;

@Getter
public class ParticleRegistry {
    @Getter
    private static final ParticleRegistry instance = new ParticleRegistry();

    private final HashMap<String, Particle> particles = new HashMap<>();

    public void register(Particle particle) {
        this.particles.put(particle.getIdentifier(), particle);
    }

    public Particle getParticle(String identifier) {
        return this.particles.getOrDefault(identifier, null);
    }

    public void invoke(Loader loader) {
        register(new Particle("freezing_flakes", TranslationKeys.PARTICLE_FREEZING_LAKES_NAME, TranslationKeys.PARTICLE_FREEZING_LAKES_LORE, new BlockIcePacked().toItem(), 1_500, false));
        register(new Particle("emerald", TranslationKeys.PARTICLE_EMERALD_SPIRAL_NAME, TranslationKeys.PARTICLE_EMERALD_SPIRAL_LORE, new ItemEmerald(), 3_000, false));
        register(new Particle("void_purple", TranslationKeys.PARTICLE_VOID_PURPLE_NAME, TranslationKeys.PARTICLE_VOID_PURPLE_LORE, new ItemAmethystShard(), 5_000, false));
        register(new Particle("dancing_flame", TranslationKeys.PARTICLE_DANCING_FLAME_NAME, TranslationKeys.PARTICLE_DANCING_FLAME_LORE, new ItemBlazeRod(), 3_000, false));
        register(new Particle("static_shock", TranslationKeys.PARTICLE_STATIC_SHOCK_NAME, TranslationKeys.PARTICLE_STATIC_SHOCK_LORE, new ItemRedstone(), 5_000, false));
        register(new Particle("celestial_aura", TranslationKeys.PARTICLE_CELESTIAL_AURA_NAME, TranslationKeys.PARTICLE_CELESTIAL_AURA_LORE, new ItemNetherStar(), 7_500, true));
        register(new Particle("sakura_petals", TranslationKeys.PARTICLE_SAKURA_PETALS_NAME, TranslationKeys.PARTICLE_SAKURA_PETALS_LORE, new BlockPinkPetals().toItem(), 8000, true));
        register(new Particle("spectral_smoke", TranslationKeys.PARTICLE_SPECTRAL_SMOKE_NAME, TranslationKeys.PARTICLE_SPECTRAL_SMOKE_LORE, new BlockSoulLantern().toItem(), 5_000, false));
        register(new Particle("chromatic_spiral", TranslationKeys.PARTICLE_CHROMATIC_SPIRAL_NAME, TranslationKeys.PARTICLE_CHROMATIC_SPIRAL_LORE, new ItemFireworkStar(), 7_500, true));
        register(new Particle("void_ashes", TranslationKeys.PARTICLE_VOID_ASHES_NAME, TranslationKeys.PARTICLE_VOID_ASHES_LORE, new ItemBlazePowder(), 10_000, true));
        register(new WindDashParticle("wind_dash", TranslationKeys.PARTICLE_WIND_DASH_NAME, TranslationKeys.PARTICLE_WIND_DASH_LORE, new ItemWindCharge(), 1_500, false));
        register(new Particle("mini_storm", TranslationKeys.PARTICLE_MINI_STORM_NAME, TranslationKeys.PARTICLE_MINI_STORM_LORE, new ItemSnowball(), 3_000, false));
        register(new Particle("frost_heart", TranslationKeys.PARTICLE_FROST_HEART_NAME, TranslationKeys.PARTICLE_FROST_HEART_LORE, new ItemPrismarineCrystals(), 5_000, false));
        register(new Particle("void_orbits", TranslationKeys.PARTICLE_VOID_ORBITS_NAME, TranslationKeys.PARTICLE_VOID_ORBITS_LORE, new ItemEnderPearl(), 7_500, true));
        register(new Particle("shadow_dust", TranslationKeys.PARTICLE_SHADOW_DUST_NAME, TranslationKeys.PARTICLE_SHADOW_DUST_LORE, new ItemCoal(), 1_500, false));
        register(new Particle("life_essence", TranslationKeys.PARTICLE_LIFE_ESSENCE_NAME, TranslationKeys.PARTICLE_LIFE_ESSENCE_LORE, new ItemAppleGold(), 7_500, true));
        register(new Particle("love_rain", TranslationKeys.PARTICLE_LOVE_RAIN_NAME, TranslationKeys.PARTICLE_LOVE_RAIN_LORE, new ItemHeartOfTheSea(), 3_500, false));
        register(new Particle("dimensional_rift", TranslationKeys.PARTICLE_DIMENSIONAL_RIFT_NAME, TranslationKeys.PARTICLE_DIMENSIONAL_RIFT_LORE, new ItemEnderEye(), 5_000, false));
        register(new Particle("comet", TranslationKeys.PARTICLE_COMET_NAME, TranslationKeys.PARTICLE_COMET_LORE, new BlockAmethystCluster().toItem(), 15_000, true));
        register(new Particle("marine_splash", TranslationKeys.PARTICLE_MARINE_SPLASH_NAME, TranslationKeys.PARTICLE_MARINE_SPLASH_LORE, new ItemBucket(ItemBucket.WATER_BUCKET), 1_500, false));
        register(new Particle("wandering_souls", TranslationKeys.PARTICLE_WANDERING_SOULS_NAME, TranslationKeys.PARTICLE_WANDERING_SOULS_LORE, new BlockSoulSand().toItem(), 10_000, true));
    }
}
