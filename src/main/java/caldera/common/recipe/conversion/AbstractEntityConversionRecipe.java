package caldera.common.recipe.conversion;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public interface AbstractEntityConversionRecipe extends ConversionRecipe<LivingEntity, LivingEntity> {

    @Override
    @Nullable
    LivingEntity assemble(LivingEntity input);

    @Override
    default RecipeType<?> getType() {
        return ModRecipeTypes.ENTITY_CONVERSION;
    }

    @Nullable
    default LivingEntity convertEntity(ServerLevel level, LivingEntity input) {
        LivingEntity result = assemble(input);
        if (result == null) {
            Caldera.LOGGER.warn("Failed to convert %s".formatted(input.getType().getRegistryName()));
            return input;
        } else if (result.getType().getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL) {
            Caldera.LOGGER.warn("Failed to convert %s to %s as difficulty is set to peaceful".formatted(input.getType().getRegistryName(), result.getType().getRegistryName()));
            return input;
        } else if (result == input) {
            // FIXME trigger entity converted forge events
            return input;
        } else {
            // noinspection unchecked
            if (!ForgeEventFactory.canLivingConvert(input, (EntityType<? extends LivingEntity>) result.getType(), timer -> {})) {
                return input;
            }

            if (!level.tryAddFreshEntityWithPassengers(result)) {
                Caldera.LOGGER.error("Failed to convert %s, tried to add an entity with a duplicate UUID".formatted(input.getType().getRegistryName()));
                return input;
            }
            input.discard();
            ForgeEventFactory.onLivingConvert(input, result);
            return result;
        }
    }
}
