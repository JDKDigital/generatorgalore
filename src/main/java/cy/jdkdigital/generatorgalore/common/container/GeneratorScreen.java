package cy.jdkdigital.generatorgalore.common.container;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.FluidContainerUtil;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu>
{
    private static final ResourceLocation GUI_SOLID = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_solid.png");
    private static final ResourceLocation GUI_FLUID = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_fluid.png");
    private static final ResourceLocation GUI_SOLID_CHARGING = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_solid_charging.png");
    private static final ResourceLocation GUI_FLUID_CHARGING = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/container/generator_fluid_charging.png");

    public GeneratorScreen(GeneratorMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, this.title.getString(), 8.0F, 6.0F, 4210752, false);
        guiGraphics.drawString(font, this.playerInventoryTitle.getString(), 8.0F, (float) (this.getYSize() - 96 + 2), 4210752, false);

        guiGraphics.drawString(font, Component.translatable(GeneratorGalore.MODID + ".screen.generation_rate", this.menu.blockEntity.getGenerationRate()).getString(), this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? 51.0F : 8.0F, 24.0F, 4210752, false);

        List<FormattedCharSequence> tooltipList = new ArrayList<>();
        int energyAmount = this.menu.blockEntity.energyHandler.getEnergyStored();

        // Energy level tooltip
        if (isHovering(134, 16, 16, 54, mouseX, mouseY)) {
            tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.energy_level", energyAmount + "/" + this.menu.blockEntity.energyHandler.getMaxEnergyStored() + "FE").getVisualOrderText());
        }

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            FluidStack fluidStack = this.menu.blockEntity.fluidInventory.getFluidInTank(0);

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
        guiGraphics.renderTooltip(font, tooltipList, mouseX - getGuiLeft(), mouseY - getGuiTop());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        var canCharge = this.menu.blockEntity.generator.hasChargeSlot();
        var GUI = this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ?
                canCharge ? GUI_FLUID_CHARGING : GUI_FLUID : canCharge ? GUI_SOLID_CHARGING : GUI_SOLID;
        guiGraphics.blit(GUI, getGuiLeft(), getGuiTop(), 0, 0, this.getXSize(), this.getYSize());

        // Burn progress
        if (this.menu.blockEntity.isLit()) {
            int progress = this.menu.getLitProgress();
            guiGraphics.blit(GUI, getGuiLeft() + 81, getGuiTop() + 50 - progress, 176, 12 - progress, 14, progress);
        }

        // Draw energy level
        int energyLevel = (int) ((float) this.menu.blockEntity.energyHandler.getEnergyStored() * 54f / (float) this.menu.blockEntity.energyHandler.getMaxEnergyStored());
        guiGraphics.blit(GUI, getGuiLeft() + 134, getGuiTop() + 70 - energyLevel, 176, 70 - energyLevel, 16, energyLevel + 1);

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            // Draw item tank
            FluidStack fluidStack = this.menu.blockEntity.fluidInventory.getFluidInTank(0);
            if (fluidStack.getAmount() > 0) {
                FluidContainerUtil.renderFluidTank(guiGraphics, this, fluidStack, this.menu.blockEntity.fluidInventory.getTankCapacity(0), 26, 16, 16, 54, 100);
            }
        }
    }
}
