package caldera.data;

import caldera.Caldera;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Caldera.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeClient()) {
            BlockStates blockStates = new BlockStates(generator, helper);

            generator.addProvider(blockStates);
            generator.addProvider(new ItemModels(generator, helper));
        }
        if (event.includeServer()) {
            BlockTags blockTags = new BlockTags(generator, helper);

            generator.addProvider(blockTags);
            generator.addProvider(new ItemTags(generator, blockTags, helper));
            generator.addProvider(new Recipes(generator));
            generator.addProvider(new LootTables(generator));
        }
    }
}
