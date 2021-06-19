package caldera.common.block.cauldron;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class CauldronBlockEntityRenderer extends TileEntityRenderer<CauldronBlockEntity> {

    public CauldronBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CauldronBlockEntity cauldron, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        if (!cauldron.isController()) {
            return;
        }

        FluidStack fluid = cauldron.fluidTank.getFluid();

        if (fluid.isEmpty()) {
            return;
        }

        float floorHeight = 4/16F;
        float maxFluidHeight = 20/16F;
        float fluidHeight = floorHeight + (maxFluidHeight - floorHeight) * fluid.getAmount() / (float) cauldron.fluidTank.getCapacity();

        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                float xMin = x == 0 ? 2 / 16F : 1;
                float zMin = z == 0 ? 2 / 16F : 1;
                float xMax = x == 0 ? 1 : 2 - 2 / 16F;
                float zMax = z == 0 ? 1 : 2 - 2 / 16F;
                renderHorizontalFluidFace(fluid, fluidHeight, xMin, zMin, xMax, zMax, buffer, matrixStack, light);
            }
        }
    }

    public static void renderHorizontalFluidFace(FluidStack fluidStack, float height, float xMin, float zMin, float xMax, float zMax, IRenderTypeBuffer buffer, MatrixStack matrixStack, int light) {
        Fluid fluid = fluidStack.getFluid();
        FluidAttributes fluidAttributes = fluid.getAttributes();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(PlayerContainer.BLOCK_ATLAS)
                .apply(fluidAttributes.getStillTexture(fluidStack));

        int color = fluidAttributes.getColor(fluidStack);
        IVertexBuilder builder = buffer.getBuffer(RenderType.translucent());

        int blockLight = (light >> 4) & 0xf;
        int luminosity = Math.max(blockLight, fluidAttributes.getLuminosity(fluidStack));
        light = (light & 0xf00000) | luminosity << 4;

        float u1 = fluidTexture.getU(xMin - (int) xMin);
        float v1 = fluidTexture.getV(zMin - (int) zMin);
        float u2 = fluidTexture.getU(xMax - (int) xMax == 0 ? 16 : (xMax - (int) xMax) * 16);
        float v2 = fluidTexture.getV(zMax - (int) zMax == 0 ? 16 : (zMax - (int) zMax) * 16);

        matrixStack.pushPose();
        putVertex(builder, matrixStack, xMin, height, zMax, color, u1, v2, light);
        putVertex(builder, matrixStack, xMax, height, zMax, color, u2, v2, light);
        putVertex(builder, matrixStack, xMax, height, zMin, color, u2, v1, light);
        putVertex(builder, matrixStack, xMin, height, zMin, color, u1, v1, light);
        matrixStack.popPose();
    }

    private static void putVertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, int color, float u, float v, int light) {
        Vector3i normal = Direction.UP.getNormal();
        MatrixStack.Entry peek = matrixStack.last();

        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        builder.vertex(peek.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(light)
                .normal(normal.getX(), normal.getY(), normal.getZ())
                .endVertex();
    }
}
