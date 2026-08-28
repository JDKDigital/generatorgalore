package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SolidFuelRecipeCategory implements IRecipeCategory<SolidFuelRecipe>
{
    private final IDrawable background;
    private final IDrawable icon;

    private static final int WIDTH = 126;
    private static final int HEIGHT = 70;

    public SolidFuelRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/jei/solid_fuel_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, JeiPlugin.categoryIcon(GeneratorUtil.FuelType.SOLID));
    }

    @Override
    public @NotNull RecipeType<SolidFuelRecipe> getRecipeType() {
        return JeiPlugin.SOLID_FUEL_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable(GeneratorGalore.MODID + ".recipe.solid_fuel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SolidFuelRecipe recipe, @NotNull IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 41)
                .addItemStack(recipe.generator())
                .setSlotName("generator");
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 41)
                .addItemStacks(recipe.fuels().stream().flatMap(ingredient -> Arrays.stream(ingredient.getItems())).toList())
                .setSlotName("fuels");
    }

    @Override
    public void draw(@NotNull SolidFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics poseStack, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        background.draw(poseStack);
        poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.rate", recipe.rate()), 37, 14, 4210752, false);
        poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.burn_time", recipe.consumptionRate()), 37, 32, 4210752, false);
        poseStack.drawString(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.total", (int) (recipe.rate() * recipe.consumptionRate())), 37, 50, 4210752, false);
    }
}
