package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TentaclesAnimatable implements GeoAnimatable {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public boolean isActive = false;
    public boolean isBlocking = false;
    public long lastAttackTime = 0;
    public long serverLastAttackTime = 0;

    private boolean wasActive = false;
    private long showStartTime = 0;

    private static final RawAnimation SHOW = RawAnimation.begin().thenPlay("show").thenLoop("idle");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    
    private static final RawAnimation HIDE = RawAnimation.begin().thenPlayAndHold("hide");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("atack1").thenLoop("idle");
    private static final RawAnimation BLOCK = RawAnimation.begin().thenPlayAndHold("block");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tentacle_controller", 2, event -> {

            long currentTime = System.currentTimeMillis();

            
            if (!this.isActive) {
                this.wasActive = false;
                event.getController().setAnimationSpeed(1.0D);
                return event.setAndContinue(HIDE);
            }

            
            if (!this.wasActive) {
                this.wasActive = true;
                this.showStartTime = currentTime;
            }

            
            if (currentTime - this.showStartTime < 1200) {
                event.getController().setAnimationSpeed(1.0);
                return event.setAndContinue(SHOW);
            }

            
            if (currentTime - this.lastAttackTime < 500) {
                event.getController().setAnimationSpeed(1.2D);
                return event.setAndContinue(ATTACK);
            }

            
            if (this.isBlocking) {
                event.getController().setAnimationSpeed(1.0D);
                return event.setAndContinue(BLOCK);
            }

            
            event.getController().setAnimationSpeed(1.0D);
            return event.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return System.currentTimeMillis() / 50.0;
    }
}
