package cy.jdkdigital.generatorgalore.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidContainerUtil
{
    private static FluidModel fluidModel(FluidStack stack) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(stack.getFluid().defaultFluidState());
    }

    private static int fluidTint(FluidStack stack) {
        FluidModel model = fluidModel(stack);
        int color = model.tintSource() != null ? model.tintSource().color(stack.getFluid().defaultFluidState().createLegacyBlock()) : -1;
        return 0xFF000000 | (color & 0xFFFFFF);
    }

    @SuppressWarnings("deprecation")
    public static void renderFluidTank(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack stack, int capacity, int x, int y, int width, int height) {
        if (stack.isEmpty() || capacity <= 0 || stack.getAmount() <= 0) {
            return;
        }
        Identifier stillTexture = fluidModel(stack).stillMaterial().sprite().contents().name();
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, stillTexture));
        int tint = fluidTint(stack);
        int filledHeight = Math.min(height * stack.getAmount() / capacity, height);
        int startX = screen.getLeftPos() + x;
        int bottomY = screen.getTopPos() + y + height;
        int topY = bottomY - filledHeight;
        int spriteWidth = Math.max(1, sprite.contents().width());
        int spriteHeight = Math.max(1, sprite.contents().height());

        graphics.enableScissor(startX, topY, startX + width, bottomY);
        for (int py = bottomY - spriteHeight; py > topY - spriteHeight; py -= spriteHeight) {
            for (int px = startX; px < startX + width; px += spriteWidth) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, px, py, spriteWidth, spriteHeight, tint);
            }
        }
        graphics.disableScissor();
    }
}
