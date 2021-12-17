package caldera.common.recipe.conversion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface ConversionRecipe<RESULT, INPUT> extends Recipe<Container> {

    ResourceLocation id();

    ResourceLocation conversionType();

    boolean matches(INPUT input);

    default boolean matches(ResourceLocation transmutationType, INPUT input) {
        return conversionType().equals(transmutationType) && matches(input);
    }

    RESULT assemble(ResourceLocation transmutationType, INPUT input);

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default ResourceLocation getId() {
        return id();
    }

    // unused
    @Override
    @Deprecated
    default boolean matches(Container inventory, Level level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack assemble(Container inventory) {
        return ItemStack.EMPTY;
    }

    // unused
    @Override
    @Deprecated
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
