package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.minecraft.client.Minecraft;
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

public class TentaclesLayerRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final TentaclesRenderer renderer;
    private final Map<UUID, TentaclesAnimatable> animatables = new HashMap<>();

    public TentaclesLayerRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.renderer = new TentaclesRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;
        if (!InfectionHandler.isClientInfected(player)) return;

        boolean isLocalPlayer = (player == Minecraft.getInstance().player);
        boolean isActive;
        boolean isBlocking;

        TentaclesAnimatable animatable = animatables.computeIfAbsent(player.getUUID(), id -> new TentaclesAnimatable());

        if (isLocalPlayer) {
            
            if (!ClientSkillData.hasSkill("adaptive_body_structuring")) return;

            isActive = ClientGravemindState.tentaclesActive;
            isBlocking = ClientGravemindState.isBlocking;
            animatable.lastAttackTime = ClientGravemindState.lastAttackTime;

        } else {
            
            boolean hasSkill = player.getPersistentData().getBoolean("sh_hasTentacleSkill");
            if (!hasSkill) return;

            
            isActive = player.getPersistentData().getBoolean("sh_tentaclesActive");
            isBlocking = player.getPersistentData().getBoolean("sh_isBlocking");

            long serverAttackTime = player.getPersistentData().getLong("sh_lastAttackTime");
            if (serverAttackTime != animatable.serverLastAttackTime && serverAttackTime != 0) {
                animatable.serverLastAttackTime = serverAttackTime;
                animatable.lastAttackTime = System.currentTimeMillis();
            }
        }

        
        animatable.isActive = isActive;
        animatable.isBlocking = isBlocking;

        try {
            poseStack.pushPose();
            this.getParentModel().body.translateAndRotate(poseStack);
            
            poseStack.translate(0.47, 2.0D, -0.14);
            poseStack.scale(1.0F, 1.0F, 1.0F);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

            ResourceLocation texture = renderer.getTextureLocation(animatable);
            RenderType renderType = RenderType.entityCutoutNoCull(texture);

            renderer.render(poseStack, animatable, buffer, renderType, buffer.getBuffer(renderType), packedLight);
            poseStack.popPose();
        } catch (Exception ignored) {}
    }
}
