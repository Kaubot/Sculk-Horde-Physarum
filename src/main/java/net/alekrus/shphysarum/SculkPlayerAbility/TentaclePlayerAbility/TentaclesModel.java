package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TentaclesModel extends GeoModel<TentaclesAnimatable> {

    @Override
    public ResourceLocation getModelResource(TentaclesAnimatable object) {
        
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "geo/tentacles.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TentaclesAnimatable object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/entity/back.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TentaclesAnimatable object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "animations/tentacles.animation.json");
    }
}