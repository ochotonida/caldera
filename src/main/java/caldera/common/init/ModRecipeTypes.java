package caldera.common.init;

import caldera.Caldera;
import caldera.common.recipe.CauldronFluidRecipe;
import caldera.common.recipe.CauldronItemRecipe;
import caldera.common.recipe.CauldronRecipe;
import caldera.common.recipe.brew.BrewType;
import caldera.common.recipe.brew.sludge.SludgeBrewType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeTypes {

    public static final DeferredRegister<IRecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Caldera.MODID);

    public static final IRecipeType<CauldronRecipe<ItemStack>> CAULDRON_ITEM_CRAFTING = IRecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_item").toString());
    public static final IRecipeType<CauldronRecipe<FluidStack>> CAULDRON_FLUID_CRAFTING = IRecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_fluid").toString());
    public static final IRecipeType<BrewType<?>> BREW_TYPE = IRecipeType.register(new ResourceLocation(Caldera.MODID, "brew_type").toString());

    public static final RegistryObject<IRecipeSerializer<?>> CAULDRON_ITEM_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_item", CauldronItemRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> CAULDRON_FLUID_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_fluid", CauldronFluidRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> SLUDGE_BREW_SERIALIZER = REGISTRY.register("sludge_brew", SludgeBrewType.Serializer::new);
}
