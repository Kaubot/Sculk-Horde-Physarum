package net.alekrus.shphysarum.Block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;


import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SculkBeaconRenderer implements BlockEntityRenderer<SculkBeaconBlockEntity> {

    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");

    
    private final GeoBlockRenderer<SculkBeaconBlockEntity> geoRenderer;

    public SculkBeaconRenderer(BlockEntityRendererProvider.Context context) {
        
        this.geoRenderer = new GeoBlockRenderer<>(new SculkBeaconModel());
    }

    @Override
    public void render(SculkBeaconBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        
        int actualLight;

        if (pBlockEntity.isRaidActive()) {
            
            actualLight = 15728880;
        } else {
            
            actualLight = net.minecraft.client.renderer.LevelRenderer.getLightColor(pBlockEntity.getLevel(), pBlockEntity.getBlockPos().above());
        }

        
        this.geoRenderer.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, actualLight, pPackedOverlay);

        
        if (pBlockEntity.isRaidActive()) {
            long gameTime = pBlockEntity.getLevel().getGameTime();
            BeaconRenderer.renderBeaconBeam(pPoseStack, pBufferSource, BEAM_LOCATION, pPartialTick, 1.0f, gameTime, 0, 256, new float[]{0.0f, 1.0f, 1.0f}, 0.2f, 0.25f);
            renderBoundary(pPoseStack, pBufferSource);
        }
    }


    private void renderBoundary(PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float r = 30.0f;
        float y = 0.5f;

        float red = 0.0f;
        float green = 0.8f;
        float blue = 0.9f;
        float alpha = 1.0f;

        
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        buffer.vertex(matrix, -r, y, -r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y, -r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();

        buffer.vertex(matrix, r, y, -r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y, r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();

        buffer.vertex(matrix, r, y, r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, -r, y, r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();

        buffer.vertex(matrix, -r, y, r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, -r, y, -r).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();

        float y2 = 2.5f;
        buffer.vertex(matrix, -r, y2, -r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y2, -r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y2, -r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y2, r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, r, y2, r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, -r, y2, r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, -r, y2, r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, -r, y2, -r).color(red, green, blue, 0.5f).normal(0, 1, 0).endVertex();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(SculkBeaconBlockEntity pBlockEntity) {
        return true;
    }
}
