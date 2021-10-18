package caldera.data;

import caldera.common.init.ModBlocks;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTables extends LootTableProvider {

    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> lootTables = new ArrayList<>();

    public LootTables(DataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        blockDropsWithProperties(ModBlocks.LARGE_CAULDRON.get());

        return lootTables;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext context) {
        map.forEach((location, lootTable) -> net.minecraft.world.level.storage.loot.LootTables.validate(context, location, lootTable));
    }

    private void blockDropsWithProperties(Block block) {
        blockDrops(block,
                LootTable.lootTable()
                        .withPool(LootPool
                                .lootPool()
                                .when(ExplosionCondition.survivesExplosion())
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem
                                        .lootTableItem(block)
                                )
                        )
        );
    }

    private void blockDrops(Block block, LootTable.Builder lootTable) {
        lootTables.add(Pair.of(() -> lootBuilder -> lootBuilder.accept(block.getLootTable(), lootTable), LootContextParamSets.BLOCK));
    }
}
