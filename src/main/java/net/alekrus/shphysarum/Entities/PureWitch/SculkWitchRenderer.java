package net.alekrus.shphysarum.Entities.PureWitch;

import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;


public class SculkWitchRenderer extends MobRenderer<SculkWitchEntity, WitchModel<SculkWitchEntity>> {

    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("shphysarum", "textures/entity/purewitch.png");

    public SculkWitchRenderer(EntityRendererProvider.Context context) {
        
        super(context, new WitchModel<>(context.bakeLayer(ModelLayers.WITCH)), 0.5f);

        
        this.addLayer(new WitchItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SculkWitchEntity entity) {
        
        if (entity.tickCount % 100 == 0) {

        }
        return TEXTURE;
    }
}
