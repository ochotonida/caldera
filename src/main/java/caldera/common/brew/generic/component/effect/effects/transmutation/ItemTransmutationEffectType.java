package caldera.common.brew.generic.component.effect.effects.transmutation;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.TransmutationHelper;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.transmutation.TransmutationRecipe;
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

public class ItemTransmutationEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<ItemTransmutationEffectType.ItemTransmutationEffectProvider> {

    @Override
    public ItemTransmutationEffectProvider deserialize(JsonObject object) {
        ResourceLocation transmutationType = CraftingHelper.readResourceLocation(object, "transmutationType");
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

        private final TransmutationHelper<ItemStack, TransmutationRecipe<ItemStack, ItemStack>> transmutationHelper;
        private final int maxAmount;

        public ItemTransmutationEffectProvider(ResourceLocation transmutationType, int maxAmount) {
             this.transmutationHelper = new TransmutationHelper<>(ModRecipeTypes.ITEM_TRANSMUTATION, transmutationType);
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
            return EffectProviders.ITEM_TRANSMUTATION.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("transmutationType", transmutationHelper.getTransmutationType().toString());
            if (maxAmount != -1) {
                object.addProperty("maxAmount", maxAmount);
            }
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(transmutationHelper.getTransmutationType());
            buffer.writeInt(maxAmount);
        }

        public class ItemTransmutationEffect implements Effect {

            // TODO prevent transmuting more than 1 item at once
            private final GenericBrew brew;
            private final String identifier; // TODO move identifier to effect provider (same in timer effect)

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

                int count = itemEntity.getItem().getCount();
                if (itemsRemaining != -1) {
                    count = Math.min(count, itemsRemaining);
                }

                ItemStack toTransmute = itemEntity.getItem().copy();
                toTransmute.setCount(count);

                Optional<TransmutationRecipe<ItemStack, ItemStack>> recipe =
                        transmutationHelper.findMatchingRecipe(brew.getCauldron().getLevel().getRecipeManager(), toTransmute);

                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().assemble(transmutationHelper.getTransmutationType(), toTransmute);
                    itemEntity.getItem().shrink(count);
                    if (itemsRemaining != -1) {
                        itemsRemaining -= count;
                        brew.getCauldron().setChanged();
                    }
                    brew.getCauldron().discardItem(result, Cauldron.getInitialDeltaMovement(itemEntity));
                    if (itemsRemaining == 0) {
                        brew.removeEffect(identifier);
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
