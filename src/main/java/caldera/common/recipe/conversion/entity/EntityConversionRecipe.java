package caldera.common.recipe.conversion.entity;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.conversion.ConversionRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public interface EntityConversionRecipe extends ConversionRecipe<LivingEntity> {

    /**
     * Convert an entity according to this recipe. The result may be equal to the input, or null if the conversion failed.
     */
    @Nullable
    LivingEntity convertEntity(ServerLevel level, LivingEntity input);

    @Override
    default RecipeType<?> getType() {
        return ModRecipeTypes.ENTITY_CONVERSION;
    }
}
