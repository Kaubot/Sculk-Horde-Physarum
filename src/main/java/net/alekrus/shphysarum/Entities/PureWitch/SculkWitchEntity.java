package net.alekrus.shphysarum.Entities.PureWitch;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SculkWitchEntity extends Witch {

    public SculkWitchEntity(EntityType<? extends Witch> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Witch.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!this.isDrinkingPotion()) {


            EntityType<?> flaskType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "purification_flask_projectile"));

            if (flaskType == null) {
                super.performRangedAttack(target, distanceFactor);
                return;
            }


            Entity flaskEntity = flaskType.create(this.level());

            if (flaskEntity instanceof Projectile projectile) {

                projectile.setPos(this.getX(), this.getEyeY() - 1.1F, this.getZ());
                projectile.setOwner(this);


                if (flaskEntity instanceof ThrowableItemProjectile itemProjectile) {
                    Item flaskItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "purification_flask"));
                    if (flaskItem != null) {
                        itemProjectile.setItem(new ItemStack(flaskItem));
                    }
                }



                fixProjectileDamage(flaskEntity, 1.0F);





                Vec3 targetVel = target.getDeltaMovement();
                double d0 = target.getX() + targetVel.x - this.getX();
                double d1 = target.getEyeY() - 1.1F - this.getY();
                double d2 = target.getZ() + targetVel.z - this.getZ();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);



                projectile.shoot(d0, d1 + (d3 * 0.2D), d2, 0.5F, 1.0F);


                if (!this.isSilent()) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }

                this.level().addFreshEntity(flaskEntity);
            }
        }
    }


    private void fixProjectileDamage(Entity entity, float damageValue) {
        try {

            try {
                Method setDamage = entity.getClass().getMethod("setDamage", float.class);
                setDamage.invoke(entity, damageValue);
                return;
            } catch (NoSuchMethodException ignored) {}


            Field damageField = null;
            try {
                damageField = entity.getClass().getDeclaredField("damage");
            } catch (NoSuchFieldException e) {

                try {
                    damageField = entity.getClass().getDeclaredField("amount");
                } catch (NoSuchFieldException ignored) {}
            }

            if (damageField != null) {
                damageField.setAccessible(true);
                damageField.setFloat(entity, damageValue);
            }

        } catch (Exception e) {


        }
    }
}