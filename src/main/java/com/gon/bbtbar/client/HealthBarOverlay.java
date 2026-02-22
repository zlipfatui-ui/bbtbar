package com.gon.bbtbar.client;

import com.gon.bbtbar.BBTBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class HealthBarOverlay implements IGuiOverlay {

    private static final ResourceLocation FULL = rl("textures/gui/healthbars/full.png");
    private static final ResourceLocation WITHER = rl("textures/gui/healthbars/wither.png");
    private static final ResourceLocation POISON = rl("textures/gui/healthbars/poison.png");
    private static final ResourceLocation FROZEN = rl("textures/gui/healthbars/frozen.png");
    private static final ResourceLocation INTERMEDIATE = rl("textures/gui/healthbars/intermediate.png");
    private static final ResourceLocation EMPTY = rl("textures/gui/healthbars/empty.png");
    private static final ResourceLocation ABSORPTION = rl("textures/gui/healthbars/absorption.png");
    private static final ResourceLocation GUI_ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");

    private ResourceLocation currentBar = FULL;
    private float intermediateHealth = 0.0F;

    private static ResourceLocation rl(String path) { return new ResourceLocation(BBTBar.MODID, path); }

    @Override
    public void render(ForgeGui gui, GuiGraphics gg, float partialTick, int width, int height) {
        if (!gui.shouldDrawSurvivalElements()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        int x = width / 2 - 91;
        int y = height - 39;

        updateBarTextures(player);

        Font font = mc.font;
        renderHealthBar(gg, partialTick, x, y, player);
        renderHealthValue(font, gg, x, y, player);

        if (player.getAbsorptionAmount() > 0.0F) {
            renderAbsorptionBar(gg, x, y, player);
            renderAbsorptionValue(font, gg, x, y, player);
        }
    }

    private void updateBarTextures(Player player) {
        if (player.hasEffect(MobEffects.WITHER)) currentBar = WITHER;
        else if (player.hasEffect(MobEffects.POISON)) currentBar = POISON;
        else if (player.isFullyFrozen()) currentBar = FROZEN;
        else currentBar = FULL;
    }

    private void renderHealthValue(Font font, GuiGraphics gg, int x, int y, Player player) {
        float health = player.getHealth();

        // Show only current HP, no max (e.g. "20" instead of "20/20")
        double shown = Math.ceil(health * 10.0) / 10.0;
        String text = String.valueOf(shown).replace(".0", "");

        // Scale down for a smaller, cleaner look inside the bar
        float scale = 0.75F;
        int textW = (int) (font.width(text) * scale);
        int textH = (int) (font.lineHeight * scale);

        // Center inside the 80×9 health bar area
        int centerX = (int) ((x + 40 - textW / 2.0F) / scale);
        int centerY = (int) ((y + (9 - textH) / 2.0F) / scale);

        var pose = gg.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);
        // White text, no drop-shadow for clean look inside the bar
        gg.drawString(font, text, centerX, centerY, 0xFFFFFF, false);
        pose.popPose();
    }

    private void renderHealthBar(GuiGraphics gg, float partialTick, int x, int y, Player player) {
        float health = player.getHealth();
        float max = player.getMaxHealth();

        float hpProp;
        float interProp;

        if (health < intermediateHealth) {
            hpProp = health / max;
            interProp = (intermediateHealth - health) / max;
        } else {
            hpProp = intermediateHealth / max;
            interProp = 0.0F;
        }

        hpProp = Math.min(1.0F, hpProp);
        if (hpProp + interProp > 1.0F) interProp = 1.0F - hpProp;

        int hpW = (int) Math.ceil(80.0F * hpProp);
        int interW = (int) Math.ceil(80.0F * interProp);

        gg.blit(currentBar, x, y, 0, 0, hpW, 9, 80, 9);
        gg.blit(INTERMEDIATE, x + hpW, y, hpW, 0, interW, 9, 80, 9);
        gg.blit(EMPTY, x + hpW + interW, y, hpW + interW, 0, 80 - hpW - interW, 9, 80, 9);

        intermediateHealth = (float) (intermediateHealth + (health - intermediateHealth) * partialTick * 0.08);
        if (Math.abs(health - intermediateHealth) <= 0.25F) intermediateHealth = health;
    }

    private void renderAbsorptionValue(Font font, GuiGraphics gg, int x, int y, Player player) {
        double absorption = Math.ceil(player.getAbsorptionAmount());
        String text = String.valueOf(absorption / 2.0).replace(".0", "");

        gg.drawString(font, text, x - font.width(text) - 16, y - 9, 0xFFFF00, false);

        gg.blit(GUI_ICONS, x - 16, y - 10, 16, 0, 9, 9, 256, 256);
        gg.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        gg.blit(GUI_ICONS, x - 16, y - 10, 160, 0, 9, 9, 256, 256);
        gg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderAbsorptionBar(GuiGraphics gg, int x, int y, Player player) {
        float absorption = player.getAbsorptionAmount();
        float max = player.getMaxHealth();

        float prop = Math.min(1.0F, absorption / max);
        int w = (int) Math.ceil(80.0F * prop);

        gg.blit(ABSORPTION, x, y - 10, 0, 0, w, 9, 80, 9);
        gg.blit(EMPTY, x + w, y - 10, w, 0, 80 - w, 9, 80, 9);
    }
}
