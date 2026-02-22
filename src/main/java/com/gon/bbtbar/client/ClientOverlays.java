package com.gon.bbtbar.client;

import com.gon.bbtbar.BBTBar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BBTBar.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientOverlays {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_HEALTH.id(),
                "bbtbar_health",
                new HealthBarOverlay());

        event.registerAbove(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.FOOD_LEVEL.id(),
                "bbtbar_cleanliness",
                new CleanlinessOverlay());
    }
}
