package caldera.common.recipe.conversion;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.EntityIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public record EntityConversionRecipeImpl(ResourceLocation id, ResourceLocation conversionType, EntityIngredient ingredient, EntityType<? extends Mob> resultEntity) implements EntityConversionRecipe {

    @Override
    public boolean matches(LivingEntity livingEntity) {
        return ingredient.test(livingEntity);
    }

    @Nullable
    @Override
    public LivingEntity convertEntity(ServerLevel level, LivingEntity input) {
        if (!(input instanceof Mob inputMob)) {
            Caldera.LOGGER.error("Failed to convert %s as it is not a mob".formatted(input.getType().getRegistryName()));
            return null;
        }

        if (!resultEntity.getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL) {
            Caldera.LOGGER.warn("Conversion of %s to %s failed as difficulty is set to peaceful".formatted(input.getType().getRegistryName(), resultEntity.getRegistryName()));
        }

        if (!ForgeEventFactory.canLivingConvert(input, resultEntity, (timer) -> { })) {
            Caldera.LOGGER.warn("Conversion of %s to %s was cancelled".formatted(input.getType().getRegistryName(), resultEntity.getRegistryName()));
            return null;
        }

        Mob result = inputMob.convertTo(resultEntity, true);

        if (result == null) {
            Caldera.LOGGER.error("Failed to convert %s as the entity no longer exists".formatted(input.getType().getRegistryName()));
            return null;
        }

        // TODO copy villager data & other entity specific stuff

        ForgeEventFactory.onLivingConvert(input, result);

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ENTITY_CONVERSION_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EntityConversionRecipeImpl> {

        @Override
        public EntityConversionRecipeImpl fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            EntityIngredient ingredient = EntityIngredient.fromJson(object, "ingredient");
            ResourceLocation resultId = CraftingHelper.readResourceLocation(object, "result");
            if (!ForgeRegistries.ENTITIES.containsKey(resultId)) {
                throw new JsonParseException("Entity type %s does not exist".formatted(resultId));
            }

            try {
                // noinspection unchecked
                EntityType<? extends Mob> resultType = (EntityType<? extends Mob>) ForgeRegistries.ENTITIES.getValue(resultId);
                return new EntityConversionRecipeImpl(id, conversionType, ingredient, resultType);
            } catch (ClassCastException exception) {
                throw new JsonParseException("Entity %s is not a mob");
            }
        }

        @Override
        public EntityConversionRecipeImpl fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            EntityIngredient ingredient = EntityIngredient.fromBuffer(buffer);
            ResourceLocation entityId = buffer.readResourceLocation();
            // noinspection unchecked
            EntityType<? extends Mob> resultEntity = (EntityType<? extends Mob>) ForgeRegistries.ENTITIES.getValue(entityId);
            return new EntityConversionRecipeImpl(id, conversionType, ingredient, resultEntity);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EntityConversionRecipeImpl recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            recipe.ingredient.toBuffer(buffer);
            // noinspection ConstantConditions
            buffer.writeResourceLocation(recipe.resultEntity.getRegistryName());
        }
    }
}
