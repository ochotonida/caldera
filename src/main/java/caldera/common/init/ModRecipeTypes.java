package caldera.common.init;

import caldera.Caldera;
import caldera.common.recipe.BrewRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeTypes {

    public static final DeferredRegister<IRecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Caldera.MODID);

    public static final IRecipeType<BrewRecipe<?>> BREW = IRecipeType.register(new ResourceLocation(Caldera.MODID, "brew").toString());

    // public static final RegistryObject<IRecipeSerializer<?>> BREW_SERIALIZER = REGISTRY.register("brew", SpecialBrewRecipe.Serializer::new);
}
