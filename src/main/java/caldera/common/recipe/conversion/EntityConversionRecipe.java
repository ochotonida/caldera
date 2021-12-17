package caldera.common.recipe.conversion;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.EntityIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public record EntityConversionRecipe(ResourceLocation id, ResourceLocation conversionType, EntityIngredient ingredient, EntityType<?> resultEntity, @Nullable CompoundTag entityNbt) implements AbstractEntityConversionRecipe {

    @Override
    public boolean matches(LivingEntity livingEntity) {
        return ingredient.test(livingEntity);
    }

    @Override
    public LivingEntity assemble(LivingEntity input) {
        if (!(input.getLevel() instanceof ServerLevel level)) {
            return null;
        }

        CompoundTag tag = entityNbt == null ? new CompoundTag() : entityNbt;
        // noinspection ConstantConditions
        tag.putString("id", resultEntity.getRegistryName().toString());
        Entity result = EntityType.loadEntityRecursive(tag, input.level, (entity) -> {
            entity.moveTo(input.position().x, input.position().y, input.position().z, input.getYRot(), input.getXRot());
            return entity;
        });

        if (!(result instanceof LivingEntity)) {
            Caldera.LOGGER.error("Could not convert %s, result is not a living entity".formatted(input.getType().getRegistryName()));
            return null;
        }

        if (entityNbt == null) {
            if (result instanceof Mob resultMob) {
                resultMob.finalizeSpawn(level, level.getCurrentDifficultyAt(input.blockPosition()), MobSpawnType.CONVERSION, null, null);

                if (input instanceof Mob inputMob) {
                    convertMob(resultMob, inputMob);
                }
            }
        }

        return (LivingEntity) result;
    }

    private static void convertMob(Mob result, Mob input) {
        result.setBaby(input.isBaby());
        result.setNoAi(input.isNoAi());
        if (input.hasCustomName()) {
            result.setCustomName(input.getCustomName());
            result.setCustomNameVisible(input.isCustomNameVisible());
        }

        if (input.isPersistenceRequired()) {
            result.setPersistenceRequired();
        }

        result.setInvulnerable(input.isInvulnerable());

        result.setCanPickUpLoot(input.canPickUpLoot());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemstack = input.getItemBySlot(slot);
            if (!itemstack.isEmpty()) {
                result.setItemSlot(slot, itemstack.copy());
                result.setDropChance(slot, 0); // TODO use getDropChance
                itemstack.setCount(0);
            }
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ENTITY_CONVERSION_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EntityConversionRecipe> {

        @Override
        public EntityConversionRecipe fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            EntityIngredient ingredient = EntityIngredient.fromJson(object, "ingredient");
            JsonObject result = GsonHelper.getAsJsonObject(object, "result");
            ResourceLocation resultId = CraftingHelper.readResourceLocation(result, "entity");
            if (!ForgeRegistries.ENTITIES.containsKey(resultId)) {
                throw new JsonParseException("Entity type %s does not exist".formatted(resultId));
            }
            EntityType<?> resultType = ForgeRegistries.ENTITIES.getValue(resultId);
            CompoundTag nbt = null;
            if (result.has("nbt")) {
                nbt = CraftingHelper.readNbt(object, "nbt");
            }

            return new EntityConversionRecipe(id, conversionType, ingredient, resultType, nbt);
        }

        @Override
        public EntityConversionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            EntityIngredient ingredient = EntityIngredient.fromBuffer(buffer);
            ResourceLocation entityId = buffer.readResourceLocation();
            // noinspection unchecked
            EntityType<? extends LivingEntity> resultEntity = (EntityType<? extends LivingEntity>) ForgeRegistries.ENTITIES.getValue(entityId);
            CompoundTag nbt = buffer.readNbt();
            return new EntityConversionRecipe(id, conversionType, ingredient, resultEntity, nbt);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EntityConversionRecipe recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            recipe.ingredient.toBuffer(buffer);
            // noinspection ConstantConditions
            buffer.writeResourceLocation(recipe.resultEntity.getRegistryName());
            buffer.writeNbt(recipe.entityNbt);
        }
    }
}
