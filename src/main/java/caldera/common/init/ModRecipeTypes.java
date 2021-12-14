package caldera.common.init;

import caldera.Caldera;
import caldera.common.recipe.cauldron.CauldronBrewingRecipe;
import caldera.common.recipe.cauldron.CauldronFluidRecipe;
import caldera.common.recipe.cauldron.CauldronItemRecipe;
import caldera.common.recipe.cauldron.CauldronRecipe;
import caldera.common.recipe.transmutation.ItemTransmutationRecipe;
import caldera.common.recipe.transmutation.TransmutationRecipe;
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

    public static final RecipeType<TransmutationRecipe<ItemStack, ItemStack>> ITEM_TRANSMUTATION = RecipeType.register(new ResourceLocation(Caldera.MODID, "item_transmutation").toString());

    public static final RegistryObject<CauldronItemRecipe.Serializer> CAULDRON_ITEM_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_item", CauldronItemRecipe.Serializer::new);
    public static final RegistryObject<CauldronFluidRecipe.Serializer> CAULDRON_FLUID_CRAFTING_SERIALIZER = REGISTRY.register("cauldron_fluid", CauldronFluidRecipe.Serializer::new);
    public static final RegistryObject<CauldronBrewingRecipe.Serializer> CAULDRON_BREWING_SERIALIZER = REGISTRY.register("cauldron_brewing", CauldronBrewingRecipe.Serializer::new);

    public static final RegistryObject<ItemTransmutationRecipe.Serializer> ITEM_TRANSMUTATION_SERIALIZER = REGISTRY.register("item_transmutation", ItemTransmutationRecipe.Serializer::new);
}
