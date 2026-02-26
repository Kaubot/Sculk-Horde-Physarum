package net.alekrus.shphysarum.Block;


import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SculkPortalModel extends GeoModel<SculkPortalEntity> {

    @Override
    public ResourceLocation getModelResource(SculkPortalEntity object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "geo/portal.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkPortalEntity object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/entity/portal.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SculkPortalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "animations/portal_anim.json");
    }
}