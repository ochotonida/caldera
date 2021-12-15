package caldera.data;

import caldera.Caldera;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.brew.generic.component.action.Actions;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.brew.generic.component.trigger.Triggers;
import caldera.data.brewtype.FinishedBrewType;
import caldera.data.brewtype.GenericBrewTypeBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

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
                .onTrigger(Triggers.BREW_CREATED.get().create())
                .startEffect("transmute_iron", EffectProviders.ITEM_CONVERSION.get().transmute(new ResourceLocation(Caldera.MODID, "iron_to_gold"), 5))
                .executeAction("set_starting_color", Actions.CHANGE_COLOR.get().setColor(0xeedd11))
                .executeAction("spawn_particles", Actions.SPAWN_PARTICLES.get().spawnParticles(new BrewParticleProvider(ParticleTypes.ENTITY_EFFECT, true), 50))
                .startEffect("emit_swirls", EffectProviders.PARTICLE_EMITTER.get().emitter(new BrewParticleProvider(ParticleTypes.ENTITY_EFFECT, true), 0.5))
                .end()

                .onEffectEnded("transmute_iron")
                .executeAction("spawn_particles")
                .executeAction("fade_to_red", Actions.CHANGE_COLOR.get().changeColor(0xee4411, 80))
                .executeAction("play_fuse_sound", Actions.PLAY_SOUND.get().playSound(SoundEvents.TNT_PRIMED))
                .startTimer("explosion_timer", 80)
                .end()

                .onEffectEnded("explosion_timer")
                .executeAction("explode", Actions.EXPLODE.get().explode(3))
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
