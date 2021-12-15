package caldera.common.brew.generic.component.effect.effects.conversion;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.conversion.ConversionRecipe;
import caldera.common.util.ConversionRecipeHelper;
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

public class ItemConversionEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<ItemConversionEffectType.ItemTransmutationEffectProvider> {

    @Override
    public ItemTransmutationEffectProvider deserialize(JsonObject object) {
        ResourceLocation transmutationType = CraftingHelper.readResourceLocation(object, "conversionType");
        int maxAmount = -1;
        if (object.has("maxAmount")) {
            maxAmount = GsonHelper.getAsInt(object, "maxAmount");
            if (maxAmount <= 0) {
                throw new JsonParseException("Maximum amount of items transmuted must be positive");
            }
        }
        return new ItemTransmutationEffectProvider(transmutationType, maxAmount);
    }

    @Override
    public ItemTransmutationEffectProvider deserialize(FriendlyByteBuf buffer) {
        ResourceLocation transmutationType = buffer.readResourceLocation();
        int maxAmount = buffer.readInt();
        return new ItemTransmutationEffectProvider(transmutationType, maxAmount);
    }

    public ItemTransmutationEffectProvider transmute(ResourceLocation transmutationType) {
        return transmute(transmutationType, -1);
    }

    public ItemTransmutationEffectProvider transmute(ResourceLocation transmutationType, int maxAmount) {
        return new ItemTransmutationEffectProvider(transmutationType, maxAmount);
    }

    public static class ItemTransmutationEffectProvider implements EffectProvider {

        private final ConversionRecipeHelper<ItemStack, ConversionRecipe<ItemStack, ItemStack>> conversionHelper;
        private final int maxAmount;

        public ItemTransmutationEffectProvider(ResourceLocation transmutationType, int maxAmount) {
             this.conversionHelper = new ConversionRecipeHelper<>(ModRecipeTypes.ITEM_CONVERSION, transmutationType);
             this.maxAmount = maxAmount;
        }

        @Override
        public Effect create(GenericBrew brew, String identifier) {
            return new ItemTransmutationEffect(brew, identifier, maxAmount);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag, String identifier) {
            int itemsRemaining = maxAmount;
            if (tag.contains("ItemsRemaining", Tag.TAG_INT)) {
                itemsRemaining = tag.getInt("ItemsRemaining");
            }
            return new ItemTransmutationEffect(brew, identifier, itemsRemaining);
        }

        @Override
        public EffectProviderType<?> getType() {
            return EffectProviders.ITEM_CONVERSION.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("conversionType", conversionHelper.getConversionType().toString());
            if (maxAmount != -1) {
                object.addProperty("maxAmount", maxAmount);
            }
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(conversionHelper.getConversionType());
            buffer.writeInt(maxAmount);
        }

        public class ItemTransmutationEffect implements Effect {

            private final GenericBrew brew;
            private final String identifier;

            private int itemsRemaining;

            public ItemTransmutationEffect(GenericBrew brew, String identifier, int itemsRemaining) {
                this.brew = brew;
                this.identifier = identifier;
                this.itemsRemaining = itemsRemaining;
            }

            @Override
            public void consumeItem(ItemEntity itemEntity) {
                if (brew.getCauldron().getLevel() == null) {
                    return;
                }

                ItemStack toTransmute = itemEntity.getItem().copy();
                toTransmute.setCount(1);

                Optional<ConversionRecipe<ItemStack, ItemStack>> recipe =
                        conversionHelper.findMatchingRecipe(brew.getCauldron().getLevel().getRecipeManager(), toTransmute);

                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().assemble(conversionHelper.getConversionType(), toTransmute);
                    itemEntity.getItem().shrink(1);
                    brew.getCauldron().discardItem(result, Cauldron.getInitialDeltaMovement(itemEntity));

                    if (itemsRemaining > 0) {
                        brew.getCauldron().setChanged();
                        if (--itemsRemaining == 0) {
                            brew.removeEffect(identifier);
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
