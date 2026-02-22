package com.gon.bbtbar.client;

import com.gon.bbtbar.BBTBar;
import com.gon.bbtbar.clean.CleanlinessData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CleanlinessOverlay implements IGuiOverlay {

    private static final ResourceLocation FULL =
            new ResourceLocation(BBTBar.MODID, "textures/gui/cleanbar/full.png");   // 80x9
    private static final ResourceLocation EMPTY =
            new ResourceLocation(BBTBar.MODID, "textures/gui/cleanbar/empty.png");  // 80x9
    private static final ResourceLocation ICON =
            new ResourceLocation(BBTBar.MODID, "textures/gui/cleanbar/icon_drop.png"); // 9x9

    @Override
    public void render(ForgeGui gui, GuiGraphics gg, float partialTick, int width, int height) {
        if (!gui.shouldDrawSurvivalElements()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        // ===== POSITION =====
        // above FOOD bar (right side)
        int x = width / 2 + 91 - 80; // right HUD edge - bar width
        int y = height - 39 - 10;    // one row above hunger

        float value = ClientState.cleanliness;
        float max = CleanlinessData.MAX;

        float prop = Math.max(0.0F, Math.min(1.0F, value / max));
        int w = (int) Math.ceil(80.0F * prop);

        // bar
        gg.blit(FULL, x, y, 0, 0, w, 9, 80, 9);
        gg.blit(EMPTY, x + w, y, w, 0, 80 - w, 9, 80, 9);

        // icon (left of bar)
        gg.blit(ICON, x - 12, y, 0, 0, 9, 9, 9, 9);
    }
}
