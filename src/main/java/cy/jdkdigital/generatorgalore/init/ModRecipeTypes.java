package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = GeneratorGalore.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModRecipeTypes
{
    public static final RegistryObject<RecipeSerializer<?>> SOLID_FUEL = GeneratorGalore.RECIPE_SERIALIZERS.register("solid_fuel", () -> new SolidFuelRecipe.Serializer<>(SolidFuelRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> FLUID_FUEL = GeneratorGalore.RECIPE_SERIALIZERS.register("fluid_fuel", () -> new FluidFuelRecipe.Serializer<>(FluidFuelRecipe::new));

    public static RegistryObject<RecipeType<SolidFuelRecipe>> SOLID_FUEL_TYPE = registerRecipeType("solid_fuel");
    public static RegistryObject<RecipeType<FluidFuelRecipe>> FLUID_FUEL_TYPE = registerRecipeType("fluid_fuel");

    static <T extends Recipe<Container>> RegistryObject<RecipeType<T>> registerRecipeType(final String name) {
        return GeneratorGalore.RECIPE_TYPES.register(name, () -> new RecipeType<T>() {
            public String toString() {
                return GeneratorGalore.MODID + ":" + name;
            }
        });
    }
}
