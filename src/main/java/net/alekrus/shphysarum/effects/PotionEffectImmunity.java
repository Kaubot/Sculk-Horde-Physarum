package net.alekrus.shphysarum.effects;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PotionEffectImmunity {

    private static final Set<ResourceLocation> IMMUNE_EFFECTS = Set.of(
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "corroded"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "diseased_cysts"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "dense"),
             ResourceLocation.fromNamespaceAndPath("minecraft", "darkness"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "diseased_atmosphere"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_fog"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_lure"),
             ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_infected")
    );

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!InfectionHandler.isInfected(player)) return;

        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
        if (effectId != null && IMMUNE_EFFECTS.contains(effectId)) {
            event.setResult(MobEffectEvent.Applicable.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!InfectionHandler.isInfected(player)) return;

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        if (effectId != null && IMMUNE_EFFECTS.contains(effectId)) {
            player.removeEffect(event.getEffectInstance().getEffect());
        }
    }
}
