package cy.jdkdigital.generatorgalore.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidContainerUtil
{
    private static float getRed(int color) {
        return (float) (color >> 16 & 255) / 255.0F;
    }

    private static float getGreen(int color) {
        return (float) (color >> 8 & 255) / 255.0F;
    }

    private static float getBlue(int color) {
        return (float) (color & 255) / 255.0F;
    }

    private static float getAlpha(int color) {
        return (float) (color >> 24 & 255) / 255.0F;
    }

    public static void setColors(int color) {
        RenderSystem.setShaderColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
    }

    public static void renderFluidTank(GuiGraphics matrices, AbstractContainerScreen<?> screen, FluidStack stack, int capacity, int x, int y, int width, int height, int depth) {
        renderFluidTank(matrices, screen, stack, stack.getAmount(), capacity, x, y, width, height, depth);
    }

    public static void renderFluidTank(GuiGraphics matrices, AbstractContainerScreen<?> screen, FluidStack stack, int amount, int capacity, int x, int y, int width, int height, int depth) {
        if(!stack.isEmpty() && capacity > 0) {
            // Hard clamp to ensure amount never exceeds capacity
            amount = Math.min(amount, capacity);

            // Use float calculation for precision, then round to nearest integer
            // This ensures that (10000 / 10000) * 54 = exactly 54
            int fluidHeight = Math.max(1, Math.round((amount / (float)capacity) * height));
            int maxY = y + height;

            // Draw pragmatic fallback: solid colored rectangle first
            int fluidColor = getFluidColorForFallback(stack);
            int absoluteX = screen.getGuiLeft() + x;
            int absoluteY = screen.getGuiTop() + y;

            // Draw solid colored fallback rectangle with high Z-Level (150)
            // The formula y + height - fluidHeight ensures the fluid starts at the correct position
            // and ends exactly at y + height when full
            matrices.fill(absoluteX, absoluteY + height - fluidHeight, absoluteX + width, absoluteY + height, depth, fluidColor);

            // Then try to render the actual fluid texture using modern NeoForge 1.21.1 approach
            renderTiledFluid(matrices, screen, stack, x, maxY - fluidHeight, width, fluidHeight, depth);
        }
    }

    private static int getFluidColorForFallback(FluidStack stack) {
        // Default to lava orange color (0xFFFF4500)
        int defaultColor = 0xFFFF4500;

        try {
            // Try to get the actual fluid color from attributes
            var attributes = IClientFluidTypeExtensions.of(stack.getFluid());
            int tintColor = attributes.getTintColor();
            if (tintColor != 0xFFFFFFFF) { // If not white (default), use it
                return tintColor;
            }
        } catch (Exception e) {
            // If anything fails, use default lava orange
            return defaultColor;
        }

        return defaultColor;
    }

    public static void renderTiledFluid(GuiGraphics guiGraphics, AbstractContainerScreen<?> screen, FluidStack stack, int x, int y, int width, int height, int depth) {
        if (!stack.isEmpty()) {
            try {
                // Get fluid attributes and texture location using modern NeoForge 1.21.1 approach
                var attributes = IClientFluidTypeExtensions.of(stack.getFluid());

                // Get the sprite using the official Minecraft texture atlas system
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture());

                // Set the fluid color tint
                setColors(attributes.getTintColor());

                // Calculate absolute screen coordinates
                int absoluteX = screen.getGuiLeft() + x;
                int absoluteY = screen.getGuiTop() + y;

                // Use the official GuiGraphics.blit method for sprite rendering
                // This handles tiling and UV coordinates automatically
                guiGraphics.blit(absoluteX, absoluteY, depth, width, height, sprite);

                // Reset shader color
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            } catch (Exception e) {
                // If texture rendering fails, the fallback rectangle will still be visible
            }
        }
    }
}