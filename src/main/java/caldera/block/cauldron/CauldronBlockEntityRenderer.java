package caldera.block.cauldron;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CauldronBlockEntityRenderer implements BlockEntityRenderer<CauldronBlockEntity> {

    @SuppressWarnings("unused")
    public CauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(CauldronBlockEntity cauldron, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if (cauldron.getLevel() == null) {
            return;
        }
        if (!cauldron.isController()) {
            return;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(CauldronBlockEntity cauldron) {
        return cauldron.isController();
    }
}
