package caldera.common.util;

import caldera.mixin.accessor.RecipeManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Map;

public class RecipeHelper {

    public static RecipeManager getManager() {
        if (EffectiveSide.get().isServer()) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        } else {
            // noinspection ConstantConditions
            return Minecraft.getInstance().player.connection.getRecipeManager();
        }
    }

    public static <RECIPE extends Recipe<Container>> Map<ResourceLocation, RECIPE> byType(
            RecipeManager manager,
            RecipeType<RECIPE> type
    ) {
        // noinspection unchecked
        return (Map<ResourceLocation, RECIPE>) ((RecipeManagerAccessor) getManager()).caldera$callByType(type);
    }

    public static <RECIPE extends Recipe<Container>> Map<ResourceLocation, RECIPE> byType(
            RecipeType<RECIPE> type
    ) {
        return byType(getManager(), type);
    }
}

