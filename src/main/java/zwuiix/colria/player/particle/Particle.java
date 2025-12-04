package zwuiix.colria.player.particle;

import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.*;
import cn.nukkit.math.Vector3;
import lombok.Getter;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Random;
import java.util.function.DoubleFunction;

public class Particle {
    private static final double TAU = Math.PI * 2;

    @Getter
    private final String identifier;
    @Getter
    private final TranslationKeys name;
    @Getter
    private final TranslationKeys description;
    private final Item reference;
    @Getter
    private final long cost;
    @Getter
    private final boolean flying;

    public Particle(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost, boolean flying) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.cost = cost;
        this.flying = flying;
    }

    public Item getReference() { return reference.clone(); }

    public void run(EnginePlayer player, int currentTick) {
        final Position base = player.getPosition();
        final double phase = (currentTick % 3600) / 12.0;
        final long seed = (player.getId() << 32) ^ currentTick;

        final boolean slowTick = (currentTick % 6) == 0;
        final boolean medTick  = (currentTick % 10) == 0;
        final boolean fastTick = (currentTick % 3) == 0;

        boolean moving = !player.keys.isEmpty();

        final Vector3 fwd = forwardDir(player);
        final Vector3 right = rightDir(player);

        switch (this.getIdentifier()) {
            case "freezing_flakes" -> {

                var flake     = dust(230, 240, 255);
                var blueCryo  = dust(150, 200, 255);
                var frost     = dust(200, 230, 255);
                var shine     = dust(255, 255, 255);

                Random rng = new Random(seed);

                if (moving) {

                    if (fastTick) {
                        double y = base.y + 0.04;

                        int dual = 2;
                        for (int side = -1; side <= 1; side += 2) {
                            for (int i = 0; i < dual; i++) {

                                double ang = phase * 2.0 + i * 1.4;
                                double sway = Math.sin(phase * 3 + i) * 0.12;

                                double x = base.x - fwd.x * (0.3 + i * 0.25) + right.x * (side * 0.32 + sway);
                                double z = base.z - fwd.z * (0.3 + i * 0.25) + right.z * (side * 0.32 + sway);

                                spawn(player, flake.at(new Vector3(x, y, z)));

                                if ((i + currentTick) % 3 == 0) {
                                    spawn(player, blueCryo.at(new Vector3(x, y + 0.04, z)));
                                }
                            }
                        }

                        if ((currentTick % 13) == 0) {
                            spawn(player, shine.at(new Vector3(
                                    base.x - fwd.x * 0.4 + (rng.nextDouble() - 0.5) * 0.25,
                                    base.y + 0.35 + rng.nextDouble() * 0.25,
                                    base.z - fwd.z * 0.4 + (rng.nextDouble() - 0.5) * 0.25
                            )));
                        }
                    }

                    if (medTick) {

                        int segs = 6;
                        double back = 1.5;

                        for (int i = 0; i < segs; i++) {

                            double t = i / (double)(segs - 1);
                            double dist = t * back;

                            double sway = Math.sin(phase * 1.5 + t * 4.0) * 0.14;

                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;
                            double y = base.y + 0.05;

                            spawn(player, flake.at(new Vector3(x, y, z)));

                            if (i % 2 == 0) {
                                spawn(player, frost.at(new Vector3(x, y + 0.04, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        double cx = base.x;
                        double cz = base.z;
                        double cy = base.y + 0.08;

                        spawn(player, frost.at(new Vector3(cx, cy, cz)));
                    }

                } else {

                    if (fastTick) {

                        double r = 0.55;
                        int pts = 14;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) pts;
                            double ang = phase * 0.9 + t * TAU;

                            double spir = Math.sin(phase * 1.8 + i) * 0.15;

                            double x = base.x + Math.cos(ang) * (r + spir);
                            double z = base.z + Math.sin(ang) * (r + spir);
                            double y = base.y + 0.06 + Math.sin(t * 8 + phase) * 0.03;

                            var fx = (i % 3 == 0) ? blueCryo : flake;
                            spawn(player, fx.at(new Vector3(x, y, z)));

                            if (i % 5 == 0) {
                                spawn(player, frost.at(new Vector3(x, y + 0.05, z)));
                            }
                        }

                        if ((seed + currentTick) % 18 == 0) {
                            spawn(player, shine.at(base.add(0, 1.5 + Math.sin(phase * 2.0) * 0.12, 0)));
                        }
                    }

                    if (medTick) {

                        double y = base.y + 0.03;
                        int arms = 6;
                        int steps = 6;

                        for (int a = 0; a < arms; a++) {
                            for (int s = 0; s < steps; s++) {

                                double t = s / (double)(steps - 1);
                                double radius = 0.22 + t * 0.58;
                                double ang = phase * 0.5 + a * (TAU / arms) + t * 0.9;

                                double x = base.x + Math.cos(ang) * radius;
                                double z = base.z + Math.sin(ang) * radius;

                                var fx = (s % 2 == 0) ? flake : frost;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }

                        int ringPts = 16;
                        double rr = 0.85;

                        for (int i = 0; i < ringPts; i++) {
                            double ang = phase * 0.25 + i * (TAU / ringPts);
                            double x = base.x + Math.cos(ang) * rr;
                            double z = base.z + Math.sin(ang) * rr;
                            double y0 = base.y + 0.04;

                            var fx = ((i + currentTick) % 3 == 0) ? frost : flake;
                            spawn(player, fx.at(new Vector3(x, y0, z)));
                        }
                    }

                    if (slowTick) {

                        int clouds = 5;
                        for (int i = 0; i < clouds; i++) {
                            double ang = rng.nextDouble() * TAU;
                            double dist = 0.1 + rng.nextDouble() * 0.25;

                            double x = base.x + Math.cos(ang) * dist;
                            double z = base.z + Math.sin(ang) * dist;
                            double y = base.y + 0.45 + rng.nextDouble() * 0.25;

                            spawn(player, frost.at(new Vector3(x, y, z)));
                        }
                    }
                }
            }


            // Emerald
            case "emerald" -> {
                var emeraldCore  = dust(0, 230, 130);
                var emeraldDeep  = dust(0, 160, 90);
                var emeraldLight = dust(120, 255, 200);

                if (moving) {

                    if (fastTick) {
                        int segs = 7;
                        double back = 1.9;
                        double sideOffset = 0.45;

                        for (int lane = -1; lane <= 1; lane++) {
                            double laneSide = lane * sideOffset;

                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);
                                double dist = t * back;

                                double sway = Math.sin(phase * 1.1 + t * 5.0 + lane * 0.8) * 0.16;

                                double x = base.x - fwd.x * dist + right.x * (laneSide + sway);
                                double z = base.z - fwd.z * dist + right.z * (laneSide + sway);
                                double y = base.y + 0.55 + t * 0.25;

                                spawn(player, emeraldCore.at(new Vector3(x, y, z)));

                                if ((i + lane + currentTick) % 3 == 0) {
                                    spawn(player, emeraldLight.at(new Vector3(x, y + 0.06, z)));
                                }
                            }
                        }
                    }

                    if (medTick) {
                        double yFeet = base.y + 0.04;
                        ring(player, emeraldDeep, base, 0.85, 14, phase * 1.0, 0.04);

                        Position chest = base.add(0, 1.0, 0);
                        coneBurst(player, emeraldCore, chest, fwd, 1.4, 6, seed ^ 0xE31DL, 0.2, 0.9);
                    }

                    if ((currentTick % 18) == 0) {
                        Position behind = base.add(-fwd.x * 0.9, 0.9, -fwd.z * 0.9);
                        burstRandom(player, emeraldLight, behind, 0.8, 5, seed ^ 0x4CBAL, 0.2, 0.9);
                    }

                } else {
                    if (fastTick) {
                        int orbs = 4;
                        double r = 0.7;

                        for (int i = 0; i < orbs; i++) {
                            double t = i / (double) orbs;

                            double angLow = phase * 0.7 + t * TAU;
                            double xL = base.x + Math.cos(angLow) * r;
                            double zL = base.z + Math.sin(angLow) * r;
                            double yL = base.y + 0.7 + 0.05 * Math.sin(phase * 1.2 + t * 6.0);
                            spawn(player, emeraldDeep.at(new Vector3(xL, yL, zL)));

                            double angHigh = -phase * 0.6 + t * TAU;
                            double xH = base.x + Math.cos(angHigh) * (r * 0.8);
                            double zH = base.z + Math.sin(angHigh) * (r * 0.8);
                            double yH = base.y + 1.4 + 0.05 * Math.cos(phase * 1.1 + t * 4.0);
                            spawn(player, emeraldCore.at(new Vector3(xH, yH, zH)));
                        }
                    }

                    if (slowTick) {
                        int pts = 10;
                        double h = 1.6;
                        double r = 0.5;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double ang = phase * 0.9 + t * TAU * 1.3;
                            double x = base.x + Math.cos(ang) * (r + 0.05 * t);
                            double z = base.z + Math.sin(ang) * (r + 0.05 * t);
                            double y = base.y + 0.5 + t * h;

                            var fx = (i % 2 == 0) ? emeraldCore : emeraldDeep;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    if ((currentTick % 20) == 0) {
                        Position chest = base.add(0, 1.0, 0);
                        int rays = 6;
                        double r0 = 0.3;
                        double r1 = 1.0;
                        for (int i = 0; i < rays; i++) {
                            double ang = phase * 0.8 + i * (TAU / rays);
                            double dx = Math.cos(ang);
                            double dz = Math.sin(ang);

                            for (int s = 0; s < 3; s++) {
                                double t = s / 2.0; // 0, 0.5, 1
                                double rr = r0 + (r1 - r0) * t;
                                double x = chest.x + dx * rr;
                                double z = chest.z + dz * rr;
                                double y = chest.y + 0.05 * s;

                                var fx = (s == 2) ? emeraldLight : emeraldCore;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }
                    }
                }
            }

            // Void Purple
            case "void_purple" -> {
                var purple = dust(140, 0, 200);
                PFactory darkSmoke = pos -> new SmokeParticle(pos);

                if (moving) {
                    if (medTick) {
                        int segs = 7;
                        double back = 1.6;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = t * back;
                            double x = base.x - fwd.x * dist;
                            double z = base.z - fwd.z * dist;
                            double y = base.y + 0.05;
                            spawn(player, purple.at(new Vector3(x, y, z)));
                            if ((i % 2) == 0) {
                                spawn(player, darkSmoke.at(new Vector3(x, y + 0.3, z)));
                            }
                        }

                        coneBurst(player, darkSmoke, base.add(0, 1.0, 0), fwd, 1.0, 5, seed, 0.5, 1.2);
                    }
                } else {
                    if (fastTick) {
                        double[] heights = {0.7, 1.2, 1.7};
                        double[] radii = {0.5, 0.7, 0.6};
                        for (int h = 0; h < heights.length; h++) {
                            for (int orb = 0; orb < 3; orb++) {
                                double ang = phase * (0.35 + 0.15 * h) + orb * (TAU / 3.0) + h * 0.7;
                                double r = radii[h];
                                double x = base.x + Math.cos(ang) * r;
                                double z = base.z + Math.sin(ang) * r;
                                double y = base.y + heights[h];
                                spawn(player, purple.at(new Vector3(x, y, z)));
                                if (h == 1) {
                                    spawn(player, darkSmoke.at(new Vector3(x, y + 0.1, z)));
                                }
                            }
                        }
                    }
                    if ((currentTick % 12) == 0) {
                        burstRandom(player, darkSmoke, base.add(0, 1.0, 0), 0.9, 4, seed, 0.5, 1.2);
                    }
                }
            }

            // 🔥 Dancing Flame
            case "dancing_flame" -> {
                var ember      = dust(240, 140, 60);  // orange chaud
                var emberHot   = dust(255, 210, 120); // jaune très chaud
                var emberDeep  = dust(180, 70, 40);   // rouge sombre

                Random rng = new Random(seed ^ 0xF1A3E11L);
                Position center = base.add(0, 0.1, 0);

                // ========= MOUVEMENT =========
                if (moving) {

                    // 1) Jet de flammes sous les pieds + traînée arrière
                    if (fastTick) {
                        // petites flammes footprints
                        footsteps(player, FlameParticle::new, base, right, 0.30, 0.02);

                        // double jet derrière le joueur (gauche/droite)
                        Position jetBase = base.add(-fwd.x * 0.7, 0.4, -fwd.z * 0.7);

                        for (int side = -1; side <= 1; side += 2) {
                            double sideOffset = 0.28 * side;
                            for (int i = 0; i < 4; i++) {
                                double t = i / 3.0;
                                double rise = 0.5 + t * 0.7;

                                double x = jetBase.x + right.x * sideOffset;
                                double z = jetBase.z + right.z * sideOffset;
                                double y = jetBase.y + rise + Math.sin(phase * 1.6 + t * 5.0) * 0.05;

                                Vector3 pos = new Vector3(x, y, z);
                                spawn(player, new FlameParticle(pos));
                                if (i >= 2) {
                                    spawn(player, emberHot.at(pos.add(0, 0.05, 0)));
                                }
                            }
                        }

                        // léger ruban de braises dans le dos
                        int segs = 5;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = 0.3 + t * 0.9;

                            double sway = Math.sin(phase * 1.3 + t * 4.0) * 0.10;

                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;
                            double y = base.y + 0.6 + t * 0.25;

                            var fx = (i % 2 == 0) ? ember : emberHot;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    // 2) Arc de flammes qui contourne le torse en arrière
                    if (medTick) {
                        int segs = 6;
                        double radius = 0.7;
                        double yBase = base.y + 1.0;

                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);

                            double ang = (Math.PI * 0.9) + t * (Math.PI * 0.8); // arc derrière
                            double x = base.x - fwd.x * 0.25 + Math.cos(ang) * radius * 0.40;
                            double z = base.z - fwd.z * 0.25 + Math.sin(ang) * radius * 0.40;

                            double sway = Math.sin(phase * 1.1 + t * 5.0) * 0.10;
                            x += right.x * sway;
                            z += right.z * sway;

                            double y = yBase + Math.sin(phase * 1.4 + t * 4.0) * 0.10;

                            var fx = (i % 2 == 0) ? ember : emberDeep;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    // ========= IDLE =========
                } else {

                    // 3) Double hélice de flammes autour du corps
                    if (fastTick) {
                        Position spiralCenter = base.add(0, 0.3, 0);
                        double radius = 0.45;
                        double height = 1.5;
                        int points = 14;

                        // Hélice 1
                        spiral(player, ember, spiralCenter, radius, height, points, phase * 0.9);
                        // Hélice 2 décalée
                        spiral(player, emberHot, spiralCenter, radius * 0.9, height, points, phase * 0.9 + Math.PI);

                        // quelques flammes brutes qui flottent au milieu
                        int extra = 3;
                        for (int i = 0; i < extra; i++) {
                            double t = i / (double) extra;
                            double y = base.y + 0.5 + t * 1.2;
                            double offset = Math.sin(phase * 1.5 + t * 6.0) * 0.10;

                            double x = base.x + offset * right.x;
                            double z = base.z + offset * right.z;
                            spawn(player, new FlameParticle(new Vector3(x, y, z)));
                        }
                    }

                    // 4) Colonne centrée et petite couronne de braises
                    if (slowTick) {
                        int pts = 6;

                        // colonne centrale
                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double y = base.y + 0.4 + t * 1.6;
                            y += Math.sin(phase * 1.8 + t * 4.0) * 0.06;

                            double sway = Math.sin(phase * 0.9 + t * 3.5) * 0.08;
                            double x = base.x + sway * right.x;
                            double z = base.z + sway * right.z;

                            spawn(player, new FlameParticle(new Vector3(x, y, z)));
                        }

                        // couronne au-dessus de la tête
                        double crownY = base.y + 1.9;
                        double crownR = 0.6;
                        int crownPts = 8;

                        for (int i = 0; i < crownPts; i++) {
                            double a = phase * 0.7 + i * (TAU / crownPts);
                            double x = base.x + Math.cos(a) * crownR;
                            double z = base.z + Math.sin(a) * crownR;
                            double y = crownY + Math.sin(phase * 1.6 + i) * 0.05;

                            var fx = (i % 2 == 0) ? emberHot : ember;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }

                        burstRandom(player, emberDeep, base.add(0, 1.4, 0),
                                0.4, 3, seed ^ 0xF1A3E21L, 0.0, 0.5);
                    }
                }

                // ========= EVENTS GLOBAUX =========

                // 5) Anneau de flammes au sol qui “pulse”
                if ((currentTick % 45) == 0) {
                    // anneau de braises
                    ring(player, emberDeep, base, 0.55, 12, phase * 1.1, 0.03);
                    // anneau plus chaud juste au-dessus
                    ring(player, emberHot, base, 0.35, 8, -phase * 0.9, 0.05);
                }

                // 6) "Battement d’ailes" de feu (comme un mini phénix)
                if ((currentTick % 90) == 0) {
                    for (int side = -1; side <= 1; side += 2) {
                        double sideSign = side;

                        int segs = 5;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);

                            double spread = 0.3 + t * 0.7;
                            double height = base.y + 0.7 + t * 0.8;
                            double forward = -0.3 + t * 0.5;

                            double x = base.x
                                    + right.x * spread * sideSign
                                    + fwd.x * forward;
                            double z = base.z
                                    + right.z * spread * sideSign
                                    + fwd.z * forward;
                            double y = height + Math.sin(phase * 1.4 + t * 5.0) * 0.06;

                            Vector3 pos = new Vector3(x, y, z);
                            spawn(player, new FlameParticle(pos));
                            if (i >= 2) {
                                spawn(player, emberHot.at(pos.add(0, 0.05, 0)));
                            }
                        }
                    }
                }
            }

            // Static Shock
            case "static_shock" -> {
                var core  = dust(120, 190, 255);
                var edge  = dust(40, 80, 180);
                var white = dust(240, 240, 255);
                var cyan  = dust(150, 230, 255);

                double chestY = base.y + 1.0;
                double feetY  = base.y + 0.04;

                if (moving) {

                    if (fastTick) {
                        int paths = 3;
                        for (int p = 0; p < paths; p++) {
                            int segs = 4;
                            double back = 0.2 + p * 0.25;
                            double startY = chestY + (p == 0 ? 0.1 : 0.0);

                            double sx = base.x - fwd.x * back;
                            double sz = base.z - fwd.z * back;

                            double px = sx;
                            double py = startY;
                            double pz = sz;

                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) segs;
                                double jitterX = (Math.random() - 0.5) * 0.25;
                                double jitterZ = (Math.random() - 0.5) * 0.25;
                                double up = (Math.random() * 0.22) + 0.03;

                                double nx = sx - fwd.x * (t * 0.6) + right.x * jitterX;
                                double nz = sz - fwd.z * (t * 0.6) + right.z * jitterZ;
                                double ny = startY + up * (i + 1);

                                int steps = 3;
                                for (int s = 0; s <= steps; s++) {
                                    double u = s / (double) steps;
                                    double ix = px + (nx - px) * u;
                                    double iy = py + (ny - py) * u;
                                    double iz = pz + (nz - pz) * u;
                                    var fx = (s % 2 == 0) ? core : cyan;
                                    spawn(player, fx.at(new Vector3(ix, iy, iz)));
                                }

                                px = nx;
                                py = ny;
                                pz = nz;
                            }
                        }

                        int arcs = 4;
                        for (int i = 0; i < arcs; i++) {
                            double offset = (i % 2 == 0 ? 0.35 : -0.35);
                            double y1 = chestY + (i < 2 ? 0.2 : -0.1);
                            double y2 = y1 + ((i % 2 == 0) ? 0.18 : -0.18);

                            Vector3 a = new Vector3(base.x + right.x * offset, y1, base.z + right.z * offset);
                            Vector3 b = new Vector3(base.x - right.x * offset * 0.6, y2, base.z - right.z * offset * 0.6);

                            int segs = 3;
                            for (int s = 0; s <= segs; s++) {
                                double t = s / (double) segs;
                                double jx = (Math.random() - 0.5) * 0.06;
                                double jz = (Math.random() - 0.5) * 0.06;
                                double x = a.x + (b.x - a.x) * t + jx;
                                double y = a.y + (b.y - a.y) * t;
                                double z = a.z + (b.z - a.z) * t + jz;
                                var fx = (s % 2 == 0) ? white : core;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }

                        double auraR = 0.55;
                        int pts = 10;
                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) pts;
                            double a = phase * 1.3 + t * TAU;
                            double jitter = Math.sin(phase * 3.0 + t * 10.0) * 0.08;

                            double x = base.x + Math.cos(a) * (auraR + jitter);
                            double z = base.z + Math.sin(a) * (auraR + jitter);
                            double y = chestY + Math.sin(phase * 2.0 + i) * 0.06;

                            var fx = (i % 3 == 0) ? cyan : core;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }

                        if ((currentTick & 3) == 0) {
                            double fx = base.x + (Math.random() - 0.5) * 0.4;
                            double fz = base.z + (Math.random() - 0.5) * 0.4;
                            spawn(player, white.at(new Vector3(fx, chestY + 0.25, fz)));
                        }
                    }

                    if (medTick) {
                        int segs = 5;
                        double back = 1.5;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = t * back;
                            double sway = Math.sin(phase * 2.0 + t * 5.0) * 0.18;

                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;
                            double y = base.y + 0.09;

                            var fx = (i % 2 == 0) ? edge : core;
                            spawn(player, fx.at(new Vector3(x, y, z)));

                            Vector3 left = new Vector3(x + right.x * 0.18, y, z + right.z * 0.18);
                            Vector3 rightV = new Vector3(x - right.x * 0.18, y, z - right.z * 0.18);

                            spawn(player, cyan.at(left));
                            spawn(player, cyan.at(rightV));
                        }

                        double footR = 0.32;
                        int spikes = 4;
                        for (int i = 0; i < spikes; i++) {
                            double a = phase * 1.1 + i * (TAU / spikes);
                            double dx = Math.cos(a) * footR;
                            double dz = Math.sin(a) * footR;

                            Vector3 start = new Vector3(base.x + dx * 0.4, feetY, base.z + dz * 0.4);
                            Vector3 end   = new Vector3(base.x + dx, feetY + 0.18, base.z + dz);

                            int steps = 3;
                            for (int s = 0; s <= steps; s++) {
                                double t = s / (double) steps;
                                double x = start.x + (end.x - start.x) * t;
                                double y = start.y + (end.y - start.y) * t;
                                double z = start.z + (end.z - start.z) * t;
                                var fx = (s == steps) ? white : edge;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                } else {

                    if (fastTick) {
                        double size = 0.9;

                        Vector3[] floor = new Vector3[]{
                                new Vector3(base.x + fwd.x * size + right.x * size, feetY, base.z + fwd.z * size + right.z * size),
                                new Vector3(base.x + fwd.x * size - right.x * size, feetY, base.z + fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size - right.x * size, feetY, base.z - fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size + right.x * size, feetY, base.z - fwd.z * size + right.z * size)
                        };

                        int edgePts = 7;
                        for (int i = 0; i < 4; i++) {
                            int j = (i + 1) % 4;
                            Vector3 a = floor[i];
                            Vector3 b = floor[j];
                            for (int k = 0; k <= edgePts; k++) {
                                double t = k / (double) edgePts;
                                double jitter = (Math.random() - 0.5) * 0.06;
                                double x = a.x + (b.x - a.x) * t + jitter;
                                double z = a.z + (b.z - a.z) * t - jitter;
                                double y = feetY + Math.sin(phase * 2.0 + t * 6.0) * 0.03;

                                var fx = ((k + i + currentTick) % 3 == 0) ? white : ((k + i) % 2 == 0 ? core : edge);
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }

                        int innerPts = 8;
                        double r = 0.45;
                        for (int i = 0; i < innerPts; i++) {
                            double a = phase * 1.1 + i * (TAU / innerPts);
                            double x = base.x + Math.cos(a) * r;
                            double z = base.z + Math.sin(a) * r;
                            var fx = (i % 2 == 0) ? cyan : core;
                            spawn(player, fx.at(new Vector3(x, feetY + 0.02, z)));
                        }
                    }

                    if (medTick) {
                        double yMid = base.y + 1.0;
                        double size = 0.6;
                        Vector3[] mid = new Vector3[]{
                                new Vector3(base.x + fwd.x * size + right.x * size, yMid, base.z + fwd.z * size + right.z * size),
                                new Vector3(base.x + fwd.x * size - right.x * size, yMid, base.z + fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size - right.x * size, yMid, base.z - fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size + right.x * size, yMid, base.z - fwd.z * size + right.z * size)
                        };

                        int segs = 4;
                        for (int i = 0; i < 4; i++) {
                            int j = (i + 1) % 4;
                            Vector3 a = mid[i];
                            Vector3 b = mid[j];
                            for (int k = 0; k <= segs; k++) {
                                double t = k / (double) segs;
                                double jx = (Math.random() - 0.5) * 0.05;
                                double jz = (Math.random() - 0.5) * 0.05;
                                double x = a.x + (b.x - a.x) * t + jx;
                                double z = a.z + (b.z - a.z) * t + jz;
                                double y = yMid + Math.sin(phase * 1.8 + t * 5.0 + i) * 0.05;
                                var fx = (k % 2 == 0) ? core : cyan;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        int spikes = 6;
                        int steps = 3;
                        double baseR = 0.38;
                        for (int i = 0; i < spikes; i++) {
                            double ang = phase * 0.7 + i * (TAU / spikes);
                            double dx = Math.cos(ang) * baseR;
                            double dz = Math.sin(ang) * baseR;

                            for (int s = 0; s <= steps; s++) {
                                double t = s / (double) steps;
                                double y = base.y + 0.25 + t * 0.9;
                                double x = base.x + dx * (1.0 + 0.1 * t);
                                double z = base.z + dz * (1.0 + 0.1 * t);

                                var fx = (s == steps) ? white : ((s % 2 == 0) ? core : edge);
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }
                    }
                }
            }

            // Celestial Aura
            case "celestial_aura" -> {
                var gold  = dust(255, 220, 120);
                var white = dust(255, 255, 255);
                var sky   = dust(160, 210, 255);

                boolean wingsTick = (currentTick % 10) == 0;
                boolean haloTick  = wingsTick;

                if (moving) {
                    if (fastTick) {
                        int segs = 6;
                        double length = 1.9;
                        double y = base.y + 1.05;

                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);

                            double sway = Math.sin(phase * 0.9 + t * 4.0) * 0.15;

                            double dist = t * length;
                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;

                            spawn(player, sky.at(new Vector3(x, y, z)));

                            if (i == segs - 1) {
                                spawn(player, white.at(new Vector3(x, y + 0.06, z)));
                            }
                        }
                    }

                    if (wingsTick) {
                        int segs = 7;
                        double baseY = base.y + 1.1;

                        double flap = Math.sin(phase * 2.5) * 0.2;

                        for (int side = -1; side <= 1; side += 2) {

                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);

                                double arch   = Math.sin(t * Math.PI) * (0.35 + flap);
                                double spread = 0.45 + 0.55 * t;
                                double back   = 0.20 + 0.40 * t;

                                double x = base.x
                                        - fwd.x * back
                                        + right.x * (side * spread);
                                double z = base.z
                                        - fwd.z * back
                                        + right.z * (side * spread);
                                double y = baseY + arch;

                                spawn(player, gold.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (haloTick) {
                        double r = 0.55;
                        double y = base.y + 1.95;

                        ring(player, white, base.add(0, 1.95 - base.y, 0), r, 10, phase * 1.2, 0.03);
                    }

                }

                else {
                    if (fastTick) {
                        ring(player, sky, base, 0.95, 18, phase * 0.4, 0.04);

                        ring(player, gold, base, 0.58, 12, -phase * 0.7, 0.04);

                        int rays = 4;
                        int steps = 3;
                        double stepLen = 0.25;

                        for (int i = 0; i < rays; i++) {
                            double ang = phase * 0.45 + i * (TAU / rays);
                            double dx = Math.cos(ang);
                            double dz = Math.sin(ang);

                            for (int s = 1; s <= steps; s++) {
                                double dist = s * stepLen;
                                double x = base.x + dx * dist;
                                double z = base.z + dz * dist;
                                spawn(player, white.at(new Vector3(x, base.y + 0.04, z)));
                            }
                        }
                    }

                    if ((currentTick % 18) == 0) {
                        int pts = 12;
                        double h = 1.7;
                        double r = 0.45;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double ang = phase * 1.1 + t * TAU * 1.3;

                            double x = base.x + Math.cos(ang) * (r + 0.06 * t);
                            double z = base.z + Math.sin(ang) * (r + 0.06 * t);
                            double y = base.y + 0.45 + t * h;

                            var fx = (i % 2 == 0) ? white : gold;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    if ((currentTick % 20) == 0) {
                        double crownY = base.y + 1.9;
                        int stars = 8;
                        double r = 0.65;

                        for (int i = 0; i < stars; i++) {
                            double t = i / (double) stars;
                            double ang = phase * 1.2 + t * TAU;

                            double x = base.x + Math.cos(ang) * r;
                            double z = base.z + Math.sin(ang) * r;
                            double y = crownY + 0.03 * Math.sin(phase * 1.4 + t * 5.0);

                            spawn(player, white.at(new Vector3(x, y, z)));
                        }
                    }

                    if (medTick) {
                        double midY = base.y + 1.0;
                        int pts = 10;
                        double r = 0.48;

                        for (int i = 0; i < pts; i++) {
                            double a = phase * 0.85 + i * (TAU / pts);
                            double x = base.x + Math.cos(a) * r;
                            double z = base.z + Math.sin(a) * r;
                            double y = midY + 0.03 * Math.sin(phase * 1.2 + i);

                            spawn(player, sky.at(new Vector3(x, y, z)));
                        }
                    }
                }
            }

            // Sakura Petals
            case "sakura_petals" -> {
                var pink = dust(255, 150, 210);
                var deep = dust(220, 80, 140);

                if (moving) {
                    if (fastTick) {
                        int segs = 7;
                        double back = 1.7;
                        double sideOffset = 0.55;

                        for (int lane = -1; lane <= 1; lane++) {
                            double laneSide = lane * sideOffset;

                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);
                                double dist = t * back;
                                double wave = Math.sin(phase * 0.8 + t * 4.5 + lane * 0.7) * 0.18;

                                double x = base.x - fwd.x * dist + right.x * (laneSide + wave);
                                double z = base.z - fwd.z * dist + right.z * (laneSide + wave);
                                double y = base.y + 0.4 + t * 0.35;

                                spawn(player, pink.at(new Vector3(x, y, z)));
                                if ((i + lane + currentTick) % 3 == 0) {
                                    spawn(player, deep.at(new Vector3(x, y + 0.05, z)));
                                }
                            }
                        }
                    }

                    if (medTick) {
                        Position gustCenter = base.add(-fwd.x * 0.9, 1.2, -fwd.z * 0.9);
                        coneBurst(player, pink, gustCenter, new Vector3(-fwd.x, 0, -fwd.z),
                                1.4, 8, seed ^ 0x5B3AL, 0.3, 0.9);
                        coneBurst(player, deep, gustCenter, new Vector3(-fwd.x, 0, -fwd.z),
                                1.2, 4, seed ^ 0x9C1L, 0.4, 1.0);
                    }

                } else {
                    if (fastTick) {
                        int pts = 24;
                        double baseY = base.y + 0.15;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) pts;
                            double ang = t * TAU;

                            double petalShape = 0.75 + 0.18 * Math.sin(4 * ang + phase * 0.9);
                            double x = base.x + Math.cos(ang + phase * 0.25) * petalShape;
                            double z = base.z + Math.sin(ang + phase * 0.25) * petalShape;
                            double y = baseY + 0.02 * Math.sin(phase * 0.6 + ang * 2.0);

                            spawn(player, pink.at(new Vector3(x, y, z)));

                            if (i % 3 == 0) {
                                spawn(player, deep.at(new Vector3(x, y + 0.02, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        Random rng = new Random(seed);
                        int count = 14;
                        for (int i = 0; i < count; i++) {
                            double a = rng.nextDouble() * TAU;
                            double r = 0.4 + rng.nextDouble() * 1.2;
                            double x = base.x + Math.cos(a) * r;
                            double z = base.z + Math.sin(a) * r;

                            double y = base.y + 1.8 + rng.nextDouble() * 0.9;

                            if (rng.nextBoolean()) {
                                spawn(player, pink.at(new Vector3(x, y, z)));
                            } else {
                                spawn(player, deep.at(new Vector3(x, y, z)));
                            }
                        }
                    }
                }
            }

            // Spectral Smoke
            case "spectral_smoke" -> {

                // Particules gris + violet spectral
                PFactory grey   = pos -> dust(90, 90, 90).at(pos);
                PFactory violet = pos -> dust(150, 70, 180).at(pos);

                PFactory smokeGrey   = SmokeParticle::new;
                PFactory smokeViolet = pos -> dust(150, 70, 180).at(pos);

                boolean hasTint = true;

                if (moving) {

                    int segs = 8;
                    double back = 1.5;
                    double sideOffset = 0.55;

                    for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
                        for (int i = 0; i < segs; i++) {

                            double t = i / (double) (segs - 1);

                            double sway = Math.sin(phase * 0.7 + t * 3.8 + sideSign * 0.5) * 0.15;

                            double dist = t * back;

                            double cx = base.x - fwd.x * dist + right.x * (sideSign * sideOffset + sway);
                            double cz = base.z - fwd.z * dist + right.z * (sideSign * sideOffset + sway);
                            double cy = base.y + 0.6 + t * 0.45;

                            if (i % 2 == 0) spawn(player, smokeGrey.at(new Vector3(cx, cy, cz)));
                            else spawn(player, hasTint ? smokeViolet.at(new Vector3(cx, cy, cz))
                                    : violet.at(new Vector3(cx, cy, cz)));
                        }
                    }

                    if (fastTick) {
                        int cols = 5;
                        double radius = 0.65;

                        for (int c = 0; c < cols; c++) {
                            double ang = phase * 0.7 + c * (TAU / cols);
                            double x = base.x + Math.cos(ang) * radius;
                            double z = base.z + Math.sin(ang) * radius;

                            double y = base.y + 0.4 + Math.sin(phase * 0.6 + c) * 0.15;

                            spawn(player, smokeGrey.at(new Vector3(x, y, z)));
                            spawn(player, hasTint ? smokeViolet.at(new Vector3(x, y + 0.1, z))
                                    : violet.at(new Vector3(x, y + 0.1, z)));
                        }
                    }
                }

                else {

                    if (fastTick) {
                        int columns = 6;
                        double radius = 1.1;
                        int heightSteps = 5;

                        for (int i = 0; i < columns; i++) {

                            double ang = phase * 0.25 + i * (TAU / columns);
                            double x = base.x + Math.cos(ang) * radius;
                            double z = base.z + Math.sin(ang) * radius;

                            for (int h = 0; h < heightSteps; h++) {

                                double t = h / (double) (heightSteps - 1);

                                double y = base.y + 0.35 + t * 1.6;

                                if (h % 2 == 0) spawn(player, smokeGrey.at(new Vector3(x, y, z)));
                                else spawn(player, hasTint ? smokeViolet.at(new Vector3(x, y, z))
                                        : violet.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        double y = base.y + 1.7;

                        spawn(player, smokeGrey.at(base.add(0, 1.7, 0)));
                        spawn(player, hasTint ? smokeViolet.at(base.add(0, 1.85, 0))
                                : violet.at(base.add(0, 1.85, 0)));
                    }
                }
            }

            // Chromatic Spiral
            case "chromatic_spiral" -> {
                int pts = 8;
                if (moving) {
                    for (int i = 0; i < pts; i++) {
                        double t = i / (double) pts;
                        int[] rgb = hsvToRgb((t + (phase * 0.02)) % 1.0, 1.0, 1.0);
                        Vector3 p = new Vector3(
                                base.x - fwd.x * (0.6 + t * 0.8),
                                base.y + 0.9 + t * 0.3,
                                base.z - fwd.z * (0.6 + t * 0.8)
                        );
                        spawn(player, new DustParticle(p, rgb[0], rgb[1], rgb[2]));
                    }
                } else if (fastTick) {
                    for (int i = 0; i < pts; i++) {
                        double t = (double) i / pts;
                        double a = phase * 0.8 + t * TAU;
                        double x = base.x + Math.cos(a) * 0.8;
                        double z = base.z + Math.sin(a) * 0.8;
                        double y = base.y + 0.4 + t * 1.6;
                        int[] rgb = hsvToRgb((t + (phase * 0.01)) % 1.0, 1.0, 1.0);
                        spawn(player, new DustParticle(new Vector3(x, y, z), rgb[0], rgb[1], rgb[2]));
                    }
                }
            }

            // Void Ashes
            case "void_ashes" -> {
                var ember = dust(235, 70, 50);
                var dark  = dust(15, 10, 25);

                if (moving) {
                    if (fastTick) {
                        int segs = 8;
                        double back = 2.0;
                        double sideOffset = 0.38;

                        for (int lane = -1; lane <= 1; lane++) {
                            double laneSide = lane * sideOffset;
                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);
                                double dist = t * back;

                                double wave = Math.sin(phase * 0.9 + t * 4.8 + lane * 0.9) * 0.16;
                                double lift = Math.sin(t * Math.PI) * 0.45;

                                double x = base.x - fwd.x * dist + right.x * (laneSide + wave);
                                double z = base.z - fwd.z * dist + right.z * (laneSide + wave);
                                double y = base.y + 0.35 + lift;

                                spawn(player, ember.at(new Vector3(x, y, z)));

                                if ((i + lane + currentTick) % 3 == 0) {
                                    spawn(player, dark.at(new Vector3(x, y - 0.18, z)));
                                }
                            }
                        }

                        footsteps(player, dark, base, right, 0.32, 0.03);
                    }

                    if (medTick) {
                        Position core = base.add(-fwd.x * 0.9, 1.0, -fwd.z * 0.9);
                        int pts = 14;
                        double aR = 0.9;
                        double bR = 0.5;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) pts;
                            double ang = phase * 0.8 + t * TAU;
                            double x = core.x + Math.cos(ang) * aR;
                            double z = core.z + Math.sin(ang) * bR;
                            double y = core.y + Math.sin(phase * 1.4 + t * 6.0) * 0.18;

                            var fx = (i % 2 == 0) ? dark : ember;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    if ((currentTick % 16) == 0) {
                        Position burst = base.add(-fwd.x * 1.2, 1.2, -fwd.z * 1.2);
                        burstRandom(player, ember, burst, 0.9, 5, seed ^ 0xAB13L, 0.1, 0.7);
                        burstRandom(player, dark,  burst, 0.9, 4, seed ^ 0x77D3L, 0.0, 0.8);
                    }

                } else {

                    if (fastTick) {
                        double yFeet = base.y + 0.03;

                        int petals = 24;
                        double baseR = 0.8;
                        double ripple = 0.22;

                        for (int i = 0; i < petals; i++) {
                            double t = i / (double) petals;
                            double ang = t * TAU;
                            double rose = baseR + ripple * Math.cos(3 * ang + phase * 1.1);
                            double x = base.x + Math.cos(ang) * rose;
                            double z = base.z + Math.sin(ang) * rose;
                            spawn(player, dark.at(new Vector3(x, yFeet, z)));
                        }

                        double innerR = 0.45 + 0.05 * Math.sin(phase * 1.7);
                        ring(player, ember, base, innerR, 12, phase * 0.8, 0.04);

                        int spikes = 6;
                        int steps = 3;
                        double stepLen = 0.28;
                        for (int s = 0; s < spikes; s++) {
                            double ang = phase * 0.5 + s * (TAU / spikes);
                            double dx = Math.cos(ang);
                            double dz = Math.sin(ang);
                            for (int i = 1; i <= steps; i++) {
                                double dist = stepLen * i;
                                double x = base.x + dx * dist;
                                double z = base.z + dz * dist;
                                double y = yFeet + 0.01;
                                var fx = (i == steps) ? ember : dark;
                                spawn(player, fx.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (medTick) {
                        int pts = 16;
                        double h = 1.9;
                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double ang = phase * 1.2 + t * TAU * 1.6;
                            double r = 0.35 + 0.25 * t;

                            double x1 = base.x + Math.cos(ang) * r;
                            double z1 = base.z + Math.sin(ang) * r;
                            double y1 = base.y + 0.4 + t * h;
                            spawn(player, ember.at(new Vector3(x1, y1, z1)));

                            double ang2 = ang + Math.PI * 0.7;
                            double r2 = r * 0.85;
                            double x2 = base.x + Math.cos(ang2) * r2;
                            double z2 = base.z + Math.sin(ang2) * r2;
                            double y2 = y1 + Math.sin(phase * 1.5 + t * 5.0) * 0.12;
                            if (i % 2 == 0) {
                                spawn(player, dark.at(new Vector3(x2, y2, z2)));
                            }
                        }
                    }

                    if ((currentTick % 12) == 0) {
                        double topY = base.y + 2.1;
                        int pts = 10;
                        double r = 0.9;

                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) pts;
                            double ang = phase * 0.9 + t * TAU;

                            double wobble = 0.06 * Math.sin(phase * 1.8 + t * 7.0);
                            double x = base.x + Math.cos(ang) * (r + wobble);
                            double z = base.z + Math.sin(ang) * (r + wobble);
                            double y = topY + 0.05 * Math.sin(phase * 1.3 + t * 5.0);

                            spawn(player, dark.at(new Vector3(x, y, z)));

                            if (i % 3 == 0) {
                                spawn(player, ember.at(new Vector3(x, y + 0.04, z)));
                            }
                        }
                    }
                }
            }

            // 🌪️ Tempête Miniature
            case "mini_storm" -> {
                var main = dust(235, 240, 255);
                var soft = dust(195, 205, 220);

                Position center = base.add(0, 0.05, 0);
                Random rng = new Random(seed ^ 0xC0F57A1L);

                if (fastTick) {
                    int points = 10;
                    double inner = 0.22;
                    double outer = 0.45;

                    double yLow = center.y + 0.02;
                    double yMid = center.y + 0.35;

                    for (int i = 0; i < points; i++) {
                        double t = i / (double) points;

                        double ang = phase * 1.0 + t * TAU;
                        double wave = 0.08 * Math.sin(phase * 1.3 + t * 5.0);

                        double r1 = inner + wave * 0.5;
                        double x1 = center.x + Math.cos(ang) * r1;
                        double z1 = center.z + Math.sin(ang) * r1;
                        spawn(player, soft.at(new Vector3(x1, yLow, z1)));

                        double r2 = outer + wave;
                        double x2 = center.x + Math.cos(-ang * 0.9) * r2;
                        double z2 = center.z + Math.sin(-ang * 0.9) * r2;
                        var fx2 = ((i + currentTick) % 3 == 0) ? main : soft;
                        spawn(player, fx2.at(new Vector3(x2, yMid, z2)));
                    }

                    int colSteps = 3;
                    for (int i = 0; i < colSteps; i++) {
                        double t = i / (double) (colSteps - 1);
                        double y = center.y + 0.18 + t * 0.45;
                        double offset = Math.sin(phase * 1.4 + t * 6.0) * 0.06;

                        double x = center.x + offset * right.x;
                        double z = center.z + offset * right.z;

                        spawn(player, main.at(new Vector3(x, y, z)));
                    }
                }

                if (moving && medTick) {
                    int segs = 4;
                    double back = 1.1;

                    for (int i = 0; i < segs; i++) {
                        double t = i / (double) (segs - 1);
                        double dist = t * back;

                        double sway = Math.sin(phase * 1.1 + t * 4.0) * 0.10;

                        double x = base.x - fwd.x * dist + right.x * sway;
                        double z = base.z - fwd.z * dist + right.z * sway;
                        double y = base.y + 0.04 + t * 0.12;

                        var fx = (i % 2 == 0) ? soft : main;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if (!moving && slowTick) {
                    int pts = 8;
                    double r = 0.40;
                    double y = base.y + 0.12;

                    for (int i = 0; i < pts; i++) {
                        double ang = phase * 0.8 + i * (TAU / pts);
                        double x = base.x + Math.cos(ang) * r;
                        double z = base.z + Math.sin(ang) * r;

                        var fx = (i % 2 == 0) ? soft : main;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }

                    spawn(player, main.at(new Vector3(base.x, y + 0.05, base.z)));
                }

                if ((currentTick % 40) == 0 && moving) {
                    int sideSign = rng.nextBoolean() ? 1 : -1;
                    Vector3 sideDir = new Vector3(right.x * sideSign, 0, right.z * sideSign);

                    Position origin = base.add(sideDir.multiply(0.45)).add(0, 0.7, 0);

                    int gustPts = 4;
                    for (int i = 0; i < gustPts; i++) {
                        double t = i / (double) (gustPts - 1);

                        double along = 0.25 + t * 0.5;
                        double lift  = 0.05 + t * 0.22;

                        double x1 = origin.x - fwd.x * along + sideDir.x * (0.10 * (1.0 - t));
                        double z1 = origin.z - fwd.z * along + sideDir.z * (0.10 * (1.0 - t));
                        double y1 = origin.y + lift;

                        var fx1 = (i == gustPts - 1) ? main : soft;
                        spawn(player, fx1.at(new Vector3(x1, y1, z1)));
                    }
                }

                if ((currentTick % 90) == 0) {
                    ring(player, main, base, 0.55, 12, phase * 1.3, 0.03);
                    ring(player, soft, base, 0.32, 8, -phase * 0.9, 0.02);

                    spawn(player, new SmokeParticle(base.add(0, 0.20, 0)));
                }
            }

            // 🩵 Cœur de Givre
            case "frost_heart" -> {
                var frost = dust(160, 220, 255);
                var deep  = dust(110, 180, 245);
                var white = dust(240, 250, 255);

                Position chest = base.add(0, 1.1, 0);

                if (moving) {

                    if (fastTick) {
                        int shards = 8;
                        for (int i = 0; i < shards; i++) {

                            double ang = (i / (double) shards) * TAU + phase * 0.6;
                            double speed = 0.18 + Math.random() * 0.1;

                            double x = chest.x + Math.cos(ang) * 0.15;
                            double z = chest.z + Math.sin(ang) * 0.15;
                            double y = chest.y + Math.random() * 0.15;

                            Vector3 dir = new Vector3(
                                    Math.cos(ang) * speed,
                                    0.07 + Math.random() * 0.05,
                                    Math.sin(ang) * speed
                            );

                            var fx = (i % 2 == 0) ? frost : deep;
                            spawn(player, fx.at(new Vector3(x + dir.x, y + dir.y, z + dir.z)));
                        }
                    }

                    if (fastTick) {
                        trail(player, pos -> frost.at(pos),
                                base.add(0, 0.2, 0),
                                fwd, 1.3, 6, 0.0);
                    }

                    if (fastTick) {
                        int cracks = 4;
                        double r = 0.32;

                        for (int i = 0; i < cracks; i++) {
                            double ang = i * (TAU / cracks) + phase * 1.4;

                            double x = base.x + Math.cos(ang) * r;
                            double z = base.z + Math.sin(ang) * r;
                            double y = base.y + 0.05;

                            spawn(player, deep.at(new Vector3(x, y, z)));
                            spawn(player, frost.at(new Vector3(x, y + 0.03, z)));
                        }
                    }

                    return;
                }

                if (fastTick) {
                    int shards = 6;
                    for (int i = 0; i < shards; i++) {
                        double t = i / (double) shards;
                        double ang = t * TAU + Math.sin(phase * 0.5) * 0.3;

                        double height = 0.05 + Math.sin(phase * 0.4 + t * 3.0) * 0.06;

                        double x = chest.x + Math.cos(ang) * 0.18;
                        double z = chest.z + Math.sin(ang) * 0.18;
                        double y = chest.y + height;

                        var fx = (i % 2 == 0) ? frost : deep;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }

                    double vapY = chest.y + 0.25;
                    spawn(player, white.at(new Vector3(
                            chest.x,
                            vapY + Math.sin(phase * 0.3) * 0.04,
                            chest.z
                    )));
                }

                if (medTick) {
                    int orbit = 3;
                    double r = 0.3;
                    for (int i = 0; i < orbit; i++) {
                        double ang = phase * 0.6 + i * (TAU / orbit);

                        double x = chest.x + Math.cos(ang) * r;
                        double z = chest.z + Math.sin(ang) * r;
                        double y = chest.y + 0.07 * Math.sin(phase + i);

                        var fx = (i % 2 == 0) ? frost : deep;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if (slowTick) {
                    double y = chest.y + 0.12;

                    spawn(player, white.at(new Vector3(
                            chest.x + Math.random() * 0.1 - 0.05,
                            y,
                            chest.z + Math.random() * 0.1 - 0.05
                    )));
                }
            }

            // 🌑 Orbites du Néant
            case "void_orbits" -> {
                var core  = dust(90, 0, 120);
                var trail = dust(50, 0, 80);
                var pulse = dust(120, 0, 160);

                double baseY = base.y + 1.05;

                double radius = 1.05;
                double vertical = 0.17;

                if (fastTick) {
                    int orbs = 3;

                    for (int i = 0; i < orbs; i++) {

                        double orbitSpeed = moving ? 0.35 : 0.18;
                        double ang = phase * orbitSpeed + i * (TAU / orbs);

                        double x = base.x + Math.cos(ang) * radius * 1.1;
                        double z = base.z + Math.sin(ang) * radius * 0.9;

                        double y = baseY + Math.sin(phase * 0.7 + i) * vertical;

                        Vector3 pos = new Vector3(x, y, z);

                        spawn(player, core.at(pos));

                        double tAng = ang - 0.55;
                        double tx = base.x + Math.cos(tAng) * (radius * 0.85);
                        double tz = base.z + Math.sin(tAng) * (radius * 0.85);
                        double ty = y - 0.04;

                        spawn(player, trail.at(new Vector3(tx, ty, tz)));
                    }
                }

                if (moving && fastTick) {
                    trail(player,
                            pos -> trail.at(pos),
                            base.add(0, 0.3, 0),
                            fwd, 1.4, 6, 0.0);

                    double backX = base.x - fwd.x * 0.5;
                    double backZ = base.z - fwd.z * 0.5;
                    double backY = base.y + 0.9 + Math.sin(phase * 2.0) * 0.05;

                    spawn(player, pulse.at(new Vector3(backX, backY, backZ)));
                }

                if (!moving && medTick) {

                    ring(player, trail, base, 0.65, 14, -phase * 0.3, 0.04);

                    spawn(player, pulse.at(new Vector3(
                            base.x,
                            base.y + 1.0,
                            base.z
                    )));
                }
            }

            // 🪶 Poussière d’Ombre
            case "shadow_dust" -> {
                var dark  = dust(15, 15, 20);
                var voidP = dust(40, 0, 60);
                var wisp  = dust(90, 0, 120);

                double yFeet = base.y + 0.05;

                if (fastTick) {
                    int layers = 2;
                    int pts = 12;
                    double baseR = 0.55;

                    for (int L = 0; L < layers; L++) {
                        for (int i = 0; i < pts; i++) {

                            double t = i / (double) pts;
                            double ang = phase * (0.25 + L * 0.1) + t * TAU;

                            double distort = Math.sin(phase * 1.6 + t * 6.0) * 0.12;
                            double r = baseR + distort + L * 0.10;

                            double x = base.x + Math.cos(ang) * r;
                            double z = base.z + Math.sin(ang) * r;

                            double y = base.y + 0.1 + Math.sin(phase * 0.9 + L) * 0.05;

                            var fx = (i % 2 == 0) ? dark : voidP;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }
                }

                if (fastTick) {
                    int rising = 3;
                    for (int i = 0; i < rising; i++) {

                        double t = i / 2.0;
                        double y = yFeet + t * 0.45;

                        double sway = Math.sin(phase * 1.4 + t * 4.0) * 0.06;

                        spawn(player, dark.at(new Vector3(
                                base.x + sway,
                                y,
                                base.z - sway
                        )));
                    }
                }

                if (moving && fastTick) {

                    double offset = 0.28;

                    Vector3 left  = new Vector3(base.x - right.x * offset, yFeet, base.z - right.z * offset);
                    Vector3 rightP = new Vector3(base.x + right.x * offset, yFeet, base.z + right.z * offset);

                    spawn(player, dark.at(left));
                    spawn(player, dark.at(rightP));

                    spawn(player, voidP.at(base.add(0, 0.12, 0)));
                }

                if (moving && fastTick) {

                    int shards = 6;
                    for (int i = 0; i < shards; i++) {

                        double ang = (i / (double) shards) * TAU + phase * 0.8;
                        double speed = 0.10 + Math.random() * 0.07;

                        Vector3 origin = base.add(
                                Math.cos(ang) * 0.2,
                                0.25 + Math.random() * 0.15,
                                Math.sin(ang) * 0.2
                        );

                        Vector3 dir = new Vector3(
                                Math.cos(ang) * speed,
                                0.03 + Math.random() * 0.03,
                                Math.sin(ang) * speed
                        );

                        spawn(player, wisp.at(origin.add(dir)));
                    }
                }

                if (!moving && medTick) {

                    int pts = 14;
                    double r = 0.75;

                    for (int i = 0; i < pts; i++) {
                        double a = phase * 0.25 + i * (TAU / pts);

                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;

                        double y = base.y + 0.05 + Math.sin(i * 0.6) * 0.03;

                        spawn(player, dark.at(new Vector3(x, y, z)));
                    }
                }
            }

            // 💚 Essence de la Vie
            case "life_essence" -> {
                var green = dust(60, 220, 80);
                var gold  = dust(255, 215, 80);
                var soft  = dust(120, 255, 140);

                Position mid = base.add(0, 1.0, 0);

                if (fastTick) {
                    int pts = 12;
                    double r = 0.62;

                    for (int i = 0; i < pts; i++) {

                        double t = i / (double) pts;
                        double ang = phase * 0.65 + t * TAU;

                        double breathe = 0.07 * Math.sin(phase * 1.0 + t * 8.0);

                        double x = mid.x + Math.cos(ang) * (r + breathe);
                        double z = mid.z + Math.sin(ang) * (r + breathe);
                        double y = mid.y + 0.07 * Math.sin(phase * 1.3 + t * 5.0);

                        var fx = (i % 3 == 0) ? gold : green;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if (fastTick) {
                    int rings = 3;
                    for (int r = 0; r < rings; r++) {

                        double tinyR = 0.20 + r * 0.06;
                        double y = mid.y + (r - 1) * 0.15;

                        double ang = phase * (0.9 + r * 0.25);

                        double x = mid.x + Math.cos(ang) * tinyR;
                        double z = mid.z + Math.sin(ang) * tinyR;

                        var fx = (r % 2 == 0) ? soft : gold;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if (moving) {
                    if (fastTick) {
                        trail(player,
                                pos -> soft.at(pos),
                                base.add(0, 0.25, 0),
                                fwd, 1.3, 7, 0.0);
                    }

                    if (fastTick) {

                        int bursts = 4;
                        for (int i = 0; i < bursts; i++) {

                            double ang = (i / (double) bursts) * TAU + phase * 1.2;
                            double speed = 0.10 + Math.random() * 0.08;

                            Vector3 origin = mid.add(
                                    Math.cos(ang) * 0.25,
                                    0.05 + Math.random() * 0.1,
                                    Math.sin(ang) * 0.25
                            );

                            Vector3 out = new Vector3(
                                    Math.cos(ang) * speed,
                                    0.03 + Math.random() * 0.04,
                                    Math.sin(ang) * speed
                            );

                            var fx = (i % 2 == 0) ? soft : green;
                            spawn(player, fx.at(origin.add(out)));
                        }
                    }

                    if (fastTick) {
                        int stepFx = 3;
                        double downR = 0.32;

                        for (int i = 0; i < stepFx; i++) {
                            double ang = (i / 3.0) * TAU + phase * 0.5;

                            double x = base.x + Math.cos(ang) * downR;
                            double z = base.z + Math.sin(ang) * downR;
                            double y = base.y + 0.03;

                            spawn(player, green.at(new Vector3(x, y, z)));
                        }
                    }

                    return;
                }

                if (slowTick) {
                    int steps = 5;
                    double h = 1.6;

                    for (int i = 0; i < steps; i++) {
                        double t = i / (double) (steps - 1);
                        double y = base.y + 0.4 + t * h;

                        var fx = (i % 2 == 0) ? green : gold;
                        spawn(player, fx.at(new Vector3(base.x, y, base.z)));
                    }
                }

                if (medTick) {
                    spawn(player, soft.at(new Vector3(
                            mid.x,
                            mid.y + Math.sin(phase * 1.6) * 0.08,
                            mid.z
                    )));
                }
            }

            // 🩷 Pluie d’Amour
            case "love_rain" -> {
                var pink  = dust(255, 160, 210);
                var deep  = dust(230, 90, 160);
                var soft  = dust(255, 200, 220);

                Random rng = new Random(seed);

                if (fastTick) {
                    int drops = 1;
                    for (int i = 0; i < drops; i++) {

                        double a = rng.nextDouble() * TAU;
                        double r = 0.25 + rng.nextDouble() * 0.95;

                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;
                        double y = base.y + 1.6 + rng.nextDouble() * 0.7;

                        var fx = (i % 3 == 0) ? soft : (rng.nextBoolean() ? pink : deep);
                        spawn(player, fx.at(new Vector3(x, y, z)));

                        if (i % 2 == 0) {
                            spawn(player, new HeartParticle(new Vector3(x, y + 0.1, z)));
                        }
                    }
                }

                if (moving && fastTick) {

                    double t = (phase * 1.2) % TAU;
                    double swirlR = 0.32;

                    double sx = base.x + Math.cos(t) * swirlR;
                    double sz = base.z + Math.sin(t) * swirlR;
                    double sy = base.y + 0.2 + Math.sin(phase * 2.0) * 0.06;

                    spawn(player, pink.at(new Vector3(sx, sy, sz)));
                    spawn(player, new HeartParticle(new Vector3(sx, sy + 0.05, sz)));

                    int steps = 3;
                    for (int i = 0; i < steps; i++) {

                        double back = 0.25 + i * 0.15;
                        double px = base.x - fwd.x * back + right.x * (Math.sin(phase * 2 + i) * 0.1);
                        double pz = base.z - fwd.z * back + right.z * (Math.sin(phase * 2 + i) * 0.1);
                        double py = base.y + 0.1 + Math.sin(phase * 3 + i * 1.4) * 0.03;

                        spawn(player, deep.at(new Vector3(px, py, pz)));

                        if (i == 1)
                            spawn(player, new HeartParticle(new Vector3(px, py + 0.06, pz)));
                    }
                }

                if (!moving && medTick) {
                    ring(player, pink, base, 0.7, 14, phase * 0.5, 0.03);
                    spawn(player, new HeartParticle(base.add(0, 1.3 + Math.sin(phase) * 0.05, 0)));
                }
            }

            // 🔮 Faille Dimensionnelle
            case "dimensional_rift" -> {

                var violet   = dust(180, 0, 230);
                var magenta  = dust(255, 40, 180);
                var cyan     = dust(80, 200, 255);
                var darkRift = dust(40, 0, 70);

                Position c = base.add(0, 0.55, 0);
                Random rng = new Random(seed ^ 0xD1A3F51L);

                if (fastTick) {
                    int shards = 18;
                    double baseR = 0.35;
                    double maxR  = 1.2;

                    for (int i = 0; i < shards; i++) {
                        double t = i / (double) shards;

                        double wave = Math.sin(phase * 1.4 + t * 6.0) * 0.18;
                        double r = baseR + (maxR - baseR) * t * 0.85 + wave;

                        double ang = phase * 0.8 + t * TAU + Math.sin(phase * 2.3 + i) * 0.4;

                        double x = c.x + Math.cos(ang) * r;
                        double z = c.z + Math.sin(ang) * r;
                        double y = c.y + 0.02 * Math.sin(phase * 1.6 + t * 7.0);

                        var fx = (i % 5 == 0)
                                ? cyan
                                : (i % 2 == 0 ? magenta : violet);

                        spawn(player, fx.at(new Vector3(x, y, z)));

                        Vector3 shadow = new Vector3(
                                x * 0.96 + c.x * 0.04,
                                y - 0.05,
                                z * 0.96 + c.z * 0.04
                        );
                        spawn(player, darkRift.at(shadow));
                    }
                }

                if (moving && fastTick) {
                    int fractures = 5;
                    for (int i = 0; i < fractures; i++) {
                        double back = 0.4 + i * 0.25;

                        double swing = Math.sin(phase * 2.0 + i * 1.3) * 0.18;
                        double x = base.x - fwd.x * back + right.x * swing;
                        double z = base.z - fwd.z * back + right.z * swing;
                        double y = base.y + 0.2 + Math.sin(phase * 1.8 + i) * 0.08;

                        var fx = (i % 3 == 0) ? cyan : (i % 2 == 0 ? magenta : violet);
                        Vector3 crack = new Vector3(x, y, z);
                        spawn(player, fx.at(crack));

                        Vector3 shard = new Vector3(
                                x + (rng.nextDouble() * 0.20 - 0.10),
                                y - 0.10,
                                z + (rng.nextDouble() * 0.20 - 0.10)
                        );
                        spawn(player, darkRift.at(shard));
                    }

                    double t = phase * 1.6;
                    double rv = 0.55;
                    double vx = base.x + Math.cos(t) * rv;
                    double vz = base.z + Math.sin(t) * rv;
                    double vy = base.y + 1.1 + Math.sin(t * 3.2) * 0.14;

                    Vector3 orb = new Vector3(vx, vy, vz);
                    spawn(player, cyan.at(orb));
                    spawn(player, new EnchantmentTableParticle(orb));
                }

                if (!moving && medTick) {
                    int ringPts = 18;
                    double r = 1.0;

                    for (int i = 0; i < ringPts; i++) {
                        double a = phase * 0.5 + i * (TAU / ringPts);

                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;
                        double y = base.y + 0.05 * Math.sin(phase * 1.3 + i);

                        var fx = (i % 4 == 0) ? cyan : (i % 2 == 0 ? magenta : violet);
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }

                    double vy = base.y + 1.2 + Math.sin(phase * 2.0) * 0.11;
                    Vector3 corePos = new Vector3(base.x, vy, base.z);
                    spawn(player, darkRift.at(corePos));
                    spawn(player, new EnchantmentTableParticle(corePos));

                    dualSpiral(player, magenta, base.add(0, 0.4, 0), 0.5, 1.6, 14, phase * 0.9);
                }

                if ((currentTick % 34) == 0) {
                    double sliceAng = rng.nextDouble() * TAU;
                    double cos = Math.cos(sliceAng);
                    double sin = Math.sin(sliceAng);

                    double len = 2.8;
                    int segs = 8;

                    for (int i = 0; i <= segs; i++) {
                        double t = i / (double) segs;
                        double offset = (t - 0.5) * len;

                        double x = base.x + cos * offset;
                        double z = base.z + sin * offset;
                        double y = base.y + 0.5 + Math.sin(phase * 1.5 + t * 5.0) * 0.15;

                        var fx = (i % 3 == 0) ? cyan : (i % 2 == 0 ? magenta : violet);
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if ((currentTick % 80) == 0) {
                    burstRandom(player, darkRift, c, 0.4, 10, seed ^ 0xB1A4A011L, 0.0, 0.4);

                    ring(player, violet, c, 0.45, 14, phase * 1.4, 0.02);

                    int rays = 10;
                    double maxR = 2.2;

                    for (int i = 0; i < rays; i++) {
                        double ang = phase * 0.9 + i * (TAU / rays);
                        double dx = Math.cos(ang);
                        double dz = Math.sin(ang);

                        int steps = 4;
                        for (int s = 1; s <= steps; s++) {
                            double t = s / (double) steps;
                            double dist = 0.6 + (maxR - 0.6) * t;

                            double x = c.x + dx * dist;
                            double z = c.z + dz * dist;
                            double y = c.y + 0.1 + t * 0.4 + Math.sin(phase * 2.0 + t * 6.0) * 0.12;

                            var fx = (s == steps) ? cyan : (s % 2 == 0 ? magenta : violet);
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    burstRandom(player, cyan,    base.add(0, 1.4, 0), 1.8, 8, seed ^ 0xB1A4A021L, 0.2, 0.9);
                    burstRandom(player, magenta, base.add(0, 1.4, 0), 1.8, 6, seed ^ 0xB1A4A031L, 0.2, 0.9);
                }

                if ((currentTick % 18) == 0) {
                    int sparks = 4;
                    for (int i = 0; i < sparks; i++) {
                        double a = rng.nextDouble() * TAU;
                        double r = 0.2 + rng.nextDouble() * 0.5;
                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;
                        double y = base.y + 1.0 + rng.nextDouble() * 0.7;
                        spawn(player, cyan.at(new Vector3(x, y, z)));
                    }
                }
            }

            // ☄️ Comète (version plus compacte)
            case "comet" -> {
                var core   = dust(255, 250, 245);
                var tail1  = dust(210, 190, 255);
                var tail2  = dust(150, 120, 230);

                Position center = base.add(0, 1.1, 0);
                Random rng = new Random(seed ^ 0xC0FFEE1L);

                if (fastTick) {
                    int cometCount = 3;
                    double radius = moving ? 1.1 : 0.9;
                    double speed  = moving ? 1.0 : 0.7;

                    for (int i = 0; i < cometCount; i++) {
                        double baseAngle = phase * speed + i * (TAU / cometCount);
                        double wobble    = Math.sin(phase * 0.8 + i) * 0.08;

                        double cx = base.x + Math.cos(baseAngle) * (radius + wobble);
                        double cz = base.z + Math.sin(baseAngle) * (radius + wobble);
                        double cy = center.y + Math.sin(phase * 0.9 + i * 1.4) * 0.10;

                        Vector3 head = new Vector3(cx, cy, cz);
                        spawn(player, core.at(head));

                        int tailSteps = 3;
                        for (int t = 1; t <= tailSteps; t++) {
                            double tt = t / (double) (tailSteps + 1);
                            double tx = cx - (cx - center.x) * tt * 0.6;
                            double tz = cz - (cz - center.z) * tt * 0.6;
                            double ty = cy - tt * 0.12;

                            var fx = (t % 2 == 0) ? tail1 : tail2;
                            spawn(player, fx.at(new Vector3(tx, ty, tz)));
                        }
                    }
                }

                if (moving && medTick) {
                    int segs = 5;
                    double back = 1.4;
                    for (int i = 0; i < segs; i++) {
                        double t = i / (double) (segs - 1);
                        double dist = t * back;

                        double wave = Math.sin(phase * 0.9 + t * 4.0) * 0.12;

                        double x = base.x - fwd.x * dist + right.x * wave;
                        double z = base.z - fwd.z * dist + right.z * wave;
                        double y = base.y + 0.06 + t * 0.10;

                        var fx = (i % 2 == 0) ? tail2 : tail1;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if (!moving && medTick) {
                    spiral(player, tail1, base.add(0, 0.4, 0), 0.45, 1.3, 12, phase * 0.9);

                    double crownY = base.y + 1.8;
                    int crownPts = 6;
                    double crownR = 0.8;
                    for (int i = 0; i < crownPts; i++) {
                        double t = i / (double) crownPts;
                        double ang = phase * 0.7 + t * TAU;
                        double x = base.x + Math.cos(ang) * crownR;
                        double z = base.z + Math.sin(ang) * crownR;
                        double y = crownY + Math.sin(phase * 1.3 + t * 5.0) * 0.05;

                        var fx = (i % 2 == 0) ? core : tail1;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                if ((currentTick % 28) == 0) {
                    double ang = rng.nextDouble() * TAU;
                    double dist = 3.0 + rng.nextDouble() * 1.2;

                    double startX = center.x + Math.cos(ang) * dist;
                    double startZ = center.z + Math.sin(ang) * dist;
                    double startY = center.y + 3.0 + rng.nextDouble() * 1.2;

                    Vector3 start = new Vector3(startX, startY, startZ);

                    int steps = 5;
                    for (int i = 0; i < steps; i++) {
                        double t = i / (double) (steps - 1);
                        double x = start.x + (center.x - start.x) * t;
                        double y = start.y + (center.y - start.y) * t;
                        double z = start.z + (center.z - start.z) * t;

                        var fx = (i >= steps - 2) ? core : tail1;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }

                    burstRandom(player, core,  center, 0.8, 4, seed ^ 0xC0FFEE2L, 0.0, 0.3);
                    burstRandom(player, tail2, center, 1.1, 6, seed ^ 0xC0FFEE3L, 0.1, 0.6);
                    ring(player, tail1, center, 0.9, 12, phase * 1.2, 0.04);
                }

                if ((currentTick % 80) == 0) {
                    int streaks = 4;
                    double showerRadius = 1.8;

                    for (int s = 0; s < streaks; s++) {
                        double baseAng = rng.nextDouble() * TAU;
                        double offAng  = baseAng + (Math.PI / 2.0);

                        double sx = base.x + Math.cos(baseAng) * showerRadius;
                        double sz = base.z + Math.sin(baseAng) * showerRadius;
                        double sy = base.y + 2.4 + rng.nextDouble() * 0.6;

                        double dx = Math.cos(offAng);
                        double dz = Math.sin(offAng);

                        int segs = 4;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double x = sx + dx * (t * 1.2 - 0.6);
                            double z = sz + dz * (t * 1.2 - 0.6);
                            double y = sy - t * 0.6;

                            var fx = (i == segs - 1) ? core : tail1;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }
                }
            }

            // 🌊 Éclaboussures Marines
            case "marine_splash" -> {

                var water1   = dust(70, 160, 255);
                var water2   = dust(40, 120, 230);
                var aqua     = dust(90, 220, 255);
                var bubble   = dust(180, 240, 255);
                var gleam    = dust(255, 255, 255);

                Random rng = new Random(seed);

                if (moving && fastTick) {

                    double side = 0.34;
                    double y = base.y + 0.04;

                    Vector3 lp = new Vector3(base.x - right.x * side, y, base.z - right.z * side);
                    Vector3 rp = new Vector3(base.x + right.x * side, y, base.z + right.z * side);

                    spawn(player, water1.at(lp));
                    spawn(player, water2.at(rp));

                    int bub = 2;
                    for (int i = 0; i < bub; i++) {
                        double ang = rng.nextDouble() * TAU;
                        double dist = 0.12 + rng.nextDouble() * 0.15;

                        double bx = base.x + Math.cos(ang) * dist;
                        double bz = base.z + Math.sin(ang) * dist;
                        double by = y + 0.07 + rng.nextDouble() * 0.05;

                        spawn(player, bubble.at(new Vector3(bx, by, bz)));
                    }

                    int ripples = 3;
                    for (int i = 0; i < ripples; i++) {
                        double ang = phase * 2.0 + i * 2.1;
                        double rx = base.x + Math.cos(ang) * 0.45;
                        double rz = base.z + Math.sin(ang) * 0.45;
                        double ry = base.y + 0.03;

                        spawn(player, water2.at(new Vector3(rx, ry, rz)));
                    }

                    if ((seed + currentTick) % 22 == 0) {
                        double gx = base.x - fwd.x * 0.4;
                        double gz = base.z - fwd.z * 0.4;
                        double gy = base.y + 0.15;

                        spawn(player, gleam.at(new Vector3(gx, gy, gz)));
                    }
                }

                if (moving && medTick) {

                    int droplets = 4;
                    for (int i = 0; i < droplets; i++) {

                        double ang = (i / 4.0) * TAU + phase * 1.1;
                        double speed = 0.08 + rng.nextDouble() * 0.05;

                        double px = base.x + Math.cos(ang) * 0.2;
                        double pz = base.z + Math.sin(ang) * 0.2;
                        double py = base.y + 0.1;

                        Vector3 dir = new Vector3(
                                Math.cos(ang) * speed,
                                0.05 + rng.nextDouble() * 0.03,
                                Math.sin(ang) * speed
                        );

                        var fx = (i % 2 == 0) ? aqua : water1;
                        spawn(player, fx.at(new Vector3(px + dir.x, py + dir.y, pz + dir.z)));
                    }
                }

                if (!moving && fastTick) {

                    int ringPts = 14;
                    double r = 0.75;

                    for (int i = 0; i < ringPts; i++) {
                        double a = phase * 0.7 + i * (TAU / ringPts);

                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;

                        double y = base.y + 0.04 + Math.sin(i * 0.7 + phase * 1.5) * 0.02;

                        var fx = (i % 3 == 0) ? aqua : water1;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }

                    double cx = base.x;
                    double cy = base.y + 0.1 + Math.sin(phase * 2.2) * 0.05;
                    double cz = base.z;

                    spawn(player, aqua.at(new Vector3(cx, cy, cz)));
                    spawn(player, bubble.at(new Vector3(cx, cy + 0.03, cz)));

                    if (rng.nextDouble() < 0.05) {
                        spawn(player, gleam.at(new Vector3(cx, cy + 0.08, cz)));
                    }
                }
            }

            // ⚰️ Âmes Errantes
            case "wandering_souls" -> {
                var soul = dust(90, 170, 255);
                var dark = dust(20, 40, 70);

                // Colonnes d’âmes qui montent autour du joueur
                if (fastTick) {
                    int cols = 4;
                    int steps = 4;
                    double radius = 0.7;

                    for (int i = 0; i < cols; i++) {
                        double a = phase * 0.3 + i * (TAU / cols);
                        double x = base.x + Math.cos(a) * radius;
                        double z = base.z + Math.sin(a) * radius;

                        for (int s = 0; s < steps; s++) {
                            double t = s / (double) (steps - 1);
                            double y = base.y + 0.4 + t * 1.6;
                            var fx = (s == steps - 1) ? soul : dark;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }
                }

                // À l’arrêt : petit noyau au-dessus de la tête
                if (!moving && slowTick) {
                    spawn(player, soul.at(new Vector3(base.x, base.y + 1.9, base.z)));
                }
            }
        }
    }

    private void spawn(EnginePlayer viewer, cn.nukkit.level.particle.Particle p) {
        Level lvl = viewer.getLevel();
        if (lvl != null) {
            lvl.addParticle(p, viewer);
            lvl.addParticle(p, viewer.getViewers().values());
        }
    }

    private interface PFactory { cn.nukkit.level.particle.Particle at(Vector3 pos); }
    private PFactory dust(int r, int g, int b) { return pos -> new DustParticle(pos, r, g, b); }

    private void ring(EnginePlayer viewer, PFactory fx, Position c, double r, int points, double phase, double yOff) {
        double y = c.getY() + yOff;
        for (int i = 0; i < points; i++) {
            double a = phase + i * (TAU / points);
            double x = c.getX() + Math.cos(a) * r;
            double z = c.getZ() + Math.sin(a) * r;
            spawn(viewer, fx.at(new Vector3(x, y, z)));
        }
    }

    private void spiral(EnginePlayer viewer, PFactory fx, Position c, double r, double height, int points, double phase) {
        for (int i = 0; i < points; i++) {
            double t = (double) i / Math.max(1, points - 1);
            double a = phase + t * TAU;
            double x = c.getX() + Math.cos(a) * r;
            double z = c.getZ() + Math.sin(a) * r;
            double y = c.getY() + 0.3 + t * height;
            spawn(viewer, fx.at(new Vector3(x, y, z)));
        }
    }

    private void dualSpiral(EnginePlayer viewer, PFactory fx, Position c, double r, double h, int points, double phase) {
        spiral(viewer, fx, c, r, h, points, phase);
        spiral(viewer, fx, c, r, h, points, phase + Math.PI);
    }

    private void coneBurst(EnginePlayer viewer, PFactory fx, Position from, Vector3 dir, double radius, int count, long seed, double yMin, double yMax) {
        java.util.Random rng = new java.util.Random(seed);
        Vector3 f = normalizeXZ(dir);
        Vector3 r = rightDir(viewer);
        for (int i = 0; i < count; i++) {
            double d = rng.nextDouble() * radius;
            double s = (rng.nextDouble() - 0.5) * radius * 0.6 * (1.0 - d / radius);
            double y = from.y + yMin + rng.nextDouble() * (yMax - yMin);
            double x = from.x + f.x * d + r.x * s;
            double z = from.z + f.z * d + r.z * s;
            spawn(viewer, fx.at(new Vector3(x, y, z)));
        }
    }

    private void trail(EnginePlayer viewer, PFactory fx, Position from, Vector3 dir, double length, int points, double yOff) {
        Vector3 d = normalizeXZ(dir);
        for (int i = 0; i < points; i++) {
            double t = (double) i / Math.max(1, points - 1);
            double dist = t * length;
            Vector3 p = new Vector3(from.x - d.x * dist, from.y + yOff, from.z - d.z * dist);
            spawn(viewer, fx.at(p));
        }
    }

    private void footsteps(EnginePlayer viewer, PFactory fx, Position base, Vector3 right, double side, double yOff) {
        Vector3 lp = new Vector3(base.x - right.x * side, base.y + yOff, base.z - right.z * side);
        Vector3 rp = new Vector3(base.x + right.x * side, base.y + yOff, base.z + right.z * side);
        spawn(viewer, fx.at(lp));
        spawn(viewer, fx.at(rp));
    }

    private void burstRandom(EnginePlayer viewer, PFactory fx, Position c, double radius, int count, long seed, double yMin, double yMax) {
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < count; i++) {
            double a = rng.nextDouble() * TAU;
            double rr = radius * (0.4 + 0.6 * rng.nextDouble());
            double x = c.getX() + Math.cos(a) * rr;
            double z = c.getZ() + Math.sin(a) * rr;
            double y = c.getY() + (yMin + (yMax - yMin) * rng.nextDouble());
            spawn(viewer, fx.at(new Vector3(x, y, z)));
        }
    }

    private static Vector3 forwardDir(EnginePlayer p) {
        double yaw = Math.toRadians(p.getYaw());
        return new Vector3(-Math.sin(yaw), 0, Math.cos(yaw));
    }
    private static Vector3 rightDir(EnginePlayer p) {
        double yaw = Math.toRadians(p.getYaw());
        return new Vector3(Math.cos(yaw), 0, Math.sin(yaw));
    }
    private static Vector3 normalizeXZ(Vector3 v) {
        double l = Math.sqrt(v.x * v.x + v.z * v.z);
        if (l < 1e-6) return new Vector3(0, 0, 0);
        return new Vector3(v.x / l, 0, v.z / l);
    }

    private static int[] hsvToRgb(double h, double s, double v) {
        double i = Math.floor(h * 6.0);
        double f = h * 6.0 - i;
        double p = v * (1.0 - s);
        double q = v * (1.0 - f * s);
        double t = v * (1.0 - (1.0 - f) * s);
        double r, g, b;
        switch ((int) i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return new int[]{(int)(r * 255), (int)(g * 255), (int)(b * 255)};
    }
}
