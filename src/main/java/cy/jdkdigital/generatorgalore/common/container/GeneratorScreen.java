package cy.jdkdigital.generatorgalore.common.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu>
{
    private static final ResourceLocation GUI_SOLID = new ResourceLocation(GeneratorGalore.MODID, "textures/gui/container/generator_solid.png");
    private static final ResourceLocation GUI_FLUID = new ResourceLocation(GeneratorGalore.MODID, "textures/gui/container/generator_fluid.png");

    public GeneratorScreen(GeneratorMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull PoseStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title, 8.0F, 6.0F, 4210752);
        this.font.draw(matrixStack, this.playerInventoryTitle, 8.0F, (float) (this.getYSize() - 96 + 2), 4210752);

        this.menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
            int energyAmount = handler.getEnergyStored();

            // Energy level tooltip
            if (isHovering(134, 16, 16, 54, mouseX, mouseY)) {
                List<FormattedCharSequence> tooltipList = new ArrayList<>();
                tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.energy_level", energyAmount + "FE").getVisualOrderText());

                renderTooltip(matrixStack, tooltipList, mouseX - getGuiLeft(), mouseY - getGuiTop());
            }
        });

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FUEL_FLUID)) {
            this.menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack fluidStack = handler.getFluidInTank(0);

                // Fluid level tooltip
                if (isHovering(26, 16, 16, 54, mouseX, mouseY)) {
                    List<FormattedCharSequence> tooltipList = new ArrayList<>();

                    if (fluidStack.getAmount() > 0) {
                        tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.fluid_level", Component.translatable(fluidStack.getTranslationKey()).getString(), fluidStack.getAmount() + "mB").getVisualOrderText());
                    } else {
                        tooltipList.add(Component.translatable(GeneratorGalore.MODID + ".screen.empty").getVisualOrderText());
                    }

                    renderTooltip(matrixStack, tooltipList, mouseX - getGuiLeft(), mouseY - getGuiTop());
                }
            });
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderTexture(0, this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FUEL_FLUID) ? GUI_FLUID : GUI_SOLID);
        blit(poseStack, getGuiLeft(), getGuiTop(), 0, 0, this.getXSize(), this.getYSize());

        // Burn progress
        if (this.menu.blockEntity.isLit()) {
            int progress = this.menu.getLitProgress();
            this.blit(poseStack, getGuiLeft() + 81, getGuiTop() + 50 - progress, 176, 12 - progress, 14, progress);
        }

        // Draw energy level
        this.menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
            int energyLevel = (int) ((float) handler.getEnergyStored() * 54f / (float) handler.getMaxEnergyStored());
            blit(poseStack, getGuiLeft() + 134, getGuiTop() + 70 - energyLevel, 176, 70 - energyLevel, 16, energyLevel + 1);
        });

        if (this.menu.blockEntity.generator.getFuelType().equals(GeneratorUtil.FUEL_FLUID)) {
            // Draw fluid tank
            this.menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                FluidStack fluidStack = handler.getFluidInTank(0);
                if (fluidStack.getAmount() > 0) {
                    FluidContainerUtil.renderFluidTank(poseStack, this, fluidStack, handler.getTankCapacity(0), 26, 16, 16, 54, 0);
                }
            });
        }
    }
}