package net.alekrus.shphysarum;

import com.mojang.logging.LogUtils;


import net.alekrus.shphysarum.Block.SculkPortalEntity;

import net.alekrus.shphysarum.Block.SculkPortalRenderer;
import net.alekrus.shphysarum.Config.ModConfigScreen;
import net.alekrus.shphysarum.Entities.PureWitch.ModEntities;
import net.alekrus.shphysarum.Entities.PureWitch.SculkWitchEntity;
import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.ItemsAndTab.ModCreativeModeTabs;
import net.alekrus.shphysarum.ModSounds.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(shPhysarum.MODID)
public class shPhysarum
{
    public static final String MODID = "shphysarum";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public shPhysarum(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, net.alekrus.shphysarum.Config.ModClientConfig.SPEC);
        net.alekrus.shphysarum.Block.ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        PacketHandler.register();
        ModSounds.register(modEventBus);


        
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parentScreen) -> new ModConfigScreen(parentScreen))
        );


        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO i'm jej");
    }

    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            
            event.put(ModEntities.SCULK_WITCH.get(), SculkWitchEntity.createAttributes().build());
            
            event.put(ModEntities.SCULK_PORTAL.get(), SculkPortalEntity.createAttributes().build());
        }
    }

    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            
            event.registerEntityRenderer(ModEntities.SCULK_WITCH.get(), net.alekrus.shphysarum.Entities.PureWitch.SculkWitchRenderer::new);

            
            event.registerEntityRenderer(ModEntities.SCULK_PORTAL.get(), SculkPortalRenderer::new);
        }
    }
}
