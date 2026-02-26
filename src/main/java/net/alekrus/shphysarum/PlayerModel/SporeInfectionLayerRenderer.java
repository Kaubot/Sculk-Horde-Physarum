package net.alekrus.shphysarum.PlayerModel;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SporeInfectionLayerRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final SporeInfectionRenderer renderer;
    private final Map<UUID, SporeInfectionAnimatable> sporeAnimatables = new HashMap<>();

    public SporeInfectionLayerRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.renderer = new SporeInfectionRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;

        
        
        if (!InfectionHandler.isClientInfected(player)) return;

        SporeInfectionAnimatable currentAnimatable = sporeAnimatables.computeIfAbsent(player.getUUID(), id -> new SporeInfectionAnimatable());

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(260.0F));
        poseStack.translate(-0.2D, -0.3D, -0.48D);
        poseStack.scale(1.0F, 1.0F, 1.0F);

        ResourceLocation texture = renderer.getTextureLocation(currentAnimatable);
        RenderType renderType = RenderType.entityCutoutNoCull(texture);

        renderer.render(poseStack, currentAnimatable, buffer, renderType, buffer.getBuffer(renderType), packedLight);
        poseStack.popPose();
    }
}
