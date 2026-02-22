package com.gon.bbtbar.clean;

import com.gon.bbtbar.BBTBar;
import com.gon.bbtbar.net.BBTBarNetwork;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BBTBar.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CleanlinessEvents {

    private static final ResourceLocation SOAP_ID =
            new ResourceLocation("supplementaries", "soap");

    private static final int STINK_INTERVAL_TICKS = 20;

    // ── Right-click soap: no longer instant, just cancel default interaction ──

    @SubscribeEvent
    public static void onRightClickSoap(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;

        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

        var stack = event.getItemStack();
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !itemId.equals(SOAP_ID)) return;

        // Cancel vanilla interaction so soap doesn't do its default behavior
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.PASS);
    }

    // ── Login / Respawn sync ──

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            BBTBarNetwork.sendCleanlinessTo(sp, CleanlinessData.get(sp));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            BBTBarNetwork.sendCleanlinessTo(sp, CleanlinessData.get(sp));
        }
    }

    // ── Logout cleanup ──

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WashingState.cleanup(event.getEntity().getUUID());
    }

    // ── Main tick: stink particles + washing progress ──

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var player = event.player;
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // ── Stink particles (unchanged) ──
        if (player.tickCount % STINK_INTERVAL_TICKS == 0 && CleanlinessData.get(player) <= 0.0F) {
            level.sendParticles(
                    ParticleTypes.ENTITY_EFFECT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    3,
                    0.30, 0.40, 0.30,
                    0.0
            );
        }

        // ── Washing mechanic ──
        tickWashing(sp, level);
    }

    private static void tickWashing(ServerPlayer sp, ServerLevel level) {
        boolean holdingSoap = isHoldingSoap(sp);
        boolean inWater = sp.isInWaterOrBubble();

        // If not holding soap in water, reset progress
        if (!holdingSoap || !inWater) {
            if (WashingState.isWashing(sp)) {
                WashingState.reset(sp);
            }
            return;
        }

        // Already at full cleanliness — no need to wash
        if (CleanlinessData.get(sp) >= CleanlinessData.MAX) return;

        // Advance washing timer
        WashingState.increment(sp);
        int ticks = WashingState.getTicks(sp);

        // Actionbar progress (every 1 second)
        if (ticks % WashingState.PROGRESS_INTERVAL_TICKS == 0) {
            int seconds = ticks / 20;
            int total = WashingState.REQUIRED_TICKS / 20;
            sp.displayClientMessage(
                    Component.literal("กำลังอาบน้ำ : " + seconds + "/" + total + "s"),
                    true  // actionbar overlay
            );
        }

        // Washing sound (every 2 seconds)
        if (ticks % WashingState.SOUND_INTERVAL_TICKS == 0) {
            var s = pickWashSound(level);
            level.playSound(
                    null,
                    sp.getX(), sp.getY(), sp.getZ(),
                    s,
                    SoundSource.PLAYERS,
                    0.35F,
                    0.9F + level.getRandom().nextFloat() * 0.2F
            );
        }


        // Completion
        if (ticks >= WashingState.REQUIRED_TICKS) {
            completeWashing(sp);
        }
    }

    private static void completeWashing(ServerPlayer sp) {
        // Set cleanliness to full
        CleanlinessData.fill(sp);
        BBTBarNetwork.sendCleanlinessTo(sp, CleanlinessData.get(sp));

        // Consume 1 soap from main hand (unless creative)
        if (!sp.getAbilities().instabuild) {
            var stack = sp.getMainHandItem();
            var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId != null && itemId.equals(SOAP_ID)) {
                stack.shrink(1);
            }
        }

        // Completion sound
        sp.level().playSound(
                null,
                sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS,
                0.6F,
                1.5F
        );

        // Completion message
        sp.displayClientMessage(
                Component.literal("อาบน้ำเสร็จแล้ว !"),
                true
        );

        // Reset washing state
        WashingState.reset(sp);
    }

    private static boolean isHoldingSoap(ServerPlayer sp) {
        var stack = sp.getMainHandItem();
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && itemId.equals(SOAP_ID);
    }

    private static SoundEvent pickWashSound(ServerLevel level) {
        int r = level.getRandom().nextInt(3);
        return switch (r) {
            case 0 -> SoundEvents.PLAYER_SWIM;
            case 1 -> SoundEvents.GENERIC_SWIM;
            default -> SoundEvents.PLAYER_SPLASH;
        };
    }
}
