package net.alekrus.shphysarum.CustomGui.HudShowBiomass;

import com.github.sculkhorde.core.ModSavedData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class InfectedInfoHud {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/interfacebiomass.png");

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!InfectionHandler.isClientInfected(mc.player)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        
        int biomass = 0;
        try {
            if (ModSavedData.getSaveData() != null) {
                biomass = ModSavedData.getSaveData().getSculkAccumulatedMass();
            }
        } catch (Exception ignored) {}

        String biomassText = "Biomass: " + biomass;
        int texWidth = 115;
        int texHeight = 25;
        int x = 2;
        int y = screenHeight - 40;

        
        
        

        
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        
        RenderSystem.disableDepthTest();

        
        RenderSystem.setShaderTexture(0, TEXTURE);

        

        
        graphics.blit(TEXTURE, x, y, 0, 0, texWidth, texHeight, texWidth, texHeight);

        
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        
        int textY = y + (texHeight - 8) / 2;
        graphics.drawString(mc.font, biomassText, x + 10, textY, 0x00AAAA, false);

        
        RenderSystem.enableDepthTest();
    }
}
