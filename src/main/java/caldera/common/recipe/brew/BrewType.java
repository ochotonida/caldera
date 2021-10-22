package caldera.common.recipe.brew;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.Cauldron;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public abstract class BrewType implements Recipe<Container> {

    private final ResourceLocation id;

    protected BrewType(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Creates the result of this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be discarded after creating the result,
     * @param cauldron The cauldron constructing this recipe
     * @return The result of this recipe
     */
    public abstract Brew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron);

    public abstract Brew create(Cauldron cauldron);

    @Override
    public ResourceLocation getId() {
        return id;
    }

    // unused
    @Override
    @Deprecated
    public final boolean matches(Container inventory, Level level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    public final ItemStack assemble(Container inventory) {
        return ItemStack.EMPTY;
    }

    // unused
    @Override
    @Deprecated
    public final boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    public final ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public final RecipeType<?> getType() {
        return ModRecipeTypes.BREW_TYPE;
    }

}
