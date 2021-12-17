package caldera.common.brew.generic.component.effect.effects.conversion;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.init.ModEffectProviders;
import caldera.common.init.ModRecipeTypes;
import caldera.common.init.ModTriggers;
import caldera.common.recipe.conversion.ConversionRecipe;
import caldera.common.recipe.conversion.ConversionRecipeHelper;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;

public class ConvertItemsEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<ConvertItemsEffectType.ConvertItemsEffectProvider> {

    @Override
    public ConvertItemsEffectProvider deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
        int maxConverted = -1;
        if (object.has("maxConverted")) {
            maxConverted = GsonHelper.getAsInt(object, "maxConverted");
            if (maxConverted <= 0) {
                throw new JsonParseException("Maximum amount of items converted must be positive");
            }
        }
        return new ConvertItemsEffectProvider(conversionType, maxConverted);
    }

    @Override
    public ConvertItemsEffectProvider deserialize(FriendlyByteBuf buffer) {
        ResourceLocation conversionType = buffer.readResourceLocation();
        int maxConverted = buffer.readInt();
        return new ConvertItemsEffectProvider(conversionType, maxConverted);
    }

    public static ConvertItemsEffectProvider convertItems(ResourceLocation conversionType) {
        return convertItems(conversionType, -1);
    }

    public static ConvertItemsEffectProvider convertItems(ResourceLocation conversionType, int maxConverted) {
        return new ConvertItemsEffectProvider(conversionType, maxConverted);
    }

    public static class ConvertItemsEffectProvider extends EffectProvider {

        private final ConversionRecipeHelper<ItemStack, ConversionRecipe<ItemStack, ItemStack>> conversionHelper;
        private final int maxConverted;

        public ConvertItemsEffectProvider(ResourceLocation conversionType, int maxConverted) {
             this.conversionHelper = new ConversionRecipeHelper<>(ModRecipeTypes.ITEM_CONVERSION, conversionType);
             this.maxConverted = maxConverted;
        }

        @Override
        public Effect create(GenericBrew brew) {
            return new ConvertItemsEffect(brew, maxConverted);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            int itemsRemaining = maxConverted;
            if (tag.contains("ItemsRemaining", Tag.TAG_INT)) {
                itemsRemaining = tag.getInt("ItemsRemaining");
            }
            return new ConvertItemsEffect(brew, itemsRemaining);
        }

        @Override
        public EffectProviderType<?> getType() {
            return ModEffectProviders.CONVERT_ITEMS.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("conversionType", conversionHelper.getConversionType().toString());
            if (maxConverted != -1) {
                object.addProperty("maxConverted", maxConverted);
            }
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(conversionHelper.getConversionType());
            buffer.writeInt(maxConverted);
        }

        public class ConvertItemsEffect implements Effect {

            private final GenericBrew brew;

            private int itemsRemaining;

            public ConvertItemsEffect(GenericBrew brew, int itemsRemaining) {
                this.brew = brew;
                this.itemsRemaining = itemsRemaining;
            }

            @Override
            public void consumeItem(ItemEntity itemEntity) {
                if (brew.getCauldron().getLevel() == null) {
                    return;
                }

                ItemStack toConvert = itemEntity.getItem().copy();
                toConvert.setCount(1);

                Optional<ConversionRecipe<ItemStack, ItemStack>> recipe =
                        conversionHelper.findMatchingRecipe(brew.getCauldron().getLevel().getRecipeManager(), toConvert);

                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().assemble(toConvert);
                    itemEntity.getItem().shrink(1);
                    brew.getCauldron().discardItem(result, Cauldron.getInitialDeltaMovement(itemEntity));

                    ModTriggers.ITEM_CONVERTED.get().trigger(brew, getIdentifier(), toConvert, result);

                    if (itemsRemaining > 0) {
                        brew.getCauldron().setChanged();
                        if (--itemsRemaining == 0) {
                            brew.endEffect(getIdentifier());
                        }
                    }
                }
            }

            @Override
            public void save(CompoundTag tag) {
                tag.putInt("ItemsRemaining", itemsRemaining);
            }
        }
    }
}
