package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.Block.SoulAnchorBlock.SoulAnchorBlock;
import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, shPhysarum.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, shPhysarum.MODID);


    
    public static final RegistryObject<Block> SCULK_BEACON = registerBlock("sculk_beacon",
            () -> new SculkBeaconBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops() 
                    .strength(3f)
                    .sound(SoundType.SCULK_SENSOR)),
            block -> new SculkBeaconItem(block, new Item.Properties()) 
    );


    public static final RegistryObject<BlockEntityType<SculkBeaconBlockEntity>> SCULK_BEACON_BE =
            BLOCK_ENTITIES.register("sculk_beacon_be", () ->
                    BlockEntityType.Builder.of(SculkBeaconBlockEntity::new, SCULK_BEACON.get()).build(null));


    
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, Function<Block, Item> customItem) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> customItem.apply(toReturn.get()));
        return toReturn;
    }

    
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }



    public static final RegistryObject<Block> SOUL_ANCHOR = registerBlock("soul_anchor",
            () -> new SoulAnchorBlock(BlockBehaviour.Properties.copy(Blocks.RESPAWN_ANCHOR)
                    .requiresCorrectToolForDrops()
                    .strength(10.0F, 200.0F)
                    .lightLevel(state -> (int) (state.getValue(SoulAnchorBlock.CHARGES) * (15.0f / 12.0f)))
                    .sound(SoundType.SCULK_CATALYST)));

}
