package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class SolidFuelRecipeCategory implements IRecipeCategory<SolidFuelRecipe>
{
    private final IDrawable background;
    private final IDrawable icon;

    private static final int WIDTH = 126;
    private static final int HEIGHT = 70;

    public SolidFuelRecipeCategory(IGuiHelper guiHelper) {
        Identifier location = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/jei/solid_fuel_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(JeiPlugin.categoryIcon(GeneratorUtil.FuelType.SOLID));
    }

    @Override
    public @NotNull IRecipeType<SolidFuelRecipe> getRecipeType() {
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
                .addItemStacks(recipe.fuels().stream().flatMap(ingredient -> ingredient.items().map(holder -> new ItemStack(holder.value()))).toList())
                .setSlotName("fuels");
    }

    @Override
    public void draw(@NotNull SolidFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor poseStack, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        background.draw(poseStack);
        poseStack.text(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.rate", recipe.rate()), 37, 14, 0xFF404040, false);
        poseStack.text(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.burn_time", recipe.consumptionRate()), 37, 32, 0xFF404040, false);
        poseStack.text(minecraft.font, Component.translatable(GeneratorGalore.MODID + ".recipe.total", (int) (recipe.rate() * recipe.consumptionRate())), 37, 50, 0xFF404040, false);
    }
}
