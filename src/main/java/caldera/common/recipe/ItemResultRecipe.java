package caldera.common.recipe;


import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public interface ItemResultRecipe extends Recipe<Container> {

    ItemStack result();
}
