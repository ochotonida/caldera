package caldera.common.brew.generic.component;

import caldera.common.recipe.transmutation.TransmutationRecipe;
import caldera.common.util.CraftingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TransmutationHelper<INPUT, RECIPE extends TransmutationRecipe<?, INPUT>> {

    private final List<ResourceLocation> cachedRecipes;
    private final RecipeType<RECIPE> recipeType;
    private final ResourceLocation transmutationType;

    public TransmutationHelper(RecipeType<RECIPE> recipeType, ResourceLocation transmutationType) {
        this.cachedRecipes = new LinkedList<>();
        this.recipeType = recipeType;
        this.transmutationType = transmutationType;
    }

    public ResourceLocation getTransmutationType() {
        return transmutationType;
    }

    public Optional<RECIPE> findMatchingRecipe(RecipeManager manager, INPUT input) {
        for (Iterator<ResourceLocation> iterator = cachedRecipes.iterator(); iterator.hasNext(); ) {
            ResourceLocation recipeId = iterator.next();

            Optional<? extends Recipe<?>> optionalRecipe = manager.byKey(recipeId);
            if (optionalRecipe.isPresent() && optionalRecipe.get().getType() == recipeType) {
                // noinspection unchecked
                RECIPE recipe = ((RECIPE) optionalRecipe.get());
                if (recipe.matches(transmutationType, input)) {
                    iterator.remove();
                    cachedRecipes.add(0, recipeId);
                    return Optional.of(recipe);
                }
            } else {
                iterator.remove();
            }
        }

        for (RECIPE recipe : CraftingHelper.getRecipesByType(manager, recipeType)) {
            if (recipe.matches(transmutationType, input)) {
                cachedRecipes.add(0, recipe.getId());
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }
}
