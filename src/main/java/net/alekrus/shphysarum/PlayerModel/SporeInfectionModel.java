package net.alekrus.shphysarum.PlayerModel;


import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SporeInfectionModel extends GeoModel<SporeInfectionAnimatable> {

    @Override
    public ResourceLocation getModelResource(SporeInfectionAnimatable object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "geo/sporovik.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SporeInfectionAnimatable object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/entity/sporovik.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SporeInfectionAnimatable object) {
        return ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "animations/sporovik.animation.json");
    }
}
