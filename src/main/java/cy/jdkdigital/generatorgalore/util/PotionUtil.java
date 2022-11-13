package cy.jdkdigital.generatorgalore.util;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.collect.SetMultiMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class PotionUtil
{
    public static final Map<String, Integer> brewingStepCache = new HashMap<>();
    private static final SetMultiMap<String, String> potionMap = new SetMultiMap<>();

    public static SetMultiMap<String, String> getPotionMap() {
        if (potionMap.allValues().isEmpty()) {
            Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
            brewingRecipes.stream()
                    .filter(VanillaBrewingRecipe.class::isInstance)
                    .map(VanillaBrewingRecipe.class::cast)
                    .findFirst()
                    .ifPresent(vanillaBrewingRecipe -> addVanillaBrewingRecipes(potionMap, vanillaBrewingRecipe));
            addModdedBrewingRecipes(brewingRecipes, potionMap);

            brewingStepCache.put(getUniquePotionName(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), 0);
        }
        return potionMap;
    }

    private static void addVanillaBrewingRecipes(SetMultiMap<String, String> potionMap, VanillaBrewingRecipe vanillaBrewingRecipe) {
        List<ItemStack> potionIngredients = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(PotionBrewing::isIngredient).toList();

        List<ItemStack> basePotions = PotionBrewing.ALLOWED_CONTAINERS.stream()
                .flatMap(potionItem -> Arrays.stream(potionItem.getItems()))
                .toList();

        Collection<ItemStack> knownPotions = new ArrayList<>();
        for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
            if (potion != Potions.EMPTY) {
                for (ItemStack input : basePotions) {
                    ItemStack result = PotionUtils.setPotion(input.copy(), potion);
                    knownPotions.add(result);
                }
            }
        }

        boolean foundNewPotions;
        do {
            List<ItemStack> newPotions = getNewPotions(knownPotions, potionIngredients, potionMap, vanillaBrewingRecipe);
            foundNewPotions = !newPotions.isEmpty();
            knownPotions.addAll(newPotions);
        } while (foundNewPotions);
    }

    private static List<ItemStack> getNewPotions(Collection<ItemStack> knownPotions, List<ItemStack> potionReagents, SetMultiMap<String, String> potionMap, VanillaBrewingRecipe vanillaBrewingRecipe) {
        List<ItemStack> newPotions = new ArrayList<>();
        for (ItemStack potionInput : knownPotions) {
            for (ItemStack potionReagent : potionReagents) {
                ItemStack potionOutput = vanillaBrewingRecipe.getOutput(potionInput.copy(), potionReagent);
                if (potionOutput.isEmpty()) {
                    continue;
                }

                if (potionInput.getItem() == potionOutput.getItem()) {
                    Potion potionOutputType = PotionUtils.getPotion(potionOutput);
                    if (potionOutputType == Potions.WATER) {
                        continue;
                    }

                    Potion potionInputType = PotionUtils.getPotion(potionInput);
                    ResourceLocation inputId = ForgeRegistries.POTIONS.getKey(potionInputType);
                    ResourceLocation outputId = ForgeRegistries.POTIONS.getKey(potionOutputType);
                    if (Objects.equals(inputId, outputId)) {
                        continue;
                    }
                }

                // Add to potion map
                potionMap.put(getUniquePotionName(potionOutput), getUniquePotionName(potionInput));
            }
        }
        return newPotions;
    }

    private static void addModdedBrewingRecipes(Collection<IBrewingRecipe> brewingRecipes, SetMultiMap<String, String> potionMap) {
        for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
            if (iBrewingRecipe instanceof BrewingRecipe brewingRecipe) {
                ItemStack[] ingredients = brewingRecipe.getIngredient().getItems();
                if (ingredients.length > 0) {
                    Ingredient inputIngredient = brewingRecipe.getInput();
                    ItemStack output = brewingRecipe.getOutput();
                    ItemStack[] inputs = inputIngredient.getItems();
                    // Add to potion map
                    for (ItemStack input: inputs) {
                        potionMap.put(getUniquePotionName(output), getUniquePotionName(input));
                    }
                }
            }
        }
    }

    public static String getUniquePotionName(ItemStack stack) {
        StringBuilder potionUid = new StringBuilder(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.getTag() != null) {
            potionUid.append(stack.getTag().getString("Potion"));
        } else {
            var potion = PotionUtils.getPotion(stack);
            potionUid.append(potion.getName(""));
        }
        return potionUid.toString();
    }
}
