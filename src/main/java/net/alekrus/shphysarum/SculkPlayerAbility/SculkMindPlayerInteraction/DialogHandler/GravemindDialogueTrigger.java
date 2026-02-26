package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.KeyBind.ModKeyBindings; 
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class GravemindDialogueTrigger {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientGravemindState.tick();
        }
    }

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItem().getItem());

            if (itemId != null && itemId.toString().equals("shphysarum:sculk_apple")) {
                if (player.level().isClientSide) {
                    
                    queueCommuneMessage(player);
                }
            }
        }
    }

    
    @OnlyIn(Dist.CLIENT)
    private static void queueCommuneMessage(Player eater) {
        
        
        if (eater != net.minecraft.client.Minecraft.getInstance().player) return;

        
        Component keyNameComponent = ModKeyBindings.GRAVEMIND_KEY.getTranslatedKeyMessage();
        String keyName = keyNameComponent.getString().toUpperCase(); 

        ClientGravemindState.queueMessage("Your body will experience new boundaries");
        
        ClientGravemindState.queueMessage("Press [" + keyName + "] to commune with me");
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            if (event.player instanceof ServerPlayer serverPlayer) {
                if (!InfectionHandler.isInfected(serverPlayer)) return;

                if (serverPlayer.tickCount % 100 == 0) {
                    checkStructures(serverPlayer);
                }
            }
        }
    }

    private static void checkStructures(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        checkAndSend(player, level, pos, "minecraft", "stronghold", "sculk_stronghold_seen", 2);
        checkAndSend(player, level, pos, "minecraft", "mineshaft", "sculk_mineshaft_seen", 3);
        checkAndSend(player, level, pos, "minecraft", "desert_pyramid", "sculk_temple_seen", 4);
        checkAndSend(player, level, pos, "minecraft", "jungle_pyramid", "sculk_temple_seen", 4);
        checkAndSend(player, level, pos, "minecraft", "bastion_remnant", "sculk_bastion_seen", 5);
        checkAndSend(player, level, pos, "minecraft", "fortress", "sculk_fortress_seen", 6);
    }

    private static void checkAndSend(ServerPlayer player, ServerLevel level, BlockPos pos, String namespace, String path, String nbtKey, int packetId) {
        if (player.getPersistentData().getBoolean(nbtKey)) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);

        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);

        if (structure != null) {
            if (level.structureManager().getStructureAt(pos, structure).isValid()) {
                player.getPersistentData().putBoolean(nbtKey, true);
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravemindScenePacket(packetId));
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            if (!InfectionHandler.isInfected(player)) return;

            if (event.getTo().location().toString().equals("minecraft:the_nether")) {
                CompoundTag data = player.getPersistentData();
                if (!data.getBoolean("sculk_nether_seen")) {
                    data.putBoolean("sculk_nether_seen", true);
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravemindScenePacket(1));
                }
            }
        }
    }
}
