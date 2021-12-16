package caldera.common.brew.generic.component.effect.effects;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.init.ModEffectProviders;
import caldera.common.init.ModTriggers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ConsumeItemsEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<ConsumeItemsEffectType.ConsumeItemsEffectProvider> {

    @Override
    public ConsumeItemsEffectProvider deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        int maxConsumed = -1;
        if (object.has("maxConsumed")) {
            maxConsumed = GsonHelper.getAsInt(object, "maxConsumed");
            if (maxConsumed <= 0) {
                throw new JsonParseException("Maximum amount of items consumed must be positive");
            }
        }
        int maxStackSize = -1;
        if (object.has("maxStackSize")) {
            maxStackSize = GsonHelper.getAsInt(object, "maxStackSize");
            if (maxStackSize <= 0) {
                throw new JsonParseException("Maximum item stack size be positive");
            }
        }
        ItemPredicate itemPredicate = ItemPredicate.fromJson(object.get("item"));
        return new ConsumeItemsEffectProvider(itemPredicate, maxConsumed, maxStackSize);
    }

    @Override
    public ConsumeItemsEffectProvider deserialize(FriendlyByteBuf buffer) {
        return new ConsumeItemsEffectProvider(ItemPredicate.ANY, 0, 0);
    }

    public static ConsumeItemsEffectProvider consumeItems(ItemPredicate item, int maxConsumed) {
        return consumeItems(item, maxConsumed, -1);
    }

    public static ConsumeItemsEffectProvider consumeItems(ItemPredicate item, int maxConsumed, int maxStackSize) {
        return new ConsumeItemsEffectProvider(item, maxConsumed, maxStackSize);
    }

    public static class ConsumeItemsEffectProvider extends EffectProvider {

        private final ItemPredicate itemPredicate;
        private final int maxConsumed;
        private final int maxStackSize;

        public ConsumeItemsEffectProvider(ItemPredicate itemPredicate, int maxConsumed, int maxStackSize) {
            this.maxConsumed = maxConsumed;
            this.itemPredicate = itemPredicate;
            this.maxStackSize = maxStackSize;
        }

        @Override
        public Effect create(GenericBrew brew) {
            return new ConsumeItemsEffect(brew, maxConsumed);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            int itemsRemaining = maxConsumed;
            if (tag.contains("ItemsRemaining", Tag.TAG_INT)) {
                itemsRemaining = tag.getInt("ItemsRemaining");
            }
            return new ConsumeItemsEffect(brew, itemsRemaining);
        }

        @Override
        public EffectProviderType<?> getType() {
            return ModEffectProviders.CONSUME_ITEMS.get();
        }

        @Override
        public void serialize(JsonObject object) {
            if (maxConsumed != -1) {
                object.addProperty("maxConsumed", maxConsumed);
            }
            if (maxStackSize != -1) {
                object.addProperty("maxStackSize", maxStackSize);
            }
            if (itemPredicate != ItemPredicate.ANY) {
                object.add("item", itemPredicate.serializeToJson());
            }
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {

        }

        public class ConsumeItemsEffect implements Effect {

            private final GenericBrew brew;

            private int itemsRemaining;

            public ConsumeItemsEffect(GenericBrew brew, int itemsRemaining) {
                this.brew = brew;
                this.itemsRemaining = itemsRemaining;
            }

            @Override
            public void consumeItem(ItemEntity itemEntity) {
                if (brew.getCauldron().getLevel() == null) {
                    return;
                }

                if (!itemPredicate.matches(itemEntity.getItem())) {
                    return;
                }

                int amount = itemEntity.getItem().getCount();
                if (maxStackSize != -1) {
                    amount = Math.min(amount, maxStackSize);
                }
                if (maxConsumed != -1) {
                    amount = Math.min(amount, itemsRemaining);
                }

                ItemStack consumedStack = itemEntity.getItem().split(amount);

                ModTriggers.ITEM_CONSUMED.get().trigger(brew, getIdentifier(), consumedStack);

                if (itemsRemaining > 0) {
                    brew.getCauldron().setChanged();
                    itemsRemaining -= amount;
                    if (itemsRemaining == 0) {
                        brew.endEffect(getIdentifier());
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
