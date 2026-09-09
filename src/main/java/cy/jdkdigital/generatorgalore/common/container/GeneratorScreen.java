package cy.jdkdigital.generatorgalore.common.container;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.FluidContainerUtil;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu>
{
    private static final Identifier GUI_SOLID = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_solid.png");
    private static final Identifier GUI_FLUID = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_fluid.png");
    private static final Identifier GUI_SOLID_CHARGING = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_solid_charging.png");
    private static final Identifier GUI_FLUID_CHARGING = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_fluid_charging.png");

    private static final int LABEL_COLOR = 0xFF404040;

    public GeneratorScreen(GeneratorMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(font, this.title.getString(), 8, 6, LABEL_COLOR, false);
        guiGraphics.text(font, this.playerInventoryTitle.getString(), 8, this.getImageHeight() - 96 + 2, LABEL_COLOR, false);

        guiGraphics.text(font, Component.translatable(GeneratorGalore.MODID + ".screen.generation_rate", this.menu.blockEntity.getGenerationRate()).getString(), this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? 51 : 8, 24, LABEL_COLOR, false);

        List<FormattedCharSequence> tooltipList = new ArrayList<>();
        int energyAmount = this.menu.blockEntity.energyHandler.getAmountAsInt();

        // Energy level tooltip
        if (isHovering(134, 16, 16, 54, mouseX, mouseY)) {
            tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.energy_level", energyAmount + "/" + this.menu.blockEntity.energyHandler.getCapacityAsInt() + "FE").getVisualOrderText());
        }

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            FluidStack fluidStack = getFluid();

            // Fluid level tooltip
            if (isHovering(26, 16, 16, 54, mouseX, mouseY)) {
                if (fluidStack.getAmount() > 0) {
                    tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.fluid_level", fluidStack.getHoverName().getString(), fluidStack.getAmount() + "mB").getVisualOrderText());
                } else {
                    tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.empty").getVisualOrderText());
                }
            }
        }
        if (this.menu.blockEntity.isLit()) {
            if (isHovering(81, 38, 13, 13, mouseX, mouseY)) {
                tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.fuel_time", this.menu.blockEntity.litTime).getVisualOrderText());
            }
        }
        if (!tooltipList.isEmpty()) {
            guiGraphics.setTooltipForNextFrame(tooltipList, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        var canCharge = this.menu.blockEntity.generator.hasChargeSlot();
        var GUI = this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ?
                canCharge ? GUI_FLUID_CHARGING : GUI_FLUID : canCharge ? GUI_SOLID_CHARGING : GUI_SOLID;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, getLeftPos(), getTopPos(), 0.0F, 0.0F, this.getImageWidth(), this.getImageHeight(), 256, 256);

        // Burn progress
        if (this.menu.blockEntity.isLit()) {
            int progress = this.menu.getLitProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, getLeftPos() + 81, getTopPos() + 50 - progress, 176.0F, (float) (12 - progress), 14, progress, 256, 256);
        }

        // Draw energy level
        int energyLevel = (int) ((float) this.menu.blockEntity.energyHandler.getAmountAsInt() * 54f / (float) this.menu.blockEntity.energyHandler.getCapacityAsInt());
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, getLeftPos() + 134, getTopPos() + 70 - energyLevel, 176.0F, (float) (70 - energyLevel), 16, energyLevel + 1, 256, 256);

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            // Draw item tank
            FluidStack fluidStack = getFluid();
            if (fluidStack.getAmount() > 0) {
                FluidContainerUtil.renderFluidTank(guiGraphics, this, fluidStack, this.menu.blockEntity.fluidInventory.getCapacityAsInt(0, this.menu.blockEntity.fluidInventory.getResource(0)), 26, 16, 16, 54);
            }
        }
    }

    private FluidStack getFluid() {
        var inventory = this.menu.blockEntity.fluidInventory;
        return inventory.getResource(0).toStack(inventory.getAmountAsInt(0));
    }
}
