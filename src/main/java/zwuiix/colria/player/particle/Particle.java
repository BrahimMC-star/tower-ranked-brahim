package zwuiix.colria.player.particle;

import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.Vector3;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Random;

public class Particle {
    private static final double TAU = Math.PI * 2;

    private final String identifier;
    private final TranslationKeys name;
    private final TranslationKeys description;
    private final Item reference;
    private final long cost;
    private final boolean flying;

    Particle(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost, boolean flying) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.cost = cost;
        this.flying = flying;
    }

    public String getIdentifier() { return identifier; }
    public TranslationKeys getName() { return name; }
    public TranslationKeys getDescription() { return description; }
    public Item getReference() { return reference.clone(); }
    public long getCost() { return cost; }
    public boolean isFlying() { return flying; }

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
                var flake = dust(230, 240, 255);

                if (moving) {
                    if (medTick) {
                        int segs = 7;
                        double back = 1.6;
                        double side = 0.35;

                        for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);
                                double dist = t * back;
                                double sway = Math.sin(phase * 0.5 + t * 3.0 + sideSign) * 0.10;

                                double x = base.x - fwd.x * dist + right.x * (sideSign * side + sway);
                                double z = base.z - fwd.z * dist + right.z * (sideSign * side + sway);
                                double y = base.y + 0.04; // bien au sol

                                spawn(player, flake.at(new Vector3(x, y, z)));
                            }
                        }

                        spawn(player, new SmokeParticle(base.add(0, 1.55, 0)));
                    }
                } else if (slowTick) {
                    int arms = 6;
                    int steps = 6;
                    for (int a = 0; a < arms; a++) {
                        for (int s = 0; s < steps; s++) {
                            double t = s / (double) (steps - 1);
                            double radius = 0.20 + t * 0.55;
                            double ang = phase * 0.5 + a * (TAU / arms) + t * 0.8;

                            double x = base.x + Math.cos(ang) * radius;
                            double z = base.z + Math.sin(ang) * radius;
                            double y = base.y + 0.03; // collé au sol

                            spawn(player, flake.at(new Vector3(x, y, z)));
                        }
                    }

                    int ringPoints = 14;
                    double ringRadius = 0.8;
                    for (int i = 0; i < ringPoints; i++) {
                        double ang = phase * 0.3 + i * (TAU / ringPoints);
                        double x = base.x + Math.cos(ang) * ringRadius;
                        double z = base.z + Math.sin(ang) * ringRadius;
                        double y = base.y + 0.035;

                        if ((i + currentTick) % 2 == 0) {
                            spawn(player, flake.at(new Vector3(x, y, z)));
                        }
                    }

                    int sparkles = 4;
                    for (int i = 0; i < sparkles; i++) {
                        double ang = phase * 0.9 + i * (TAU / sparkles);
                        double r = 0.15 + 0.15 * Math.sin(phase * 1.3 + i);
                        double x = base.x + Math.cos(ang) * r;
                        double z = base.z + Math.sin(ang) * r;
                        double y = base.y + 0.04;

                        spawn(player, flake.at(new Vector3(x, y, z)));
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

            // Dancing Flame
            case "dancing_flame" -> {
                var ember = dust(240, 140, 60);

                if (moving) {
                    if (fastTick) {
                        footsteps(player, FlameParticle::new, base, right, 0.35, 0.08);

                        trail(player, pos -> new FlameParticle(pos),
                                base.add(0, 0.6, 0),
                                fwd, 1.1, 5, 0.0);

                        burstRandom(player, ember, base.add(0, 1.0, 0),
                                0.5, 2, seed, 0.0, 0.5);
                    }

                    if (medTick) {
                        int segs = 6;
                        double radius = 0.8;
                        double y = base.y + 0.9;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double ang = (Math.PI * 0.7) + t * (Math.PI * 0.6); // arc centré derrière
                            double x = base.x - fwd.x * 0.4 + Math.cos(ang) * radius * 0.4;
                            double z = base.z - fwd.z * 0.4 + Math.sin(ang) * radius * 0.4;
                            spawn(player, ember.at(new Vector3(x, y, z)));
                        }
                    }

                } else {
                    if (fastTick) {
                        double yFeet = base.y + 0.04;
                        ring(player, ember, base, 0.6, 12, phase * 0.7, 0.04);

                        int segments = 4;
                        for (int side = -1; side <= 1; side += 2) {
                            double sx = right.x * 0.4 * side;
                            double sz = right.z * 0.4 * side;
                            for (int i = 0; i < segments; i++) {
                                double t = i / (double) (segments - 1);
                                double wobble = Math.sin(phase * 0.6 + t * 3.0 + side * 0.8) * 0.12;
                                double x = base.x + sx + fwd.x * wobble;
                                double z = base.z + sz + fwd.z * wobble;
                                double y = base.y + 0.3 + t * 1.2;
                                spawn(player, new FlameParticle(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        int pts = 7;
                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double y = base.y + 0.4 + t * 1.6;
                            double sway = Math.sin(phase * 0.9 + t * 4.0) * 0.12;
                            double x = base.x + sway * right.x;
                            double z = base.z + sway * right.z;
                            spawn(player, new FlameParticle(new Vector3(x, y, z)));
                        }

                        burstRandom(player, ember, base.add(0, 1.8, 0),
                                0.4, 3, seed ^ 0x77A3L, 0.0, 0.4);
                    }
                }
            }

            // Static Shock
            case "static_shock" -> {
                var core = dust(120, 190, 255);
                var edge = dust(40, 80, 180);

                if (moving) {
                    if (fastTick) {
                        double f = 0.6;
                        double s = 0.45;
                        double yMid = base.y + 1.1;

                        Vector3[] corners = new Vector3[]{
                                new Vector3(base.x + fwd.x * f + right.x * s, yMid, base.z + fwd.z * f + right.z * s),
                                new Vector3(base.x + fwd.x * f - right.x * s, yMid, base.z + fwd.z * f - right.z * s),
                                new Vector3(base.x - fwd.x * f - right.x * s, yMid, base.z - fwd.z * f - right.z * s),
                                new Vector3(base.x - fwd.x * f + right.x * s, yMid, base.z - fwd.z * f + right.z * s)
                        };

                        double hs = 0.16;
                        for (Vector3 c : corners) {
                            spawn(player, core.at(c.add(-hs, 0, -hs)));
                            spawn(player, core.at(c.add(hs, 0, -hs)));
                            spawn(player, core.at(c.add(hs, 0, hs)));
                            spawn(player, core.at(c.add(-hs, 0, hs)));
                        }

                        int segs = 4;
                        for (int i = 0; i < 4; i++) {
                            int j = (i + 1) % 4;
                            Vector3 a = corners[i];
                            Vector3 b = corners[j];
                            for (int k = 0; k <= segs; k++) {
                                double t = k / (double) segs;
                                double jx = (Math.random() - 0.5) * 0.04;
                                double jz = (Math.random() - 0.5) * 0.04;
                                double x = a.x + (b.x - a.x) * t + jx;
                                double y = a.y;
                                double z = a.z + (b.z - a.z) * t + jz;
                                spawn(player, edge.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (medTick) {
                        int segs = 6;
                        double back = 1.6;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = t * back;
                            double sway = Math.sin(phase * 1.0 + t * 4.0) * 0.15;

                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;
                            double y = base.y + 0.06;

                            spawn(player, core.at(new Vector3(x, y, z)));

                            if (i % 2 == 0) {
                                spawn(player, edge.at(new Vector3(x + right.x * 0.15, y, z + right.z * 0.15)));
                                spawn(player, edge.at(new Vector3(x - right.x * 0.15, y, z - right.z * 0.15)));
                            }
                        }
                    }

                } else {
                    if (fastTick) {
                        double size = 0.85;
                        double yFeet = base.y + 0.04;

                        Vector3[] floor = new Vector3[]{
                                new Vector3(base.x + fwd.x * size + right.x * size, yFeet, base.z + fwd.z * size + right.z * size),
                                new Vector3(base.x + fwd.x * size - right.x * size, yFeet, base.z + fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size - right.x * size, yFeet, base.z - fwd.z * size - right.z * size),
                                new Vector3(base.x - fwd.x * size + right.x * size, yFeet, base.z - fwd.z * size + right.z * size)
                        };

                        int edgePts = 6;
                        for (int i = 0; i < 4; i++) {
                            int j = (i + 1) % 4;
                            Vector3 a = floor[i];
                            Vector3 b = floor[j];
                            for (int k = 0; k <= edgePts; k++) {
                                double t = k / (double) edgePts;
                                double x = a.x + (b.x - a.x) * t;
                                double z = a.z + (b.z - a.z) * t;

                                if ((k + i + currentTick) % 2 == 0) {
                                    spawn(player, core.at(new Vector3(x, yFeet, z)));
                                } else {
                                    spawn(player, edge.at(new Vector3(x, yFeet, z)));
                                }
                            }
                        }
                    }

                    if (medTick) {
                        double yMid = base.y + 1.0;
                        double size = 0.55;
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
                                double x = a.x + (b.x - a.x) * t;
                                double z = a.z + (b.z - a.z) * t;
                                double y = yMid + Math.sin(phase * 1.3 + t * 4.0 + i) * 0.03;
                                spawn(player, core.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if (slowTick) {
                        int spikes = 4;
                        int steps = 3;
                        double baseR = 0.35;
                        for (int i = 0; i < spikes; i++) {
                            double ang = phase * 0.4 + i * (TAU / spikes);
                            double dx = Math.cos(ang) * baseR;
                            double dz = Math.sin(ang) * baseR;

                            for (int s = 0; s <= steps; s++) {
                                double t = s / (double) steps;
                                double y = base.y + 0.2 + t * 0.9;
                                double x = base.x + dx * (1.0 + 0.1 * t);
                                double z = base.z + dz * (1.0 + 0.1 * t);

                                var fx = (s == steps) ? core : edge;
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

                if (moving) {
                    if (fastTick) {
                        int segs = 5;
                        double length = 1.6;
                        double y = base.y + 1.0;
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = t * length;
                            double sway = Math.sin(phase * 0.7 + t * 3.5) * 0.12;

                            double x = base.x - fwd.x * dist + right.x * sway;
                            double z = base.z - fwd.z * dist + right.z * sway;

                            spawn(player, sky.at(new Vector3(x, y, z)));
                            if (i == segs - 1) {
                                spawn(player, gold.at(new Vector3(x, y + 0.05, z)));
                            }
                        }
                    }

                    if ((currentTick % 15) == 0) {
                        int segs = 5;
                        double baseY = base.y + 1.1;
                        for (int side = -1; side <= 1; side += 2) {
                            for (int i = 0; i < segs; i++) {
                                double t = i / (double) (segs - 1);
                                double arch = Math.sin(t * Math.PI);
                                double spread = 0.35 + 0.45 * t;
                                double back   = 0.15 + 0.4 * t;

                                double x = base.x
                                        - fwd.x * back
                                        + right.x * (side * spread);
                                double z = base.z
                                        - fwd.z * back
                                        + right.z * (side * spread);
                                double y = baseY + arch * 0.4;

                                spawn(player, gold.at(new Vector3(x, y, z)));
                            }
                        }
                    }

                    if ((currentTick % 20) == 0) {
                        ring(player, white, base, 0.55, 8, phase * 0.8, 1.8);
                    }

                } else {
                    if (fastTick) {
                        double yFeet = base.y + 0.04;

                        ring(player, sky, base, 0.9, 18, phase * 0.4, 0.04);
                        ring(player, gold, base, 0.55, 12, -phase * 0.6, 0.04);

                        int rays = 4;
                        int steps = 3;
                        double stepLen = 0.23;
                        for (int i = 0; i < rays; i++) {
                            double ang = phase * 0.3 + i * (TAU / rays);
                            double dx = Math.cos(ang);
                            double dz = Math.sin(ang);
                            for (int s = 1; s <= steps; s++) {
                                double dist = stepLen * s;
                                double x = base.x + dx * dist;
                                double z = base.z + dz * dist;
                                spawn(player, white.at(new Vector3(x, yFeet, z)));
                            }
                        }
                    }

                    if ((currentTick % 18) == 0) {
                        int pts = 10;
                        double h = 1.6;
                        double r = 0.45;
                        for (int i = 0; i < pts; i++) {
                            double t = i / (double) (pts - 1);
                            double ang = phase * 0.9 + t * TAU * 1.2;
                            double x = base.x + Math.cos(ang) * (r + 0.05 * t);
                            double z = base.z + Math.sin(ang) * (r + 0.05 * t);
                            double y = base.y + 0.5 + t * h;

                            var fx = (i % 2 == 0) ? white : gold;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }

                    if ((currentTick % 24) == 0) {
                        double crownY = base.y + 1.9;
                        int stars = 6;
                        double r = 0.6;

                        for (int i = 0; i < stars; i++) {
                            double t = i / (double) stars;
                            double ang = phase * 1.0 + t * TAU;

                            double x = base.x + Math.cos(ang) * r;
                            double z = base.z + Math.sin(ang) * r;
                            double y = crownY + 0.03 * Math.sin(phase * 1.4 + t * 5.0);

                            spawn(player, white.at(new Vector3(x, y, z)));
                        }
                    }

                    if (medTick) {
                        double midY = base.y + 1.0;
                        int pts = 8;
                        double r = 0.45;
                        for (int i = 0; i < pts; i++) {
                            double a = phase * 0.8 + i * (TAU / pts);
                            double x = base.x + Math.cos(a) * r;
                            double z = base.z + Math.sin(a) * r;
                            double y = midY + 0.03 * Math.sin(phase * 1.3 + i);
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
                PFactory smoke = pos -> new SmokeParticle(pos);
                if (moving) {
                    int segs = 7;
                    double back = 1.4;
                    double side = 0.4;
                    for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
                        for (int i = 0; i < segs; i++) {
                            double t = i / (double) (segs - 1);
                            double dist = t * back;
                            double sway = Math.sin(phase * 0.6 + t * 3.0 + sideSign) * 0.1;
                            double cx = base.x - fwd.x * dist + right.x * (sideSign * side + sway);
                            double cz = base.z - fwd.z * dist + right.z * (sideSign * side + sway);
                            double cy = base.y + 0.8 + t * 0.3;
                            spawn(player, new SmokeParticle(new Vector3(cx, cy, cz)));
                        }
                    }
                } else {
                    if (fastTick) {
                        int columns = 4;
                        double radius = 0.9;
                        int heightSteps = 4;
                        for (int i = 0; i < columns; i++) {
                            double ang = phase * 0.3 + i * (TAU / columns);
                            double x = base.x + Math.cos(ang) * radius;
                            double z = base.z + Math.sin(ang) * radius;
                            for (int h = 0; h < heightSteps; h++) {
                                double t = h / (double) (heightSteps - 1);
                                double y = base.y + 0.4 + t * 1.4;
                                spawn(player, smoke.at(new Vector3(x, y, z)));
                            }
                        }
                    }
                    if (slowTick) {
                        spawn(player, new SmokeParticle(base.add(0, 1.8, 0)));
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
                var light = dust(245, 245, 255);
                var dark  = dust(180, 180, 190);

                // Tornade constante autour du joueur
                if (fastTick) {
                    int rings = 3;
                    int points = 10;
                    double heightStep = 0.45;

                    for (int r = 0; r < rings; r++) {
                        double y = base.y + 0.15 + r * heightStep;
                        double radius = 0.35 + r * 0.18;

                        for (int i = 0; i < points; i++) {
                            double t = i / (double) points;
                            double ang = phase * 0.6 + t * TAU + r * 0.7;

                            double x = base.x + Math.cos(ang) * radius;
                            double z = base.z + Math.sin(ang) * radius;

                            var fx = ((i + r + currentTick) % 2 == 0) ? light : dark;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }
                }

                // Petit nuage au sommet quand le joueur est statique
                if (!moving && slowTick) {
                    spawn(player, new SmokeParticle(base.add(0, 1.9, 0)));
                }
            }

            // 🩵 Cœur de Givre
            case "frost_heart" -> {
                var frost = dust(160, 220, 255);
                var deep  = dust(110, 180, 245);

                Position chest = base.add(0, 1.1, 0);

                // Pulsation glaciale autour du cœur
                if (fastTick) {
                    int pts = 8;
                    double baseR = 0.22;
                    double pulse = 0.06 * Math.sin(phase * 0.6);

                    for (int i = 0; i < pts; i++) {
                        double t = i / (double) pts;
                        double ang = phase * 0.7 + t * TAU;
                        double r = baseR + pulse;

                        double x = chest.x + Math.cos(ang) * r;
                        double z = chest.z + Math.sin(ang) * r;
                        double y = chest.y + 0.03 * Math.sin(phase * 1.2 + t * 4.0);

                        var fx = (i % 2 == 0) ? frost : deep;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                // Anneau glacé au sol quand le joueur ne bouge pas
                if (!moving && medTick) {
                    ring(player, frost, base, 0.7, 14, phase * 0.5, 0.04);
                }
            }

            // 🌑 Orbites du Néant
            case "void_orbits" -> {
                var core  = dust(90, 0, 120);
                var trail = dust(50, 0, 80);

                double baseY = base.y + 1.0;
                double radius = 0.75;

                // Trois orbes en orbite autour du joueur
                if (fastTick) {
                    int orbs = 3;
                    for (int i = 0; i < orbs; i++) {
                        double ang = phase * 0.15 + i * (TAU / orbs);
                        double x = base.x + Math.cos(ang) * radius;
                        double z = base.z + Math.sin(ang) * radius;
                        double y = baseY + Math.sin(phase * 0.5 + i) * 0.12;

                        Vector3 p = new Vector3(x, y, z);
                        spawn(player, core.at(p));

                        // petite traînée derrière
                        double tx = base.x + Math.cos(ang + 0.45) * (radius * 0.6);
                        double tz = base.z + Math.sin(ang + 0.45) * (radius * 0.6);
                        double ty = y - 0.05;
                        spawn(player, trail.at(new Vector3(tx, ty, tz)));
                    }
                }

                // Quand statique : anneau sombre plus serré
                if (!moving && medTick) {
                    ring(player, trail, base, 0.55, 12, -phase * 0.4, 0.04);
                }
            }

            // 🪶 Poussière d’Ombre
            case "shadow_dust" -> {
                var dark = dust(15, 15, 20);

                // En mouvement : poussière derrière les pas
                if (moving && fastTick) {
                    double side = 0.32;
                    double yOff = 0.03;

                    Vector3 lp = new Vector3(base.x - right.x * side, base.y + yOff, base.z - right.z * side);
                    Vector3 rp = new Vector3(base.x + right.x * side, base.y + yOff, base.z + right.z * side);

                    spawn(player, dark.at(lp));
                    spawn(player, dark.at(rp));

                    spawn(player, new SmokeParticle(base.add(0, 0.15, 0)));
                }

                // À l’arrêt : halo discret au sol
                if (!moving && medTick) {
                    int pts = 16;
                    double r = 0.7;
                    double y = base.y + 0.03;
                    for (int i = 0; i < pts; i++) {
                        double a = phase * 0.4 + i * (TAU / pts);
                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;
                        spawn(player, dark.at(new Vector3(x, y, z)));
                    }
                }
            }

            // 💚 Essence de la Vie
            case "life_essence" -> {
                var green = dust(60, 220, 80);
                var gold  = dust(255, 215, 80);

                Position mid = base.add(0, 1.0, 0);

                // Aura vivante autour du torse
                if (fastTick) {
                    int pts = 10;
                    double r = 0.55;
                    for (int i = 0; i < pts; i++) {
                        double t = i / (double) pts;
                        double a = phase * 0.7 + t * TAU;
                        double x = mid.x + Math.cos(a) * r;
                        double z = mid.z + Math.sin(a) * r;
                        double y = mid.y + 0.06 * Math.sin(phase * 1.1 + t * 5.0);

                        var fx = (i % 2 == 0) ? green : gold;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                // En statique : colonne ascendante douce
                if (!moving && slowTick) {
                    int steps = 5;
                    double h = 1.6;
                    for (int i = 0; i < steps; i++) {
                        double t = i / (double) (steps - 1);
                        double y = base.y + 0.4 + t * h;
                        var fx = (i % 2 == 0) ? green : gold;
                        spawn(player, fx.at(new Vector3(base.x, y, base.z)));
                    }
                }
            }

            // 🩷 Pluie d’Amour
            case "love_rain" -> {
                var pink  = dust(255, 160, 210);
                var deep  = dust(230, 90, 160);

                Random rng = new Random(seed);

                // Gouttes qui tombent autour du joueur
                if (fastTick) {
                    int drops = 5;
                    for (int i = 0; i < drops; i++) {
                        double a = rng.nextDouble() * TAU;
                        double r = 0.2 + rng.nextDouble() * 0.8;
                        double x = base.x + Math.cos(a) * r;
                        double z = base.z + Math.sin(a) * r;
                        double y = base.y + 1.6 + rng.nextDouble() * 0.6;

                        var fx = rng.nextBoolean() ? pink : deep;
                        spawn(player, fx.at(new Vector3(x, y, z)));
                    }
                }

                // À l’arrêt : petit cercle “puddle” au sol
                if (!moving && medTick) {
                    ring(player, pink, base, 0.65, 14, phase * 0.5, 0.03);
                }
            }

            // 🔮 Faille Dimensionnelle
            case "dimensional_rift" -> {
                var core   = dust(180, 0, 230);
                var fringe = dust(120, 0, 170);

                Position c = base.add(0, 0.5, 0);

                // Double spirale violette autour du joueur
                if (fastTick) {
                    dualSpiral(player, core, c, 0.55, 1.5, 18, phase * 0.9);
                }

                // Anneau instable au sol
                if (medTick) {
                    ring(player, fringe, base, 0.85, 16, -phase * 0.6, 0.04);
                }
            }

            // ☄️ Comète
            case "comet" -> {
                var white  = dust(245, 245, 255);
                var violet = dust(190, 160, 255);

                Position center = base.add(0, 1.2, 0);

                // Trois comètes qui traversent le joueur
                if (fastTick) {
                    int streaks = 3;
                    for (int i = 0; i < streaks; i++) {
                        double ang = phase * 0.6 + i * (TAU / streaks);
                        double len = 1.4;

                        double dx = Math.cos(ang);
                        double dz = Math.sin(ang);

                        int steps = 4;
                        for (int s = 0; s < steps; s++) {
                            double t = s / (double) (steps - 1);
                            double x = center.x + dx * (len * (t - 0.5));
                            double z = center.z + dz * (len * (t - 0.5));
                            double y = center.y + 0.12 * (t - 0.5);

                            var fx = (s == steps - 1) ? white : violet;
                            spawn(player, fx.at(new Vector3(x, y, z)));
                        }
                    }
                }

                // En statique : petite spirale verticale
                if (!moving && medTick) {
                    spiral(player, violet, base.add(0, 0.4, 0), 0.4, 1.4, 10, phase * 0.9);
                }
            }

            // 🌊 Éclaboussures Marines
            case "marine_splash" -> {
                var water1 = dust(70, 160, 255);
                var water2 = dust(40, 120, 230);

                // En mouvement : splash aux pieds
                if (moving && fastTick) {
                    double side = 0.35;
                    double yOff = 0.03;

                    Vector3 lp = new Vector3(base.x - right.x * side, base.y + yOff, base.z - right.z * side);
                    Vector3 rp = new Vector3(base.x + right.x * side, base.y + yOff, base.z + right.z * side);

                    spawn(player, water1.at(lp));
                    spawn(player, water2.at(rp));
                }

                // Statique : vague circulaire très légère
                if (!moving && medTick) {
                    ring(player, water1, base, 0.75, 14, phase * 0.5, 0.03);
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
