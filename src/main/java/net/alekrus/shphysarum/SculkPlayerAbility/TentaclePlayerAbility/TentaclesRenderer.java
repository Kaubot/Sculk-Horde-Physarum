package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class TentaclesRenderer extends GeoObjectRenderer<TentaclesAnimatable> {
    public TentaclesRenderer() {
        super(new TentaclesModel());
    }
}