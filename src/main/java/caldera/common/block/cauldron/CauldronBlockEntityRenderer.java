package caldera.common.block.cauldron;

import caldera.Caldera;
import caldera.common.brew.Brew;
import caldera.common.util.ColorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

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

        float floorHeight = (4 + 0.001F) / 16;
        float fluidLevel = floorHeight + cauldron.getVisualFluidLevel(partialTicks);

        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                float previousAlpha = cauldron.previousFluidAlpha.getValue(partialTicks);
                renderFluid(cauldron, cauldron.getPreviousFluid(), fluidLevel, x, z, buffer, matrixStack, light, previousAlpha, 1);
                renderBrew(cauldron, cauldron.getPreviousBrew(), fluidLevel, x, z, partialTicks, buffer, matrixStack, light, previousAlpha);

                float alpha = cauldron.fluidAlpha.getValue(partialTicks);
                float brewingColorAlpha = cauldron.brewingColorAlpha.getValue(partialTicks);
                renderFluid(cauldron, cauldron.getFluid(), fluidLevel, x, z, buffer, matrixStack, light, alpha, brewingColorAlpha);
                renderBrew(cauldron, cauldron.getBrew(), fluidLevel, x, z, partialTicks, buffer, matrixStack, light, alpha);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(CauldronBlockEntity cauldron) {
        return cauldron.isController();
    }

    public static void renderFluid(CauldronBlockEntity cauldron, FluidStack fluidStack, float fluidHeight, int x, int z, MultiBufferSource buffer, PoseStack matrixStack, int light, float alpha, float brewingColorAlpha) {
        if (fluidStack.isEmpty()) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        FluidAttributes fluidAttributes = fluid.getAttributes();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidAttributes.getStillTexture(fluidStack));

        VertexConsumer builder = buffer.getBuffer(RenderType.translucentMovingBlock());

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

    public static void renderBrew(CauldronBlockEntity cauldron, Brew brew, float fluidHeight, int x, int z, float partialTicks, MultiBufferSource buffer, PoseStack matrixStack, int light, float alpha) {
        if (brew == null) {
            return;
        }

        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation(Caldera.MODID, "block/brew"));

        VertexConsumer builder = buffer.getBuffer(RenderType.translucentMovingBlock());

        int color = ColorHelper.applyAlpha(brew.getColorAndAlpha(partialTicks), alpha);

        float u1 = fluidTexture.getU(x == 0 ? 1 : 8);
        float v1 = fluidTexture.getV(z == 0 ? 1 : 8);
        float u2 = fluidTexture.getU(x == 0 ? 8 : 15);
        float v2 = fluidTexture.getV(z == 0 ? 8 : 15);

        buildVertices(builder, matrixStack, fluidHeight, x, z, u1, v1, u2, v2, light, color);
    }

    private static void buildVertices(VertexConsumer builder, PoseStack matrixStack, float height, int x, int z, float u1, float v1, float u2, float v2, int light, int color) {
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

    private static void putVertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, int color, float u, float v, int light) {
        Vec3i normal = Direction.UP.getNormal();
        PoseStack.Pose peek = matrixStack.last();

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
