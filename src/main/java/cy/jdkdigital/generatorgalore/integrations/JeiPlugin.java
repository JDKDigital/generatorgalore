package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.Config;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.datamap.SolidFuelMap;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.init.ModTags;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.plugins.vanilla.cooking.fuel.FuelRecipeMaker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    private static final ResourceLocation pluginId = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, GeneratorGalore.MODID);

    public static RecipeType<SolidFuelRecipe> SOLID_FUEL_RECIPE_TYPE = RecipeType.create(GeneratorGalore.MODID, "solid_fuels", SolidFuelRecipe.class);
    public static RecipeType<FluidFuelRecipe> FLUID_FUEL_RECIPE_TYPE = RecipeType.create(GeneratorGalore.MODID, "fluid_fuels", FluidFuelRecipe.class);

    public JeiPlugin() {
    }

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return pluginId;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), FLUID_FUEL_RECIPE_TYPE);
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), SOLID_FUEL_RECIPE_TYPE);
            } else {
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), SOLID_FUEL_RECIPE_TYPE);
            }
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new FluidFuelRecipeCategory(guiHelper));
        registration.addRecipeCategories(new SolidFuelRecipeCategory(guiHelper));
    }

    static List<IJeiFuelingRecipe> vanillaFuelRecipes;
    static List<ItemStack> foodList;
    static List<ItemStack> enchantmentList;
    static List<SolidFuelMap.SolidFuel> potionList;
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        vanillaFuelRecipes = FuelRecipeMaker.getFuelRecipes(registration.getIngredientManager());
        foodList = registration.getIngredientManager().getAllItemStacks().stream().filter((stack) -> {
            FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
            return foodProperties != null;
        }).toList();
        enchantmentList = RegistryUtil.getRegistry(Registries.ENCHANTMENT).holders().map(enchantment -> {
            List<ItemStack> books = new ArrayList<>();
            IntStream.range(0, enchantment.value().getMaxLevel()).forEach(
                i -> books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i + 1)))
            );
            return books;
        }).flatMap(Collection::stream).toList();
        List<Item> basePotions = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
        potionList = GeneratorUtil.getPotionFuels(Minecraft.getInstance().level.registryAccess());

        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            addGeneratorFuelRecipes(registration, generator, generator.getBlockSupplier().get().asItem().getDefaultInstance(), 1);
            if (generator.has8x()) {
                addGeneratorFuelRecipes(registration, generator, BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(generator.getBlockSupplier().get()).withPath(p -> p + "_8x")).getDefaultInstance(), 8);
            }
            if (generator.has64x()) {
                addGeneratorFuelRecipes(registration, generator, BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(generator.getBlockSupplier().get()).withPath(p -> p + "_64x")).getDefaultInstance(), 64);
            }
        });
    }

    private void addGeneratorFuelRecipes(IRecipeRegistration registration, GeneratorObject generator, ItemStack genIngredient, int modifier) {
        var solidFuelData = generator.getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.SOLID_FUEL_MAP);
        var fluidFuelData = generator.getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.FLUID_FUEL_MAP);

        float consumptionModifier = Config.SERVER.increasedConsumption.get() ? modifier : 1;

        if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) && fluidFuelData == null) {
            var fuelRecipes = new ArrayList<FluidFuelRecipe>();
            if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                var fluids = BuiltInRegistries.FLUID.getTag(ModTags.getFluidTag(generator.getFuelTag()));
                if (fluids.isPresent()) {
                    List<FluidStack> fluidStacks = fluids.get().stream().map(fluid -> new FluidStack(fluid, 10000)).toList();
                    fuelRecipes.add(new FluidFuelRecipe(fluidStacks, genIngredient, (float) generator.getGenerationRate() * modifier, (float) generator.getConsumptionRate() * consumptionModifier));
                }
            }
            registration.addRecipes(FLUID_FUEL_RECIPE_TYPE, fuelRecipes);
        } else if (fluidFuelData != null) {
            // Datamap fluid fuel generator
            var fuelRecipes = new ArrayList<FluidFuelRecipe>();
            fluidFuelData.fuels().forEach(fuel -> {
                fuelRecipes.add(new FluidFuelRecipe(List.of(fuel.fluid().getStacks()), genIngredient, (float) fuel.generationRate() * modifier, (float) fuel.consumptionRate() * consumptionModifier));
            });
            registration.addRecipes(FLUID_FUEL_RECIPE_TYPE, fuelRecipes);
        } else if (solidFuelData != null) {
            // Datamap solid fuel generator
            var fuelRecipes = new ArrayList<SolidFuelRecipe>();
            solidFuelData.fuels().forEach(fuel -> {
                fuelRecipes.add(new SolidFuelRecipe(List.of(fuel.item()), genIngredient, (float) fuel.generationRate() * modifier, fuel.burnTime() / consumptionModifier));
            });
            registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
        } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
            // Standard generator
            var fuelRecipes = new ArrayList<SolidFuelRecipe>();
            vanillaFuelRecipes.forEach(fuelingRecipe -> {
                fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(fuelingRecipe.getInputs().get(0))), genIngredient, (float) generator.getGenerationRate() * modifier, (int) (fuelingRecipe.getBurnTime() * generator.getConsumptionRate() / consumptionModifier)));
            });
            registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
        } else {
            var fuelRecipes = new ArrayList<SolidFuelRecipe>();
            if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID)) {
                if (generator.getFuelList() != null) {
                    // Manual fuels item list
                    generator.getFuelList().forEach((itemId, fuel) -> {
                        fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(BuiltInRegistries.ITEM.get(itemId))), genIngredient, fuel.rate() * modifier, fuel.burnTime() / consumptionModifier));
                    });
                } else if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                    // Item tag
                    fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(ModTags.getItemTag(generator.getFuelTag()))), genIngredient, (float) generator.getGenerationRate() * modifier, (int) generator.getConsumptionRate() * consumptionModifier));
                }
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
                foodList.forEach((stack) -> {
                    var rate = GeneratorUtil.calculateFoodGenerationRate(generator, stack);
                    fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(stack)), genIngredient, rate.getFirst() * modifier, rate.getSecond() / consumptionModifier));
                });
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                enchantmentList.forEach((stack) -> {
                    var rate = GeneratorUtil.calculateEnchantmentGenerationRate(generator, stack);
                    fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(stack)), genIngredient, rate.getFirst() * modifier, rate.getSecond() / consumptionModifier));
                });
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
                potionList.forEach((fuel) -> {
                    fuelRecipes.add(new SolidFuelRecipe(List.of(fuel.item()), genIngredient, fuel.generationRate() * modifier, fuel.consumptionRate() / consumptionModifier));
                });
            }

            registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//        registration.addRecipeClickArea(GeneratorScreen.class, 35, 35, 24, 16, SOLID_FUEL_RECIPE_TYPE);
    }
}
