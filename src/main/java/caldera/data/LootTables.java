package caldera.data;

import caldera.common.block.LargeCauldronBlock;
import caldera.common.init.ModBlocks;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTables extends LootTableProvider {

    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTables = new ArrayList<>();

    public LootTables(DataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        blockDropsWithProperties(ModBlocks.LARGE_CAULDRON.get(),
                StatePropertiesPredicate.Builder
                        .properties()
                        .hasProperty(LargeCauldronBlock.HALF, DoubleBlockHalf.LOWER)
                        .hasProperty(LargeCauldronBlock.FACING, Direction.SOUTH)
        );

        return lootTables;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((location, lootTable) -> LootTableManager.validate(validationtracker, location, lootTable));
    }

    private void blockDropsWithProperties(Block block, StatePropertiesPredicate.Builder properties) {
        blockDrops(block,
                LootTable.lootTable()
                        .withPool(LootPool
                                .lootPool()
                                .when(SurvivesExplosion.survivesExplosion())
                                .setRolls(ConstantRange.exactly(1))
                                .add(ItemLootEntry
                                        .lootTableItem(block)
                                        .when(BlockStateProperty
                                                .hasBlockStateProperties(block)
                                                .setProperties(properties)
                                        )
                                )
                        )
        );
    }

    private void blockDrops(Block block, LootTable.Builder lootTable) {
        lootTables.add(Pair.of(() -> lootBuilder -> lootBuilder.accept(block.getLootTable(), lootTable), LootParameterSets.BLOCK));
    }
}
