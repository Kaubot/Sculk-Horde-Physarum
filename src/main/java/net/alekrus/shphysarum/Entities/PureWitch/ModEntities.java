package net.alekrus.shphysarum.Entities.PureWitch;


import net.alekrus.shphysarum.Block.SculkPortalEntity;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, shPhysarum.MODID);

    public static final RegistryObject<EntityType<SculkWitchEntity>> SCULK_WITCH =
            ENTITY_TYPES.register("sculk_witch",
                    () -> EntityType.Builder.of(SculkWitchEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f) 
                            .build("sculk_witch"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    public static final RegistryObject<EntityType<SculkPortalEntity>> SCULK_PORTAL =
            ENTITY_TYPES.register("sculk_portal",
                    () -> EntityType.Builder.of(SculkPortalEntity::new, MobCategory.MISC)
                            .sized(1.0F, 2.5F) 
                            .build("sculk_portal"));



}
