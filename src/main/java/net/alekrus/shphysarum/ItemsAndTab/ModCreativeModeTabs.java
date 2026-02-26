package net.alekrus.shphysarum.ItemsAndTab;

import net.alekrus.shphysarum.Block.ModBlocks;
import net.alekrus.shphysarum.Entities.PureWitch.ModEntities;
import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, shPhysarum.MODID);

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("shphysarum_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SCULK_APPLE.get()))
                    .title(Component.translatable("creativetab.shphysarum_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        
                        pOutput.accept(ModItems.SCULK_APPLE.get());
                        pOutput.accept(ModItems.MYCELIUM_SPROUT.get());
                        pOutput.accept(ModItems.SCULK_WITCH_SPAWN_EGG.get());
                        
                        pOutput.accept(ModItems.MYCELIAL_BRAIN_SCAFFOLD.get());
                        pOutput.accept(ModBlocks.SCULK_BEACON.get());
                        pOutput.accept(ModBlocks.SOUL_ANCHOR.get());
                        pOutput.accept(ModItems.RICH_BIOMASS_BARK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
