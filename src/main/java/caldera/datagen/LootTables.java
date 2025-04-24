package caldera.datagen;

import caldera.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LootTables extends LootTableProvider {

    public LootTables(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, Set.of(), List.of(
                new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    public record BlockLoot(HolderLookup.Provider registries) implements LootTableSubProvider {

        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            blockDropsWithProperties(consumer, ModBlocks.LARGE_CAULDRON.get());
        }

        private void blockDropsWithProperties(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer, Block block) {
            consumer.accept(block.getLootTable(), blockDropsWithProperties(block));
        }

        private LootTable.Builder blockDropsWithProperties(Block block) {
            return LootTable.lootTable()
                    .withPool(LootPool
                            .lootPool()
                            .when(ExplosionCondition.survivesExplosion())
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem
                                    .lootTableItem(block)
                            )
                    );
        }
    }
}
