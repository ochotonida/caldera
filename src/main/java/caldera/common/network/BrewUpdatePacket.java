package caldera.common.network;

import caldera.common.init.ModBlockEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class BrewUpdatePacket {

    private final BlockPos pos;
    private final CompoundTag tag;

    @SuppressWarnings("unused")
    public BrewUpdatePacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        tag = buffer.readNbt();
    }

    public BrewUpdatePacket(BlockPos pos, CompoundTag tag) {
        this.pos = pos;
        this.tag = tag;
    }

    @SuppressWarnings("unused")
    void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(tag);
    }

    void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.getBlockEntity(pos, ModBlockEntityTypes.LARGE_CAULDRON.get())
                .ifPresent(cauldron -> {
                    if (cauldron.hasBrew()) {
                        cauldron.getBrew().onUpdate(tag);
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
