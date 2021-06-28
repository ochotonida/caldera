package caldera.common.util;

import caldera.mixin.accessor.RecipeManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
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

    public static <RECIPE extends IRecipe<IInventory>> Map<ResourceLocation, RECIPE> byType(
            RecipeManager manager,
            IRecipeType<RECIPE> type
    ) {
        // noinspection unchecked
        return (Map<ResourceLocation, RECIPE>) ((RecipeManagerAccessor) getManager()).caldera$callByType(type);
    }

    public static <RECIPE extends IRecipe<IInventory>> Map<ResourceLocation, RECIPE> byType(
            IRecipeType<RECIPE> type
    ) {
        return byType(getManager(), type);
    }
}

