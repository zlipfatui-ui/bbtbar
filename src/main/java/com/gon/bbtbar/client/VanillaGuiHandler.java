package com.gon.bbtbar.client;

import com.gon.bbtbar.BBTBar;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BBTBar.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class VanillaGuiHandler {

    @SubscribeEvent
    public static void disableVanillaHealth(RenderGuiOverlayEvent.Pre event) {
        if (!event.isCanceled() && event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ForgeGui gui = (ForgeGui) mc.gui;
            if (!gui.shouldDrawSurvivalElements()) return;
            if (mc.options.hideGui) return;

            gui.leftHeight += 10;
            if (mc.player.getAbsorptionAmount() > 0.0F) gui.leftHeight += 10;

            event.setCanceled(true);
        }
    }
}
