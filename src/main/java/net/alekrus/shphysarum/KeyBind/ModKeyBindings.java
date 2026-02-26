package net.alekrus.shphysarum.KeyBind;

import net.alekrus.shphysarum.shPhysarum;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.sculk_shphysarum";


    public static final KeyMapping ABILITY_MENU_KEY = new KeyMapping(
            "key.shphysarum.ability_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );


    public static final KeyMapping ABILITY_ACTIVATE_KEY = new KeyMapping(
            "key.shphysarum.ability_activate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );


    public static final KeyMapping MOVE_KEY = new KeyMapping(
            "key.shphysarum.sculk_move",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );


    public static final KeyMapping GRAVEMIND_KEY = new KeyMapping(
            "key.shphysarum.gravemind_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ABILITY_MENU_KEY);
        event.register(ABILITY_ACTIVATE_KEY);
        event.register(MOVE_KEY);
        event.register(GRAVEMIND_KEY);

    }
}
