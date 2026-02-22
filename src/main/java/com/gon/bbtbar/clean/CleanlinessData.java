package com.gon.bbtbar.clean;

import net.minecraft.world.entity.player.Player;

public final class CleanlinessData {
    private CleanlinessData() {}

    public static final float MAX = 160.0F;
    private static final String KEY = "bbtbar_cleanliness";

    public static float get(Player p) {
        return p.getPersistentData().getFloat(KEY);
    }

    public static void set(Player p, float v) {
        float clamped = Math.max(0.0F, Math.min(MAX, v));
        p.getPersistentData().putFloat(KEY, clamped);
    }

    public static void fill(Player p) {
        set(p, MAX);
    }
}
