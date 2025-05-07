package caldera.registry;

import caldera.Caldera;
import caldera.block.cauldron.contents.BrewBaseContents;
import caldera.block.cauldron.contents.CauldronContents;
import caldera.block.cauldron.contents.FluidContents;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCauldronContentTypes {

    public static final DeferredRegister<CauldronContents.Type<?>> CAULDRON_CONTENT_TYPES = DeferredRegister.create(ModRegistries.CAULDRON_CONTENT_TYPES, Caldera.MOD_ID);

    public static final Supplier<CauldronContents.Type<FluidContents>> FLUID = register("fluid", FluidContents.CODEC);
    public static final Supplier<CauldronContents.Type<BrewBaseContents>> BREW_BASE = register("brew_base", BrewBaseContents.CODEC);

    private static <T extends CauldronContents> Supplier<CauldronContents.Type<T>> register(String name, MapCodec<T> codec) {
        return CAULDRON_CONTENT_TYPES.register(name, () -> new CauldronContents.Type<>(codec));
    }
}
