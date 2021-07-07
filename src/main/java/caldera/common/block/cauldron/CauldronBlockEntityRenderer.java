package caldera.common.block.cauldron;

import caldera.Caldera;
import caldera.client.util.ColorHelper;
import caldera.common.recipe.brew.Brew;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class CauldronBlockEntityRenderer extends TileEntityRenderer<CauldronBlockEntity> {

    public CauldronBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CauldronBlockEntity cauldron, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        if (cauldron.getLevel() == null) {
            return;
        }
        if (!cauldron.isController()) {
            return;
        }

        float floorHeight = (4 + 0.001F) / 16;
        float fluidLevel = floorHeight + cauldron.fluidLevel.get(partialTicks);

        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                float previousAlpha = cauldron.previousFluidAlpha.get(partialTicks);
                renderFluid(cauldron, cauldron.getPreviousFluid(), fluidLevel, x, z, buffer, matrixStack, light, previousAlpha, 1);
                renderBrew(cauldron, cauldron.getPreviousBrew(), fluidLevel, x, z, partialTicks, buffer, matrixStack, light, previousAlpha);

                float alpha = cauldron.fluidAlpha.get(partialTicks);
                float brewingColorAlpha = cauldron.brewingColorAlpha.get(partialTicks);
                renderFluid(cauldron, cauldron.getFluid(), fluidLevel, x, z, buffer, matrixStack, light, alpha, brewingColorAlpha);
                renderBrew(cauldron, cauldron.getBrew(), fluidLevel, x, z, partialTicks, buffer, matrixStack, light, alpha);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(CauldronBlockEntity cauldron) {
        return cauldron.isController();
    }

    public static void renderFluid(CauldronBlockEntity cauldron, FluidStack fluidStack, float fluidHeight, int x, int z, IRenderTypeBuffer buffer, MatrixStack matrixStack, int light, float alpha, float brewingColorAlpha) {
        if (fluidStack.isEmpty()) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        FluidAttributes fluidAttributes = fluid.getAttributes();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(PlayerContainer.BLOCK_ATLAS)
                .apply(fluidAttributes.getStillTexture(fluidStack));

        IVertexBuilder builder = buffer.getBuffer(RenderType.translucentMovingBlock());

        int color;
        if (fluidStack.getFluid() == Fluids.WATER) {
            color = fluidAttributes.getColor(cauldron.getLevel(), cauldron.getBlockPos());
        } else {
            color = fluidAttributes.getColor(fluidStack);
        }

        if ((color & 0xFFFFFF) != 0xFFFFFF) {
            color = ColorHelper.mixColors(color, ColorHelper.applyAlpha(CauldronBlockEntity.BREWING_COLOR, 1), brewingColorAlpha);
        }

        color = ColorHelper.applyAlpha(color, alpha);

        int blockLight = (light >> 4) & 0xf;
        int luminosity = Math.max(blockLight, fluidAttributes.getLuminosity(fluidStack));
        light = (light & 0xf00000) | luminosity << 4;

        float u1 = fluidTexture.getU(x == 0 ? 2 : 0);
        float v1 = fluidTexture.getV(z == 0 ? 2 : 0);
        float u2 = fluidTexture.getU(x == 0 ? 16 : 14);
        float v2 = fluidTexture.getV(z == 0 ? 16 : 14);

        buildVertices(builder, matrixStack, fluidHeight, x, z, u1, v1, u2, v2, light, color);
    }

    public static void renderBrew(CauldronBlockEntity cauldron, Brew brew, float fluidHeight, int x, int z, float partialTicks, IRenderTypeBuffer buffer, MatrixStack matrixStack, int light, float alpha) {
        if (brew == null) {
            return;
        }

        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(PlayerContainer.BLOCK_ATLAS)
                .apply(new ResourceLocation(Caldera.MODID, "block/brew"));

        IVertexBuilder builder = buffer.getBuffer(RenderType.translucentMovingBlock());

        int color = ColorHelper.applyAlpha(brew.getColorAndAlpha(partialTicks), alpha);

        float u1 = fluidTexture.getU(x == 0 ? 1 : 8);
        float v1 = fluidTexture.getV(z == 0 ? 1 : 8);
        float u2 = fluidTexture.getU(x == 0 ? 8 : 15);
        float v2 = fluidTexture.getV(z == 0 ? 8 : 15);

        buildVertices(builder, matrixStack, fluidHeight, x, z, u1, v1, u2, v2, light, color);
    }

    private static void buildVertices(IVertexBuilder builder, MatrixStack matrixStack, float height, int x, int z, float u1, float v1, float u2, float v2, int light, int color) {
        float xMin = x == 0 ? 2 / 16F : 1;
        float zMin = z == 0 ? 2 / 16F : 1;
        float xMax = x == 0 ? 1 : 2 - 2 / 16F;
        float zMax = z == 0 ? 1 : 2 - 2 / 16F;

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

        int a = ColorHelper.getAlpha(color);
        int r = ColorHelper.getRed(color);
        int g = ColorHelper.getGreen(color);
        int b = ColorHelper.getBlue(color);

        builder.vertex(peek.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(light)
                .normal(normal.getX(), normal.getY(), normal.getZ())
                .endVertex();
    }
}
