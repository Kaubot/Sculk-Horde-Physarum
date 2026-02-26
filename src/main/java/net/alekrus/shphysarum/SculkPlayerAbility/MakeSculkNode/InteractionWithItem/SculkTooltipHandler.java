package net.alekrus.shphysarum.SculkPlayerAbility.MakeSculkNode.InteractionWithItem;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkTooltipHandler {

    private static final Style SCULK_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#00CED1")).withItalic(true);

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        if (!InfectionHandler.isClientInfected(player)) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (itemId == null) return;

        String idString = itemId.toString();
        String customText = null;

        switch (idString) {
            case "sculkhorde:infestation_purifier":
                customText = "It hurts to hold this";
                break;
            case "sculkhorde:eye_of_purity":
                customText = "Useless";
                break;
            case "sculkhorde:infestation_ward_block":
                customText = "It hurts to hold this";
                break;
            case "sculkhorde:purification_flask":
                customText = "It hurts to hold this";
                break;
            case "sculkhorde:heart_of_purity":
                customText = "This must be destroyed";
                break;
            case "sculkhorde:pure_souls":
                customText = "They have not seen a better existence";
                break;
            case "sculkhorde:blade_of_purity":
                customText = "I feel a burning sensation when I pick it up";
                break;
            case "sculkhorde:essence_of_purity":
                customText = "Useless";
                break;
            case "sculkhorde:chunk_o_brain":
                customText = "It can be put back together";
                break;
            case "sculkhorde:sculk_resin":
                customText = "Can be absorbed";
                break;
            case "minecraft:shield":
                customText = "With new body, it became inconvenient";
                break;
            case "sculkhorde:sculk_sweeper_sword":
                customText = "I know a better use for this...";
                break;
            case "shphysarum:mycelial_brain_scaffold":
                customText = "Can be filled with souls in a Soul Harvester to get a Sculk Node";
                break;
            case "shphysarum:mycelium_sprout":
                customText = "It can be saturated in the Sculk Beacon";
                break;
            case "shphysarum:rich_biomass_bark":
                customText = "One step to the highest";
                break;
            case "shphysarum:sculk_beacon":
                customText = "It won't be as easy as it seems.";
                break;
            case "shphysarum:soul_anchor":
                customText = "More convenient than its predecessor, it charges with crying souls.";
                break;
        }


        if (customText != null) {

            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal(customText).withStyle(SCULK_STYLE));
        }
    }
}