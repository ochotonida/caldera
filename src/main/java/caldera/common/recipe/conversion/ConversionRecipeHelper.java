package caldera.common.recipe.conversion;

import caldera.Caldera;
import caldera.common.util.CraftingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ConversionRecipeHelper<INPUT, RECIPE extends ConversionRecipe<INPUT>> {

    private final List<ResourceLocation> cachedRecipes;
    private final RecipeType<RECIPE> recipeType;
    private final ResourceLocation conversionType;

    public ConversionRecipeHelper(RecipeType<RECIPE> recipeType, ResourceLocation conversionType) {
        this.cachedRecipes = new LinkedList<>();
        this.recipeType = recipeType;
        this.conversionType = conversionType;
    }

    public ResourceLocation getConversionType() {
        return conversionType;
    }

    public Optional<RECIPE> findMatchingRecipe(RecipeManager manager, INPUT input) {
        if (cachedRecipes.isEmpty()) {
            refresh(manager);
        }

        for (Iterator<ResourceLocation> iterator = cachedRecipes.iterator(); iterator.hasNext(); ) {
            ResourceLocation recipeId = iterator.next();

            Optional<? extends Recipe<?>> optionalRecipe = manager.byKey(recipeId);
            if (optionalRecipe.isPresent() && optionalRecipe.get().getType() == recipeType) {
                // noinspection unchecked
                RECIPE recipe = ((RECIPE) optionalRecipe.get());
                if (recipe.matches(conversionType, input)) {
                    iterator.remove();
                    cachedRecipes.add(0, recipeId);
                    return Optional.of(recipe);
                }
            } else {
                Caldera.LOGGER.error("Cached recipe mismatch: %s does no longer exist".formatted(recipeId));
                iterator.remove();
            }
        }

        return Optional.empty();
    }

    /**
     * Refresh the recipes cached by this conversion helper.
     * Refreshing the cache on data pack reload from action types or effect types isn't necessary,
     * as the action types and effect types get reloaded themselves.
     */
    public void refresh(RecipeManager manager) {
        cachedRecipes.clear();

        for (RECIPE recipe : CraftingHelper.getRecipesByType(manager, recipeType)) {
            if (conversionType.equals(recipe.conversionType())) {
                cachedRecipes.add(recipe.getId());
            }
        }
    }
}
