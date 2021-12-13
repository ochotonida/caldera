package caldera.common.brew;

import caldera.Caldera;
import caldera.common.init.CalderaRegistries;
import caldera.common.network.BrewTypeSyncPacket;
import caldera.common.network.NetworkHandler;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;

public class BrewTypeManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Map<ResourceLocation, BrewType> brewTypes = Map.of();

    private final PredicateManager predicateManager;

    public BrewTypeManager(PredicateManager predicateManager) {
        super(GSON, Caldera.MODID + "/brew_types");
        this.predicateManager = predicateManager;
    }

    @Nullable
    public static BrewType get(ResourceLocation id) {
        return brewTypes.get(id);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, BrewType> map = ImmutableMap.builder();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                if (entry.getValue().isJsonObject() && !CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions")) {
                    Caldera.LOGGER.debug("Skipping loading brew type {} as it's conditions were not met", id);
                    continue;
                }
                JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), "top element");
                BrewTypeDeserializationContext context = new BrewTypeDeserializationContext(id, predicateManager);
                BrewType brewType = fromJson(object, context);
                map.put(id, brewType);
            } catch (IllegalArgumentException | JsonParseException exception) {
                Caldera.LOGGER.error("Parsing error loading brew type {}", id, exception);
            }
        }

        brewTypes = map.build();
        Caldera.LOGGER.info("Loaded {} brew types", brewTypes.size());
    }

    public static BrewType fromJson(JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(object, "type"));
        if (!CalderaRegistries.BREW_TYPE_SERIALIZERS.containsKey(type)) {
            throw new JsonSyntaxException("Invalid or unsupported brew type '" + type + "'");
        }
        // noinspection ConstantConditions
        return CalderaRegistries.BREW_TYPE_SERIALIZERS.getValue(type).fromJson(object, context);
    }

    public static void setBrewTypes(Map<ResourceLocation, BrewType> brewTypes) {
        BrewTypeManager.brewTypes = brewTypes;
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new BrewTypeManager(event.getDataPackRegistries().getPredicateManager()));
    }

    public static void onDataPackReload(OnDatapackSyncEvent event) {
        if (Environment.get().getDist().isClient()) {
            return;
        }
        if (event.getPlayer() != null) {
            sync(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(BrewTypeManager::sync);
        }
    }

    private static void sync(ServerPlayer player) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BrewTypeSyncPacket(brewTypes));
    }
}
