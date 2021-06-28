package caldera.common.util;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.brew.BrewType;
import caldera.mixin.accessor.RecipeManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class RecipeHelper {

    public static RecipeManager getManager() {
        if (EffectiveSide.get().isServer()) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        } else {
            // noinspection ConstantConditions
            return Minecraft.getInstance().player.connection.getRecipeManager();
        }
    }

    @Nullable
    public static BrewType<?> getBrewType(ResourceLocation id) {
        return (BrewType<?>) ((RecipeManagerAccessor) getManager()).caldera$callByType(ModRecipeTypes.BREW_TYPE).get(id);
    }
}

