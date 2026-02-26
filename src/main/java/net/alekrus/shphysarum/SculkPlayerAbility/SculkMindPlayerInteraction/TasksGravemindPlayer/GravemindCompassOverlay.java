package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class GravemindCompassOverlay {

    private static final ResourceLocation COMPASS_TEXTURE = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/point.png");
    private static final int SIZE = 32;

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!InfectionHandler.isClientInfected(mc.player)) return;

        GravemindTask task = TaskManager.getCurrentTask();
        if (task == null || task.isComplete()) return;


        boolean isMovementTask = task.type == GravemindTask.Type.INFECT_AREA;

        if (!isMovementTask || task.targetLocation == null) return;


        BlockPos target = task.targetLocation;
        Vec3 playerPos = mc.player.position();
        double dx = target.getX() - playerPos.x;
        double dz = target.getZ() - playerPos.z;
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        float playerYaw = mc.player.getYRot();
        float rotation = targetYaw - playerYaw;


        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x = (width / 2) - 91 - 25 - SIZE;
        int y = height - 28 + (22 - SIZE) / 2;

        GuiGraphics graphics = event.getGuiGraphics();


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, COMPASS_TEXTURE);

        graphics.pose().pushPose();
        graphics.pose().translate(x + SIZE / 2.0f, y + SIZE / 2.0f, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        graphics.pose().translate(-SIZE / 2.0f, -SIZE / 2.0f, 0);
        graphics.blit(COMPASS_TEXTURE, 0, 0, 0, 0, SIZE, SIZE, SIZE, SIZE);
        graphics.pose().popPose();


        int distance = (int) Math.sqrt(playerPos.distanceToSqr(target.getX(), playerPos.y, target.getZ()));
        String distText = distance + "m";
        float scale = 0.7f;
        graphics.pose().pushPose();
        graphics.pose().translate(x + (SIZE / 2.0f), y + SIZE + 2, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        int textWidth = mc.font.width(distText);
        graphics.drawString(mc.font, distText, -textWidth / 2, 0, 0x55FFFF, true);
        graphics.pose().popPose();

        RenderSystem.disableBlend();
    }
}