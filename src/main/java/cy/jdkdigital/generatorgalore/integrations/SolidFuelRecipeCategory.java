package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SolidFuelRecipeCategory implements IRecipeCategory<SolidFuelRecipe>
{
    private final IDrawable background;
    private final IDrawable icon;

    public SolidFuelRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "textures/gui/jei/solid_fuel_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, 126, 70);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "iron")).getBlockSupplier().get()));
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
    public @NotNull IDrawable getBackground() {
        return background;
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
        poseStack.drawString(minecraft.font, "Rate: " + recipe.rate() + "FE/t", 37F, 14F, 4210752, false);
        poseStack.drawString(minecraft.font, "Burntime: " + recipe.consumptionRate(), 37F, 32F, 4210752, false);
        poseStack.drawString(minecraft.font, "Total: " + (int) (recipe.rate() * recipe.consumptionRate()) + "FE", 37F, 50F, 4210752, false);
    }
}
