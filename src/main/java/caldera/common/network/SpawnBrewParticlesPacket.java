package caldera.common.network;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.init.ModBlockEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnBrewParticlesPacket {

    private final BlockPos pos;
    private final int count;
    private final BrewParticleProvider particle;

    public SpawnBrewParticlesPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        count = buffer.readInt();
        particle = BrewParticleProvider.deserialize(buffer);
    }

    public SpawnBrewParticlesPacket(BlockPos pos, int count, BrewParticleProvider particle) {
        this.pos = pos;
        this.count = count;
        this.particle = particle;
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(count);
        particle.serialize(buffer);
    }

    void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.getBlockEntity(pos, ModBlockEntityTypes.LARGE_CAULDRON.get())
                        .flatMap(Cauldron::getGenericBrew)
                        .ifPresent(brew -> particle.spawnParticles(brew, count));
            }
        });
        context.get().setPacketHandled(true);
    }
}
