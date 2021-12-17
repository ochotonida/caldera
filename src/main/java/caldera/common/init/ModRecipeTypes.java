package caldera.common.init;

import caldera.Caldera;
import caldera.common.recipe.cauldron.CauldronBrewingRecipe;
import caldera.common.recipe.cauldron.CauldronFluidRecipe;
import caldera.common.recipe.cauldron.CauldronItemRecipe;
import caldera.common.recipe.cauldron.CauldronRecipe;
import caldera.common.recipe.conversion.ConversionRecipe;
import caldera.common.recipe.conversion.ItemConversionRecipe;
import caldera.common.recipe.conversion.ToolConversionRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Caldera.MODID);

    public static final RecipeType<CauldronRecipe<ItemStack>> CAULDRON_ITEM_CRAFTING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_item").toString());
    public static final RecipeType<CauldronRecipe<FluidStack>> CAULDRON_FLUID_CRAFTING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_fluid").toString());
    public static final RecipeType<CauldronRecipe<ResourceLocation>> CAULDRON_BREWING = RecipeType.register(new ResourceLocation(Caldera.MODID, "cauldron_brewing").toString());

    public static final RecipeType<ConversionRecipe<ItemStack, ItemStack>> ITEM_CONVERSION = RecipeType.register(new ResourceLocation(Caldera.MODID, "item_conversion").toString());

    public static final RegistryObject<CauldronItemRecipe.Serializer> CAULDRON_ITEM_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_item", CauldronItemRecipe.Serializer::new);
    public static final RegistryObject<CauldronFluidRecipe.Serializer> CAULDRON_FLUID_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_fluid", CauldronFluidRecipe.Serializer::new);
    public static final RegistryObject<CauldronBrewingRecipe.Serializer> CAULDRON_BREWING_SERIALIZER = REGISTRY.register("cauldron_brewing", CauldronBrewingRecipe.Serializer::new);

    public static final RegistryObject<ItemConversionRecipe.Serializer> ITEM_CONVERSION_SERIALIZER = REGISTRY.register("item_conversion", ItemConversionRecipe.Serializer::new);
    public static final RegistryObject<ToolConversionRecipe.Serializer> TOOL_CONVERSION_SERIALIZER = REGISTRY.register("tool_conversion", ToolConversionRecipe.Serializer::new);
}
