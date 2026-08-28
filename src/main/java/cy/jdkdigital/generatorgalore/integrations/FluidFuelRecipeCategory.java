package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FluidFuelRecipeCategory implements IRecipeCategory<FluidFuelRecipe>
{
    private final IDrawable background;
    private final IDrawable icon;

    private static final int WIDTH = 126;
    private static final int HEIGHT = 70;

    public FluidFuelRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/jei/fluid_fuel_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, JeiPlugin.categoryIcon(GeneratorUtil.FuelType.FLUID));
    }

    @Override
    public @NotNull RecipeType<FluidFuelRecipe> getRecipeType() {
        return JeiPlugin.FLUID_FUEL_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable(GeneratorGalore.MODID + ".recipe.fluid_fuel");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidFuelRecipe recipe, @NotNull IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 41)
                .addItemStack(recipe.generator())
                .setSlotName("generator");
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 3)
                .addIngredients(NeoForgeTypes.FLUID_STACK, recipe.fuels())
                .setFluidRenderer(1000, false, 16, 54)
                .setSlotName("fuels");
    }

    @Override
    public void draw(@NotNull FluidFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics poseStack, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        background.draw(poseStack);
        poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.rate", recipe.rate()), 37, 14, 4210752, false);
        poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.burn_rate", recipe.consumptionRate()), 37, 32, 4210752, false);
        if (recipe.consumptionRate() > 0) {
            poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.total_bucket", (int) (recipe.rate() / recipe.consumptionRate() * 1000)), 37, 50, 4210752, false);
        }
    }
}
