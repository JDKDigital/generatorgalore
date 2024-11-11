package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipeTypes
{
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SOLID_FUEL = GeneratorGalore.RECIPE_SERIALIZERS.register("solid_fuel", SolidFuelRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> FLUID_FUEL = GeneratorGalore.RECIPE_SERIALIZERS.register("fluid_fuel", FluidFuelRecipe.Serializer::new);

    public static DeferredHolder<RecipeType<?>, RecipeType<SolidFuelRecipe>> SOLID_FUEL_TYPE = registerRecipeType("solid_fuel");
    public static DeferredHolder<RecipeType<?>, RecipeType<FluidFuelRecipe>> FLUID_FUEL_TYPE = registerRecipeType("fluid_fuel");

    static <T extends Recipe<RecipeInput>> DeferredHolder<RecipeType<?>, RecipeType<T>> registerRecipeType(final String name) {
        return GeneratorGalore.RECIPE_TYPES.register(name, () -> new RecipeType<T>() {
            public String toString() {
                return GeneratorGalore.MODID + ":" + name;
            }
        });
    }
}
