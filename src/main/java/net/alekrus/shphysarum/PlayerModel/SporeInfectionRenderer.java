package net.alekrus.shphysarum.PlayerModel;


import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class SporeInfectionRenderer extends GeoObjectRenderer<SporeInfectionAnimatable> {
    public SporeInfectionRenderer() {
        super(new SporeInfectionModel());
    }
}
