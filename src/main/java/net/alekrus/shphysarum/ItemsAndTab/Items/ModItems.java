package net.alekrus.shphysarum.ItemsAndTab.Items;

import net.alekrus.shphysarum.Entities.PureWitch.ModEntities;
import net.alekrus.shphysarum.ItemsAndTab.Items.OpenNewTree.RichBiomassBarkItem;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore.NutrientEssenceItem;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, shPhysarum.MODID);

    public static final RegistryObject<Item> SCULK_APPLE = ITEMS.register("sculk_apple", () -> new Item(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .food(new net.minecraft.world.food.FoodProperties.Builder()
                    .alwaysEat()
                    .nutrition(1)
                    .saturationMod(0.1f)
                    .build())) {

        

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

            if (InfectionHandler.isInfected(player)) {
                if (!world.isClientSide) {

                }
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            return super.use(world, player, hand);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
            if (entity instanceof Player player) {

                if (!world.isClientSide) {
                    InfectionHandler.setInfected(player, true);
                    player.sendSystemMessage(Component.literal("§3You feel unbearable pain"));


                    world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0f, 0.5f);
                    world.playSound(null, player.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0f, 0.8f);


                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));

                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 0));


                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 0));

                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));

                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 2));

                }


                else {

                    net.alekrus.shphysarum.ItemsAndTab.Items.SculkTransformationClientHandler.triggerTransformation();
                }
            }
            return super.finishUsingItem(stack, world, entity);
        }
    });
    public static final RegistryObject<Item> MYCELIUM_SPROUT = ITEMS.register("mycelium_sprout",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.COMMON))); 






    public static final RegistryObject<Item> SCULK_WITCH_SPAWN_EGG = ITEMS.register("sculk_witch_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.SCULK_WITCH, 
                    0x061e38, 
                    0x009295, 
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> RICH_BIOMASS_BARK = ITEMS.register("rich_biomass_bark",
            () -> new RichBiomassBarkItem(new Item.Properties()));



    public static final RegistryObject<Item> MYCELIAL_BRAIN_SCAFFOLD = ITEMS.register("mycelial_brain_scaffold",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));

    public static final RegistryObject<Item> NUTRIENT_ESSENCE = ITEMS.register("nutrient_essence",
            () -> new NutrientEssenceItem(new Item.Properties()
                    .stacksTo(1) 
                    .rarity(Rarity.RARE).fireResistant()));


}
