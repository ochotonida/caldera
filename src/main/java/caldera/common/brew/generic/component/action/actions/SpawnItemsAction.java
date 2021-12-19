package caldera.common.brew.generic.component.action.actions;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;

public class SpawnItemsAction extends ForgeRegistryEntry<ActionType<?>> implements ActionType<SpawnItemsAction.RollLootTableAction> {

    @Override
    public RollLootTableAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation lootTable = new ResourceLocation(GsonHelper.getAsString(object, "lootTable"));

        return new RollLootTableAction(lootTable);
    }

    @Nullable
    @Override
    public RollLootTableAction deserialize(FriendlyByteBuf buffer) {
        return null;
    }

    public static RollLootTableAction spawnItems(ResourceLocation lootTable) {
        return new RollLootTableAction(lootTable);
    }

    public static final class RollLootTableAction extends SimpleAction {

        private final ResourceLocation lootTable;

        public RollLootTableAction(ResourceLocation lootTable) {
            this.lootTable = lootTable;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.SPAWN_ITEMS.get();
        }

        @Override
        public void execute(GenericBrew brew) {
            Cauldron cauldron = brew.getCauldron();
            if (cauldron.getLevel() instanceof ServerLevel level) {
                LootContext context = new LootContext.Builder(level)
                        .withRandom(level.random)
                        .create(LootContextParamSets.EMPTY);
                List<ItemStack> items = level.getServer().getLootTables().get(lootTable).getRandomItems(context);

                for (ItemStack item : items) {
                    cauldron.discardItem(item, new Vec3(level.random.nextDouble() - 0.5, 0, level.random.nextDouble() - 0.5));
                }
            }
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("lootTable", lootTable.toString());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) { }
    }
}
