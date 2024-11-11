package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.init.ModTags;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
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

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var vanillaFuelRecipes = FuelRecipeMaker.getFuelRecipes(registration.getIngredientManager());
        var foodList = registration.getIngredientManager().getAllItemStacks().stream().filter((stack) -> {
            FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
            return foodProperties != null;
        }).toList();
        var enchantmentList = RegistryUtil.getRegistry(Registries.ENCHANTMENT).holders().map(enchantment -> {
            List<ItemStack> books = new ArrayList<>();
            IntStream.range(0, enchantment.value().getMaxLevel()).forEach(
                i -> books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i + 1)))
            );
            return books;
        }).flatMap(Collection::stream).toList();
        List<Item> basePotions = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
        var potionList = BuiltInRegistries.POTION.holders().map(potion -> {
            List<ItemStack> potions = new ArrayList<>();
            if (potion.value().getEffects().size() > 0) {
                for (Item input : basePotions) {
                    ItemStack result = new ItemStack(input);
                    result.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    potions.add(result);
                }
            }
            return potions;
        }).flatMap(Collection::stream).toList();

        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            var genIngredient = Ingredient.of(generator.getBlockSupplier().get());
            String idPrefix = BuiltInRegistries.BLOCK.getKey(generator.getBlockSupplier().get()).getPath();
            AtomicInteger i = new AtomicInteger();

            if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                var fuelRecipes = new ArrayList<FluidFuelRecipe>();
                if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                    var fluids = BuiltInRegistries.FLUID.getTag(ModTags.getFluidTag(generator.getFuelTag()));
                    if (fluids.isPresent()) {
                        List<FluidStack> fluidStacks = fluids.get().stream().map(fluid -> new FluidStack(fluid, 10000)).toList();
                        fuelRecipes.add(new FluidFuelRecipe(fluidStacks, genIngredient, (float) generator.getGenerationRate(), (float) generator.getConsumptionRate()));
                    }
                }
                registration.addRecipes(FLUID_FUEL_RECIPE_TYPE, fuelRecipes);
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                // Standard generator
                var fuelRecipes = new ArrayList<SolidFuelRecipe>();
                vanillaFuelRecipes.forEach(fuelingRecipe -> {
                    fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(fuelingRecipe.getInputs().get(0))), genIngredient, (float) generator.getGenerationRate(), (int) (fuelingRecipe.getBurnTime() * generator.getConsumptionRate())));
                });
                registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
            } else {
                var fuelRecipes = new ArrayList<SolidFuelRecipe>();
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID)) {
                    if (generator.getFuelList() != null) {
                        // Manual fuels item list
                        generator.getFuelList().forEach((itemId, fuel) -> {
                            fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(BuiltInRegistries.ITEM.get(itemId))), genIngredient, fuel.rate(), fuel.burnTime()));
                        });
                    } else if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                        // Item tag
                        fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(ModTags.getItemTag(generator.getFuelTag()))), genIngredient, (float) generator.getGenerationRate(), (int) generator.getConsumptionRate()));
                    }
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
                    foodList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculateFoodGenerationRate(generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                    enchantmentList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculateEnchantmentGenerationRate(generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
                    potionList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculatePotionGenerationRate(Minecraft.getInstance().level, generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                }

                registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
            }
        });
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//        registration.addRecipeClickArea(GeneratorScreen.class, 35, 35, 24, 16, SOLID_FUEL_RECIPE_TYPE);
    }
}
