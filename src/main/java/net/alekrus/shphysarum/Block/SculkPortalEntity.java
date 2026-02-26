package net.alekrus.shphysarum.Block;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkPortalEntity extends Mob implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SculkPortalEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;        
        this.setNoGravity(true);      
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    public void checkDespawn() {
        
    }

    @Override
    protected void registerGoals() {
        
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0, 0, 0); 

        if (!this.level().isClientSide) {
            if (this.tickCount >= 50) {
                this.discard();
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.tickCount < 40) {
                
                state.getController().setAnimationSpeed(0.4D);
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("open"));
            } else {
                
                state.getController().setAnimationSpeed(0.6D);
                return state.setAndContinue(RawAnimation.begin().thenPlay("close"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}