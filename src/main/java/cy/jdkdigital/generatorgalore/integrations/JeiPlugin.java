package cy.jdkdigital.generatorgalore.integrations;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.recipe.FluidFuelRecipe;
import cy.jdkdigital.generatorgalore.common.recipe.SolidFuelRecipe;
import cy.jdkdigital.generatorgalore.init.ModTags;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    private static final ResourceLocation pluginId = new ResourceLocation(GeneratorGalore.MODID, GeneratorGalore.MODID);

    public static RecipeType<SolidFuelRecipe> SOLID_FUEL_RECIPE_TYPE = RecipeType.create(GeneratorGalore.MODID, "solid_fuels", SolidFuelRecipe.class);
    public static Map<GeneratorObject, RecipeType<SolidFuelRecipe>> FUEL_RECIPE_TYPES = new HashMap<>();
    public static Map<GeneratorObject, RecipeType<FluidFuelRecipe>> FLUID_FUEL_RECIPE_TYPES = new HashMap<>();

    public JeiPlugin() {
        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                FLUID_FUEL_RECIPE_TYPES.put(generator, RecipeType.create(GeneratorGalore.MODID, generator.getId().getPath() + "_fuels", FluidFuelRecipe.class));
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                if (generator.getId().getPath().equals("iron")) {
                    FUEL_RECIPE_TYPES.put(generator, SOLID_FUEL_RECIPE_TYPE);
                }
            } else{
                FUEL_RECIPE_TYPES.put(generator, RecipeType.create(GeneratorGalore.MODID, generator.getId().getPath() + "_fuels", SolidFuelRecipe.class));
            }
        });
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
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), FLUID_FUEL_RECIPE_TYPES.get(generator));
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), SOLID_FUEL_RECIPE_TYPE);
            } else {
                registration.addRecipeCatalyst(new ItemStack(generator.getBlockSupplier().get()), FUEL_RECIPE_TYPES.get(generator));
            }
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        AtomicBoolean hasRegisteredGeneric = new AtomicBoolean(false);
        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                registration.addRecipeCategories(new FluidFuelRecipeCategory(guiHelper, generator));
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                if (!hasRegisteredGeneric.get()) {
                    registration.addRecipeCategories(new SolidFuelRecipeCategory(guiHelper, GeneratorRegistry.generators.get(new ResourceLocation(GeneratorGalore.MODID, "iron"))));
                    hasRegisteredGeneric.set(true);
                }
            } else {
                registration.addRecipeCategories(new SolidFuelRecipeCategory(guiHelper, generator));
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
//        var vanillaFuelRecipes = FuelRecipeMaker.getFuelRecipes(registration.getIngredientManager());
        var foodList = registration.getIngredientManager().getAllIngredients(VanillaTypes.ITEM).stream().filter((stack) -> {
            FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
            if (foodProperties != null) {
                return true;
            }
            return false;
        }).toList();
        var enchantmentList = ForgeRegistries.ENCHANTMENTS.getValues().stream().map(enchantment -> {
            List<ItemStack> books = new ArrayList<>();
            IntStream.range(0, enchantment.getMaxLevel()).forEach(
                i -> books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i + 1)))
            );
            return books;
        }).flatMap(Collection::stream).toList();
        List<ItemStack> basePotions = PotionBrewing.ALLOWED_CONTAINERS.stream().flatMap(potionItem -> Arrays.stream(potionItem.getItems())).toList();
        var potionList = ForgeRegistries.POTIONS.getValues().stream().map(potion -> {
            List<ItemStack> potions = new ArrayList<>();
            if (potion != Potions.EMPTY) {
                for (ItemStack input : basePotions) {
                    ItemStack result = PotionUtils.setPotion(input.copy(), potion);
                    potions.add(result);
                }
            }
            return potions;
        }).flatMap(Collection::stream).toList();

        GeneratorRegistry.generators.forEach((resourceLocation, generator) -> {
            var genIngredient = Ingredient.of(generator.getBlockSupplier().get());
            String idPrefix = ForgeRegistries.BLOCKS.getKey(generator.getBlockSupplier().get()).getPath();
            AtomicInteger i = new AtomicInteger();

            if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                var fuelRecipes = new ArrayList<FluidFuelRecipe>();
                if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                    Optional<HolderSet.Named<Fluid>> fluids = Registry.FLUID.getTag(ModTags.getFluidTag(generator.getFuelTag()));
                    if (fluids.isPresent()) {
                        List<FluidStack> fluidStacks = fluids.stream().flatMap(holder -> holder.stream().map(fluidHolder -> new FluidStack(fluidHolder.value(), 10000))).toList();
                        fuelRecipes.add(new FluidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_fuels_" + (i.getAndIncrement())), fluidStacks, genIngredient, (float) generator.getGenerationRate(), (float) generator.getConsumptionRate()));
                    }
                }
                registration.addRecipes(FLUID_FUEL_RECIPE_TYPES.get(generator), fuelRecipes);
            } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID) && generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG) && generator.getFuelList() == null) {
                // Standard generator
                var fuelRecipes = new ArrayList<SolidFuelRecipe>();
//                vanillaFuelRecipes.forEach(fuelingRecipe -> {
//                    fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_" + ForgeRegistries.ITEMS.getKey(fuelingRecipe.getInputs().get(0).getItem()).getPath() + "_" + (i.getAndIncrement())), List.of(Ingredient.of(fuelingRecipe.getInputs().get(0))), genIngredient, (float) generator.getGenerationRate(), (int) (fuelingRecipe.getBurnTime() * generator.getConsumptionRate())));
//                });
                registration.addRecipes(SOLID_FUEL_RECIPE_TYPE, fuelRecipes);
            } else {
                var fuelRecipes = new ArrayList<SolidFuelRecipe>();
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.SOLID)) {
                    if (generator.getFuelList() != null) {
                        // Manual fuels item list
                        generator.getFuelList().forEach((itemId, fuel) -> {
                            fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_" + itemId.getPath() + "_" + (i.getAndIncrement())), List.of(Ingredient.of(ForgeRegistries.ITEMS.getValue(itemId))), genIngredient, fuel.rate(), fuel.burnTime()));
                        });
                    } else if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                        // Item tag
                        fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_fuels"), List.of(Ingredient.of(ModTags.getItemTag(generator.getFuelTag()))), genIngredient, (float) generator.getGenerationRate(), (int) generator.getConsumptionRate()));
                    }
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
                    foodList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculateFoodGenerationRate(generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_" + ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + "_" + (i.getAndIncrement())), List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                    enchantmentList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculateEnchantmentGenerationRate(generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_" + ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + "_" + (i.getAndIncrement())), List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                } else if (generator.getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
                    potionList.forEach((stack) -> {
                        var rate = GeneratorUtil.calculatePotionGenerationRate(generator, stack);
                        fuelRecipes.add(new SolidFuelRecipe(new ResourceLocation(GeneratorGalore.MODID, idPrefix + "_" + ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + "_" + (i.getAndIncrement())), List.of(Ingredient.of(stack)), genIngredient, rate.getFirst(), rate.getSecond()));
                    });
                }

                registration.addRecipes(FUEL_RECIPE_TYPES.get(generator), fuelRecipes);
            }
        });
    }
}
