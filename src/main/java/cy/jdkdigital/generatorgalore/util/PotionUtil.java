package cy.jdkdigital.generatorgalore.util;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.collection.SetMultiMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

import java.util.*;
import java.util.stream.Collectors;

public class PotionUtil
{
    public static final Map<String, Integer> brewingStepCache = new HashMap<>();
    private static final SetMultiMap<String, String> potionMap = new SetMultiMap<>();

    public static SetMultiMap<String, String> getPotionMap(Level level) {
        if (potionMap.allValues().isEmpty() && level instanceof ServerLevel serverLevel) {

            List<IBrewingRecipe> brewingRecipes = serverLevel.potionBrewing().getRecipes();
            brewingRecipes.stream()
                    .filter(Objects::nonNull)
                    .map(IBrewingRecipe.class::cast)
                    .findFirst()
                    .ifPresent(vanillaBrewingRecipe -> addVanillaBrewingRecipes(potionMap, vanillaBrewingRecipe));
            addModdedBrewingRecipes(brewingRecipes, potionMap);
        }
        return potionMap;
    }

    private static void addVanillaBrewingRecipes(SetMultiMap<String, String> potionMap, IBrewingRecipe vanillaBrewingRecipe) {
        List<ItemStack> potionIngredients = BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(PotionBrewing.EMPTY::isIngredient).toList();

        List<Item> basePotions = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

        List<ItemStack> knownPotions = BuiltInRegistries.POTION.holders().map(potion -> {
            List<ItemStack> potions = new ArrayList<>();
            if (potion.value().getEffects().size() > 0) {
                for (Item input : basePotions) {
                    ItemStack result = new ItemStack(input);
                    result.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                    potions.add(result);
                }
            }
            return potions;
        }).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));

        boolean foundNewPotions;
        do {
            List<ItemStack> newPotions = getNewPotions(knownPotions, potionIngredients, potionMap, vanillaBrewingRecipe);
            foundNewPotions = !newPotions.isEmpty();
            knownPotions.addAll(newPotions);
        } while (foundNewPotions);
    }

    private static List<ItemStack> getNewPotions(Collection<ItemStack> knownPotions, List<ItemStack> potionReagents, SetMultiMap<String, String> potionMap, IBrewingRecipe vanillaBrewingRecipe) {
        List<ItemStack> newPotions = new ArrayList<>();
        for (ItemStack potionInput : knownPotions) {
            for (ItemStack potionReagent : potionReagents) {
                ItemStack potionOutput = vanillaBrewingRecipe.getOutput(potionInput.copy(), potionReagent);
                if (potionOutput.isEmpty()) {
                    continue;
                }

                if (potionInput.getItem() == potionOutput.getItem()) {
                    var potionOutputType = potionOutput.get(DataComponents.POTION_CONTENTS);
                    if (potionOutputType.potion().get().equals(Potions.WATER)) {
                        continue;
                    }

                    var potionInputType = potionInput.get(DataComponents.POTION_CONTENTS);
                    if (potionInputType.is(potionOutputType.potion().get())) {
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
        StringBuilder potionUid = new StringBuilder(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            var potionData = stack.get(DataComponents.POTION_CONTENTS);
            if (potionData != null) {
                potionData.getAllEffects().forEach(mobEffectInstance -> potionUid.append(mobEffectInstance.getDescriptionId()));
            }
        }
        return potionUid.toString();
    }
}
