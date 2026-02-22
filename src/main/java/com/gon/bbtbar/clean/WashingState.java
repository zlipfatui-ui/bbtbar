package com.gon.bbtbar.clean;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side per-player washing progress tracker.
 * Tracks how many ticks a player has been continuously washing (in water + holding soap).
 */
public final class WashingState {
    private WashingState() {}

    /** Ticks of continuous washing required to complete (30 seconds = 600 ticks). */
    public static final int REQUIRED_TICKS = 600;

    /** How often to play the washing sound (every 2 seconds = 40 ticks). */
    public static final int SOUND_INTERVAL_TICKS = 40;

    /** How often to send actionbar progress updates (every 1 second = 20 ticks). */
    public static final int PROGRESS_INTERVAL_TICKS = 20;

    private static final Map<UUID, Integer> washTicks = new ConcurrentHashMap<>();

    public static int getTicks(ServerPlayer player) {
        return washTicks.getOrDefault(player.getUUID(), 0);
    }

    public static void increment(ServerPlayer player) {
        washTicks.merge(player.getUUID(), 1, Integer::sum);
    }

    public static void reset(ServerPlayer player) {
        washTicks.remove(player.getUUID());
    }

    public static boolean isWashing(ServerPlayer player) {
        return washTicks.containsKey(player.getUUID());
    }

    public static void cleanup(UUID uuid) {
        washTicks.remove(uuid);
    }
}
