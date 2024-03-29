package caldera.data;

import caldera.Caldera;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.brew.generic.component.action.actions.*;
import caldera.common.brew.generic.component.action.actions.conversion.ConvertEntitiesActionType;
import caldera.common.brew.generic.component.effect.effects.ConsumeItemsEffectType;
import caldera.common.brew.generic.component.effect.effects.EmitParticlesEffectType;
import caldera.common.brew.generic.component.effect.effects.conversion.ConvertItemsEffectType;
import caldera.common.brew.generic.component.trigger.triggers.CauldronBrokenTriggerType;
import caldera.common.brew.generic.component.trigger.triggers.EntityDiedTriggerType;
import caldera.common.brew.generic.component.trigger.triggers.ItemConsumedTriggerType;
import caldera.common.brew.generic.component.trigger.triggers.ItemConvertedTriggerType;
import caldera.common.init.ModTriggers;
import caldera.data.brewtype.FinishedBrewType;
import caldera.data.brewtype.GenericBrewTypeBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public record BrewTypes(DataGenerator generator) implements DataProvider {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    public void run(HashCache cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        buildBrewTypes((brewType) -> {
            if (!set.add(brewType.getId())) {
                throw new IllegalStateException("Duplicate recipe " + brewType.getId());
            } else {
                saveBrewType(cache, brewType.serializeBrewType(), path.resolve("data/" + brewType.getId().getNamespace() + "/caldera/brew_types/" + brewType.getId().getPath() + ".json"));
            }
        });
    }

    private static void saveBrewType(HashCache cache, JsonObject object, Path path) {
        try {
            String json = GSON.toJson(object);
            String hash = SHA1.hashUnencodedChars(json).toString();
            if (!Objects.equals(cache.getHash(path), hash) || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (BufferedWriter bufferedwriter = Files.newBufferedWriter(path)) {
                    bufferedwriter.write(json);
                }
            }
            cache.putNew(path, hash);
        } catch (IOException exception) {
            Caldera.LOGGER.error("Couldn't save brew type {}", path, exception);
        }
    }

    protected void buildBrewTypes(Consumer<FinishedBrewType> consumer) {
        genericBrew("test_brew")
                .onTrigger(ModTriggers.BREW_CREATED.get().create())
                .groupId("setup")
                .startEffect("transmute_iron", ConvertItemsEffectType.convertItems(new ResourceLocation(Caldera.MODID, "iron_to_gold"), 5))
                .startEffect("consume_tnt", ConsumeItemsEffectType.consumeItems(ItemPredicate.Builder.item().of(Items.TNT).build(), 1))
                .executeAction("set_starting_color", ChangeColorActionType.setColor(0xeedd11))
                .executeAction("spawn_particles", SpawnParticlesActionType.spawnParticles(new BrewParticleProvider(ParticleTypes.ENTITY_EFFECT, true), 50))
                .startEffect("emit_swirls", EmitParticlesEffectType.emitParticles(new BrewParticleProvider(ParticleTypes.ENTITY_EFFECT, true), 0.5))
                .end()

                .onTrigger(EntityDiedTriggerType.entityDied(
                        EntityPredicate.Builder.entity()
                                .of(EntityType.CREEPER)
                                .effects(MobEffectsPredicate.effects().and(MobEffects.MOVEMENT_SPEED))
                                .build(),
                        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER)).build()
                ))
                .groupId("on_creeper_died")
                .executeAction("play_transmutation_sound")
                .executeAction("spawn_loot", SpawnItemsAction.spawnItems(new ResourceLocation("chests/simple_dungeon")))
                .end()

                .onTrigger(ItemConsumedTriggerType.itemConverted(null, ItemPredicate.ANY))
                .groupId("start_fuse")
                .startTimer("explosion_timer", 80)
                .executeAction("spawn_particles")
                .executeAction("fade_to_red", ChangeColorActionType.changeColor(0xee4411, 80))
                .executeAction("play_fuse_sound", PlaySoundActionType.playSound(SoundEvents.TNT_PRIMED))
                .end()

                .onEffectEnded("transmute_iron")
                .groupId("handle_transmutation_ended")
                .removeEffect("consume_tnt")
                .executeAction("start_fuse")
                .end()

                .onTrigger(ItemConvertedTriggerType.itemConverted("transmute_iron", ItemPredicate.ANY, ItemPredicate.ANY))
                .executeAction("play_transmutation_sound", PlaySoundActionType.playSound(SoundEvents.ENCHANTMENT_TABLE_USE))
                .end()

                .onEffectEnded("explosion_timer")
                .groupId("explode")
                .executeAction("convert_entities", ConvertEntitiesActionType.convert(new ResourceLocation(Caldera.MODID, "test_conversion"), 8))
                .executeAction("dye_sheep", ConvertEntitiesActionType.convert(new ResourceLocation(Caldera.MODID, "dyeing/red"), 8))
                .executeAction("spawn_explosion", ExplodeActionType.explode(3))
                .end()

                .onTrigger(CauldronBrokenTriggerType.cauldronBroken(EntityPredicate.Composite.ANY))
                .executeAction("explode")
                .end()

                .save(consumer);
    }

    protected GenericBrewTypeBuilder genericBrew(String id) {
        return GenericBrewTypeBuilder.builder(id);
    }

    @Override
    public String getName() {
        return "Brew Types";
    }
}
