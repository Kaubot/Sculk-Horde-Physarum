package net.alekrus.shphysarum.CustomGui.HudPlayer;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkHeartOverlay {


    private static final ResourceLocation SCULK_ICONS = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/icons.png");


    private static final ResourceLocation VANILLA_ICONS = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/icons.png");

    private static final RandomSource random = RandomSource.create();

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;


        if (mc.player.isCreative() || mc.player.isSpectator() || !InfectionHandler.isClientInfected(mc.player)) {
            return;
        }


        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            event.setCanceled(true);
            renderSculkHearts(event.getGuiGraphics(), mc.player, mc);
        }

        else if (event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            event.setCanceled(true);
            renderSculkArmor(event.getGuiGraphics(), mc.player, mc);
        }
    }

    private static void renderSculkHearts(GuiGraphics graphics, Player player, Minecraft mc) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SCULK_ICONS);

        int health = Mth.ceil(player.getHealth());
        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        long tickCount = mc.gui.getGuiTicks();
        int regen = -1;
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
            regen = (int) (tickCount % 25);
        }

        int left = width / 2 - 91;
        int top = height - 39;

        int healthRows = Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        random.setSeed(tickCount * 312871L);

        int totalHearts = Mth.ceil((float) (maxHealth + absorption) / 2.0F);
        int maxHealthHearts = Mth.ceil((float) maxHealth / 2.0F);

        int yOffset = 0;
        if (player.level().getLevelData().isHardcore()) {
            yOffset = 45;
        }

        for (int i = totalHearts - 1; i >= 0; --i) {
            int row = Mth.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health + absorption <= 4) y += random.nextInt(2);
            if (i == regen) y -= 2;


            graphics.blit(SCULK_ICONS, x, y, 16, yOffset, 9, 9);


            if (i < maxHealthHearts) {
                int i2 = i * 2;
                if (i2 + 1 < health) {
                    graphics.blit(SCULK_ICONS, x, y, 52, yOffset, 9, 9);
                } else if (i2 + 1 == health) {
                    graphics.blit(SCULK_ICONS, x, y, 61, yOffset, 9, 9);
                }
            } else {
                int i2 = i * 2;
                if (i2 + 1 < health + absorption) {
                    graphics.blit(SCULK_ICONS, x, y, 160, yOffset, 9, 9);
                } else if (i2 + 1 == health + absorption) {
                    graphics.blit(SCULK_ICONS, x, y, 169, yOffset, 9, 9);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    private static void renderSculkArmor(GuiGraphics graphics, Player player, Minecraft mc) {
        int armorValue = player.getArmorValue();
        if (armorValue <= 0) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SCULK_ICONS);

        int left = width / 2 - 91;
        int top = height - 39;


        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);


        int armorY = top - (healthRows * rowHeight) - 10;

        for (int i = 0; i < 10; ++i) {
            if (armorValue > 0) {
                int x = left + i * 8;

                if (i * 2 + 1 < armorValue) {

                    graphics.blit(SCULK_ICONS, x, armorY, 34, 9, 9, 9);
                } else if (i * 2 + 1 == armorValue) {

                    graphics.blit(SCULK_ICONS, x, armorY, 25, 9, 9, 9);
                } else {

                    graphics.blit(SCULK_ICONS, x, armorY, 16, 9, 9, 9);
                }
            }
        }

        RenderSystem.disableBlend();

        RenderSystem.setShaderTexture(0, VANILLA_ICONS);
    }
}
