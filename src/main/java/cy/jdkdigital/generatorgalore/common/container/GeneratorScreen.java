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

    // Smooth energy bar interpolation variables
    private int lastEnergy = 0;
    private int clientEnergy = 0;
    private boolean firstEnergyRender = true;

    // Smooth fluid level interpolation variables
    private int lastFluidAmount = 0;
    private int clientFluidAmount = 0;
    private boolean firstFluidRender = true;

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

        // Energy level tooltip - use current energy for tooltip
        int currentEnergy = this.menu.blockEntity.energyHandler.getEnergyStored();
        if (isHovering(134, 16, 16, 54, mouseX, mouseY)) {
            tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.energy_level", currentEnergy + "/" + this.menu.blockEntity.energyHandler.getMaxEnergyStored() + "FE").getVisualOrderText());
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

        // Update energy interpolation
        int currentEnergy = this.menu.blockEntity.energyHandler.getEnergyStored();
        int maxEnergy = this.menu.blockEntity.energyHandler.getMaxEnergyStored();

        // Initialize interpolation variables on first render to prevent overshooting
        if (firstEnergyRender) {
            lastEnergy = currentEnergy;
            clientEnergy = currentEnergy;
            firstEnergyRender = false;
        }

        // Store last energy value and interpolate for smooth animation
        if (lastEnergy != currentEnergy) {
            clientEnergy = lastEnergy;
            lastEnergy = currentEnergy;
        }

        // Calculate interpolated energy level with partial ticks for smooth animation
        // Use float calculation for precision and round to nearest integer
        float interpolatedEnergy = clientEnergy + (lastEnergy - clientEnergy) * partialTicks;
        int energyLevel = Math.round((interpolatedEnergy / maxEnergy) * 54f);

        // Burn progress - use vanilla flame animation
        if (this.menu.blockEntity.isLit()) {
            int progress = this.menu.getLitProgress();
            // Use correct vanilla furnace flame sprite
            guiGraphics.blitSprite(ResourceLocation.withDefaultNamespace("container/furnace/lit_progress"), 14, 14, 0, 0, getGuiLeft() + 81, getGuiTop() + 50 - progress, 14, progress);
        }

        // Realtime energy bar rendering - replaces legacy blit calls
        if (maxEnergy > 0) {
            // Calculate fill height: (current * totalHeight) / max
            int fillHeight = (int) (((float) currentEnergy / maxEnergy) * 54f);

            // Main energy bar (red) - grows from bottom up
            if (fillHeight > 0) {
                guiGraphics.fill(
                    getGuiLeft() + 134, getGuiTop() + 70 - fillHeight,
                    getGuiLeft() + 134 + 16, getGuiTop() + 70,
                    0xFFFF0000 // Redstone red
                );
            }

            // Left accent stripe (1 pixel, lighter red)
            if (fillHeight > 0) {
                guiGraphics.fill(
                    getGuiLeft() + 134, getGuiTop() + 70 - fillHeight,
                    getGuiLeft() + 134 + 1, getGuiTop() + 70,
                    0xFFFF5555 // Lighter red accent
                );
            }
        }

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            FluidStack fluidStack = this.menu.blockEntity.fluidInventory.getFluidInTank(0);
            int currentFluidAmount = fluidStack.getAmount();
            int fluidCapacity = this.menu.blockEntity.fluidInventory.getTankCapacity(0);

            // Initialize interpolation variables on first render to prevent overshooting
            if (firstFluidRender) {
                lastFluidAmount = currentFluidAmount;
                clientFluidAmount = currentFluidAmount;
                firstFluidRender = false;
            }

            // Update fluid interpolation for smooth animation
            if (lastFluidAmount != currentFluidAmount) {
                clientFluidAmount = lastFluidAmount;
                lastFluidAmount = currentFluidAmount;
            }

            // Calculate interpolated fluid amount with partial ticks for smooth animation
            float interpolatedFluidAmount = clientFluidAmount + (lastFluidAmount - clientFluidAmount) * partialTicks;

            if (currentFluidAmount > 0) {
                // Calculate fill height: (current * totalHeight) / max
                int fluidFillHeight = (int) (((float) currentFluidAmount / fluidCapacity) * 54f);

                // Try to get fluid color dynamically with precise lava detection
                int fluidColor = 0xFF3663D9; // Default blue
                if (!fluidStack.isEmpty()) {
                    try {
                        // Try to get fluid color using the same method as FluidContainerUtil
                        var attributes = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluidStack.getFluid());
                        int tintColor = attributes.getTintColor();

                        // Precise lava color detection
                        if (fluidStack.getFluid().getFluidType().toString().contains("lava") || tintColor == -1) {
                            tintColor = 0xFFD45A12; // Strong lava orange
                        }

                        if (tintColor != 0xFFFFFFFF) { // If not white (default), use it
                            fluidColor = tintColor;
                        }
                        // Ensure alpha channel is set
                        fluidColor = (fluidColor & 0x00FFFFFF) | 0xFF000000;
                    } catch (Exception e) {
                        // Fallback to blue if color extraction fails
                        fluidColor = 0xFF3663D9;
                    }
                }

                // Realtime fluid bar rendering - grows from bottom up
                if (fluidFillHeight > 0) {
                    guiGraphics.fill(
                        getGuiLeft() + 26, getGuiTop() + 70 - fluidFillHeight,
                        getGuiLeft() + 26 + 16, getGuiTop() + 70,
                        fluidColor
                    );
                }
            }
        }
    }
}
