package net.alekrus.shphysarum.Craft;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkCraftingRestriction {

    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;

        if (player.containerMenu != null) {
            boolean isInfected = InfectionHandler.isInfected(player);
            boolean isUsingInfestedTable = player.containerMenu.getClass().getName().contains("InfestedCraftingTable");

            
            if (isInfected && isUsingInfestedTable) return;

            boolean didBlock = false;

            for (net.minecraft.world.inventory.Slot slot : player.containerMenu.slots) {
                
                if (slot.container instanceof net.minecraft.world.inventory.ResultContainer || slot.getClass().getName().contains("ResultSlot")) {
                    ItemStack result = slot.getItem();

                    if (!result.isEmpty()) {
                        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(result.getItem());
                        if (itemID != null) {
                            String namespace = itemID.getNamespace();
                            String path = itemID.getPath();

                            boolean isRestrictedItem =
                                    (namespace.equals("sculkhorde") && path.equals("sculk_dura_matter")) ||
                                            (namespace.equals("shphysarum") && path.equals("soul_anchor")) ||
                                            (namespace.equals("sculkhorde") && path.equals("spike"));

                            if (isRestrictedItem) {
                                
                                
                                
                                slot.set(ItemStack.EMPTY);
                                didBlock = true;
                            }
                        }
                    }
                }
            }

            if (didBlock) {
                
                player.containerMenu.broadcastChanges();
                playRejectMessage(player, isInfected);
            }
        }
    }

    
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ItemStack resultStack = event.getCrafting();
        if (resultStack.isEmpty()) return;

        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(resultStack.getItem());
        if (itemID == null) return;

        Player player = event.getEntity();
        String path = itemID.getPath();
        String namespace = itemID.getNamespace();

        boolean isRestrictedItem =
                (namespace.equals("sculkhorde") && path.equals("sculk_dura_matter")) ||
                        (namespace.equals("shphysarum") && path.equals("soul_anchor")) ||
                        (namespace.equals("sculkhorde") && path.equals("spike"));

        if (!isRestrictedItem) return;

        boolean isInfected = InfectionHandler.isInfected(player);
        boolean isUsingInfestedTable = player.containerMenu != null && player.containerMenu.getClass().getName().contains("InfestedCraftingTable");

        if (isInfected && isUsingInfestedTable) return;

        
        
        
        resultStack.setCount(0);
    }

    private static void playRejectMessage(Player player, boolean isInfected) {
        long currentTime = player.level().getGameTime();
        long lastMsgTime = player.getPersistentData().getLong("sh_lastCraftReject");

        
        if (currentTime - lastMsgTime >= 20) {
            player.getPersistentData().putLong("sh_lastCraftReject", currentTime);

            if (isInfected) {
                player.displayClientMessage(Component.literal("§3This structure requires the Infested Crafting Table."), true);
            } else {
                player.displayClientMessage(Component.literal("§cYour mind cannot comprehend this structure."), true);
            }
        }
    }
}
