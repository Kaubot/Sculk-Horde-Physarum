package net.alekrus.shphysarum.CustomGui.HudPlayer;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HudEvents {
    private static final ResourceLocation XP_EMPTY = ResourceLocation.fromNamespaceAndPath("shphysarum", "textures/gui/exp_bar_empty.png");
    private static final ResourceLocation XP_FULL = ResourceLocation.fromNamespaceAndPath("shphysarum", "textures/gui/exp_bar_full.png");

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;


        if (!InfectionHandler.isClientInfected(player)) {
            return;
        }


        if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
            renderDetailedExperienceBar(event.getGuiGraphics());
        }

        if (event.getOverlay().id().getPath().equals("food_level")) {
            event.setCanceled(true);
        }
    }

    private static void renderDetailedExperienceBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator()) return;

        int sourceTextureWidth = 259;
        int sourceTextureHeight = 16;
        int screenBarWidth = 218;
        int screenBarHeight = 13;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int x = (screenWidth - screenBarWidth) / 2;
        int y = screenHeight - 32 - 2;


        guiGraphics.blit(XP_EMPTY, x, y, screenBarWidth, screenBarHeight, 0, 0, sourceTextureWidth, sourceTextureHeight, sourceTextureWidth, sourceTextureHeight);


        if (player.experienceProgress > 0) {
            int progressSourceWidth = (int) (player.experienceProgress * sourceTextureWidth);
            int progressScreenWidth = (int) (player.experienceProgress * screenBarWidth);
            guiGraphics.blit(XP_FULL, x, y, progressScreenWidth, screenBarHeight, 0, 0, progressSourceWidth, sourceTextureHeight, sourceTextureWidth, sourceTextureHeight);
        }


        if (player.experienceLevel > 0) {
            String levelStr = Integer.toString(player.experienceLevel);
            Font font = mc.font;
            int textX = (screenWidth - font.width(levelStr)) / 2;
            int textY = screenHeight - 54;
            guiGraphics.drawString(font, levelStr, textX, textY, 0x20ACDF, true);
        }
    }
}
