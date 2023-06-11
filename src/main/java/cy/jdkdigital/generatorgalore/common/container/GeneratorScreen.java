package cy.jdkdigital.generatorgalore.common.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu>
{
    private static final ResourceLocation GUI_SOLID = new ResourceLocation(GeneratorGalore.MODID, "textures/gui/container/generator_solid.png");
    private static final ResourceLocation GUI_FLUID = new ResourceLocation(GeneratorGalore.MODID, "textures/gui/container/generator_fluid.png");

    public GeneratorScreen(GeneratorMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, this.title.getString(), 8.0F, 6.0F, 4210752, false);
        guiGraphics.drawString(font, this.playerInventoryTitle.getString(), 8.0F, (float) (this.getYSize() - 96 + 2), 4210752, false);

        guiGraphics.drawString(font, Component.translatable(GeneratorGalore.MODID + ".screen.generation_rate", this.menu.blockEntity.generator.getGenerationRate()).getString(), this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? 51.0F : 8.0F, 24.0F, 4210752, false);

        List<FormattedCharSequence> tooltipList = new ArrayList<>();
        this.menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
            int energyAmount = handler.getEnergyStored();

            // Energy level tooltip
            if (isHovering(134, 16, 16, 54, mouseX, mouseY)) {
                tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.energy_level", energyAmount + "FE").getVisualOrderText());
            }
        });

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            this.menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack fluidStack = handler.getFluidInTank(0);

                // Fluid level tooltip
                if (isHovering(26, 16, 16, 54, mouseX, mouseY)) {
                    if (fluidStack.getAmount() > 0) {
                        tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.fluid_level", Component.translatable(fluidStack.getTranslationKey()).getString(), fluidStack.getAmount() + "mB").getVisualOrderText());
                    } else {
                        tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.empty").getVisualOrderText());
                    }
                }
            });
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
        var GUI = this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? GUI_FLUID : GUI_SOLID;
        guiGraphics.blit(GUI, getGuiLeft(), getGuiTop(), 0, 0, this.getXSize(), this.getYSize());

        // Burn progress
        if (this.menu.blockEntity.isLit()) {
            int progress = this.menu.getLitProgress();
            guiGraphics.blit(GUI, getGuiLeft() + 81, getGuiTop() + 50 - progress, 176, 12 - progress, 14, progress);
        }

        // Draw energy level
        this.menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
            int energyLevel = (int) ((float) handler.getEnergyStored() * 54f / (float) handler.getMaxEnergyStored());
            guiGraphics.blit(GUI, getGuiLeft() + 134, getGuiTop() + 70 - energyLevel, 176, 70 - energyLevel, 16, energyLevel + 1);
        });

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            // Draw fluid tank
            this.menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack fluidStack = handler.getFluidInTank(0);
                if (fluidStack.getAmount() > 0) {
                    FluidContainerUtil.renderFluidTank(guiGraphics, this, fluidStack, handler.getTankCapacity(0), 26, 16, 16, 54, 0);
                }
            });
        }
    }
}
