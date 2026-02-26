package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
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
public class TentacleBlockOverlay {

    
    private static final ResourceLocation RESISTANCE_ICON = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/resistance.png");

    
    @SubscribeEvent
    public static void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        
        if (ClientGravemindState.isBlocking) {

            
            event.setCanceled(true);

            
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            GuiGraphics graphics = event.getGuiGraphics();

            
            int iconSize = 24;

            
            int x = (width - iconSize) / 2;
            int y = (height - iconSize) / 2;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.85F);

            
            graphics.blit(RESISTANCE_ICON, x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);

            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }
}
