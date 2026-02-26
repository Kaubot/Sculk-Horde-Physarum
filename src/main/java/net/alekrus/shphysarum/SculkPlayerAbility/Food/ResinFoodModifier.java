package net.alekrus.shphysarum.SculkPlayerAbility.Food;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResinFoodModifier {

    
    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            try {
                
                Item resin = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_resin"));

                if (resin == null) {

                    return;
                }

                
                FoodProperties foodStats = new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationMod(0.1f)
                        .alwaysEat() 
                        .build();

                
                
                Field targetField = null;
                for (Field field : Item.class.getDeclaredFields()) {
                    if (field.getType() == FoodProperties.class) {
                        targetField = field;
                        break;
                    }
                }

                if (targetField != null) {
                    targetField.setAccessible(true);
                    targetField.set(resin, foodStats);

                } else {

                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        });
    }
}
