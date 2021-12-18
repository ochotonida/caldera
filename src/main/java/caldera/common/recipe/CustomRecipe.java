package caldera.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface CustomRecipe extends Recipe<Container> {

    @Override
    default boolean isSpecial() {
        return true;
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
