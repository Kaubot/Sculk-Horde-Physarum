package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SculkBeaconItemModel extends GeoModel<SculkBeaconItem> {
    @Override
    public ResourceLocation getModelResource(SculkBeaconItem object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "geo/beacon_sculk.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkBeaconItem object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/block/texture_beacon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SculkBeaconItem object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "animations/beacon_anim.json");
    }
}
