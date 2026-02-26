package net.alekrus.shphysarum.Block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkPortalRenderer extends GeoEntityRenderer<SculkPortalEntity> {

    public SculkPortalRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkPortalModel());
        this.shadowRadius = 0.0f; 
    }

    
    @Override
    public void preRender(PoseStack poseStack, SculkPortalEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        
        poseStack.scale(3.5F, 3.5F, 3.5F);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    
    @Override
    public RenderType getRenderType(SculkPortalEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        
        return RenderType.entityTranslucentCull(texture);
    }
}