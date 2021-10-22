package caldera.common.init;

import caldera.Caldera;
import caldera.common.recipe.CauldronBrewingRecipe;
import caldera.common.recipe.CauldronFluidRecipe;
import caldera.common.recipe.CauldronItemRecipe;
import caldera.common.recipe.CauldronRecipe;
import caldera.common.recipe.brew.BrewType;
import caldera.common.recipe.brew.generic.GenericBrewType;
import caldera.common.recipe.brew.sludge.SludgeBrewType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Caldera.MODID);

    public static final RecipeType<CauldronRecipe<ItemStack>> CAULDRON_ITEM_CRAFTING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_item").toString());
    public static final RecipeType<CauldronRecipe<FluidStack>> CAULDRON_FLUID_CRAFTING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_fluid").toString());
    public static final RecipeType<CauldronRecipe<ResourceLocation>> CAULDRON_BREWING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_brewing").toString());
    public static final RecipeType<BrewType> BREW_TYPE = RecipeType.register(new ResourceLocation(Caldera.MODID, "brew_type").toString());

    public static final RegistryObject<CauldronItemRecipe.Serializer> CAULDRON_ITEM_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_item", CauldronItemRecipe.Serializer::new);
    public static final RegistryObject<CauldronFluidRecipe.Serializer> CAULDRON_FLUID_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_fluid", CauldronFluidRecipe.Serializer::new);
    public static final RegistryObject<CauldronBrewingRecipe.Serializer> CAULDRON_BREWING_SERIALIZER = REGISTRY.register("cauldron_brewing", CauldronBrewingRecipe.Serializer::new);
    public static final RegistryObject<SludgeBrewType.Serializer> SLUDGE_BREW_SERIALIZER = REGISTRY.register("sludge_brew", SludgeBrewType.Serializer::new);
    public static final RegistryObject<GenericBrewType.Serializer> GENERIC_BREW_SERIALIZER = REGISTRY.register("brew", GenericBrewType.Serializer::new);
}
