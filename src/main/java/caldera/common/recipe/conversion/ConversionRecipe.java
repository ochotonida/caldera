package caldera.common.recipe.conversion;

import caldera.common.recipe.CustomRecipe;
import net.minecraft.resources.ResourceLocation;

public interface ConversionRecipe<INPUT> extends CustomRecipe {

    ResourceLocation id();

    ResourceLocation conversionType();

    boolean matches(INPUT input);

    default boolean matches(ResourceLocation conversionType, INPUT input) {
        return conversionType().equals(conversionType) && matches(input);
    }

    @Override
    default ResourceLocation getId() {
        return id();
    }
}
