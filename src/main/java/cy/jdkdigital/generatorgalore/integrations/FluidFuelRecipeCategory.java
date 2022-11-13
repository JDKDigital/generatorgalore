package cy.jdkdigital.generatorgalore.integrations;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FluidFuelRecipeCategory implements IRecipeCategory<FluidFuelRecipe>
{
    private final IDrawable background;
    private final IDrawable icon;
    private final GeneratorObject generator;

    public FluidFuelRecipeCategory(IGuiHelper guiHelper, GeneratorObject generator) {
        this.generator = generator;
        ResourceLocation location = new ResourceLocation(GeneratorGalore.MODID, "textures/gui/jei/fluid_fuel_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, 126, 70);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratorGalore.MODID, this.generator.getId().getPath() + "_generator"))));
    }

    @Override
    public @NotNull RecipeType<FluidFuelRecipe> getRecipeType() {
        return JeiPlugin.FLUID_FUEL_RECIPE_TYPES.get(generator);
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable(GeneratorGalore.MODID + ".recipe.fluid_fuel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FluidFuelRecipe recipe, @NotNull IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 41)
                .addItemStacks(Arrays.asList(recipe.generator().getItems()))
                .setSlotName("generator");
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 3)
                .addIngredients(ForgeTypes.FLUID_STACK, recipe.fuels())
                .setFluidRenderer(10000, true, 16, 54)
                .setSlotName("fuels");
    }

    @Override
    public void draw(@NotNull FluidFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack poseStack, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.font.draw(poseStack, "Rate: " + recipe.rate() + "FE/t", 37, 14, 4210752);
        minecraft.font.draw(poseStack, "Burn rate: " + recipe.consumptionRate() + "mB/t", 37, 32, 4210752);
        minecraft.font.draw(poseStack, "Total: " + (int) (recipe.rate() * recipe.consumptionRate() *  100) + "FE/B", 37, 50, 4210752);
    }
}
