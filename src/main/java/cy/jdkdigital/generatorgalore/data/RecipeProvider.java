package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;

import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider
{
    public RecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        RecipeOutput pRecipeOutput = this.output;
        var copperGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "copper"));
        shaped(RecipeCategory.MISC, copperGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(Items.FURNACE), has(Items.FURNACE))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_COPPER)
                .define('G', Items.FURNACE)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(copperGenerator.getBlockSupplier().get())), prefixedRecipeId(copperGenerator.getBlockSupplier().get(), "generators/"));

        var ironGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "iron"));
        shaped(RecipeCategory.MISC, ironGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(copperGenerator.getBlockSupplier().get()), has(copperGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Ingredient.of(copperGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(ironGenerator.getBlockSupplier().get())), prefixedRecipeId(ironGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, ironGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(copperGenerator.getBlockSupplier().get()), has(copperGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(ironGenerator.getUpgradeSupplier().get())), prefixedRecipeId(ironGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var goldGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "gold"));
        shaped(RecipeCategory.MISC, goldGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(ironGenerator.getBlockSupplier().get()), has(ironGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('G', Ingredient.of(ironGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(goldGenerator.getBlockSupplier().get())), prefixedRecipeId(goldGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, goldGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(ironGenerator.getBlockSupplier().get()), has(ironGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(goldGenerator.getUpgradeSupplier().get())), prefixedRecipeId(goldGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var culinaryGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "culinary"));
        shaped(RecipeCategory.MISC, culinaryGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("ICI").pattern("IGI").pattern("ERE")
                .define('I', Tags.Items.CROPS)
                .define('C', Items.CAKE)
                .define('E', Tags.Items.EGGS)
                .define('G', Ingredient.of(goldGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(culinaryGenerator.getBlockSupplier().get())), prefixedRecipeId(culinaryGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, culinaryGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("ICI").pattern("IFI").pattern("ERE")
                .define('I', Tags.Items.CROPS)
                .define('C', Items.CAKE)
                .define('E', Tags.Items.EGGS)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(culinaryGenerator.getUpgradeSupplier().get())), prefixedRecipeId(culinaryGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var potionGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "potion"));
        shaped(RecipeCategory.MISC, potionGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("BRB")
                .define('I', Tags.Items.DYED_PINK)
                .define('B', Items.BREWING_STAND)
                .define('G', Ingredient.of(culinaryGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(potionGenerator.getBlockSupplier().get())), prefixedRecipeId(potionGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, potionGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("BRB")
                .define('I', Tags.Items.DYED_PINK)
                .define('B', Items.BREWING_STAND)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(potionGenerator.getUpgradeSupplier().get())), prefixedRecipeId(potionGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var honeyGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "honey"));
        shaped(RecipeCategory.MISC, honeyGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("BRB")
                .define('I', Items.HONEY_BLOCK)
                .define('B', Items.BUCKET)
                .define('G', Ingredient.of(culinaryGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(honeyGenerator.getBlockSupplier().get())), prefixedRecipeId(honeyGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, honeyGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("BRB")
                .define('I', Items.HONEY_BLOCK)
                .define('B', Items.BUCKET)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(honeyGenerator.getUpgradeSupplier().get())), prefixedRecipeId(honeyGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var diamondGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "diamond"));
        shaped(RecipeCategory.MISC, diamondGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.GEMS_DIAMOND)
                .define('G', Ingredient.of(goldGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(diamondGenerator.getBlockSupplier().get())), prefixedRecipeId(diamondGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, diamondGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.GEMS_DIAMOND)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(diamondGenerator.getUpgradeSupplier().get())), prefixedRecipeId(diamondGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var emeraldGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "emerald"));
        shaped(RecipeCategory.MISC, emeraldGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.GEMS_EMERALD)
                .define('G', Ingredient.of(diamondGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(emeraldGenerator.getBlockSupplier().get())), prefixedRecipeId(emeraldGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, emeraldGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.GEMS_EMERALD)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(emeraldGenerator.getUpgradeSupplier().get())), prefixedRecipeId(emeraldGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var netheriteGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "netherite"));
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(diamondGenerator.getBlockSupplier().get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC,
                        netheriteGenerator.getBlockSupplier().get().asItem()
                )
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .unlocks(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .save(pRecipeOutput.withConditions(exists(netheriteGenerator.getBlockSupplier().get())), prefixedRecipeId(netheriteGenerator.getBlockSupplier().get(), "generators/"));
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(diamondGenerator.getUpgradeSupplier().get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC,
                        netheriteGenerator.getUpgradeSupplier().get()
                )
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .unlocks(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .save(pRecipeOutput.withConditions(exists(netheriteGenerator.getUpgradeSupplier().get())), prefixedRecipeId(netheriteGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var netherstarGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "netherstar"));
        shaped(RecipeCategory.MISC, netherstarGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(netheriteGenerator.getBlockSupplier().get()), has(netheriteGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Items.WITHER_SKELETON_SKULL)
                .define('G', Ingredient.of(netheriteGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(netherstarGenerator.getBlockSupplier().get())), prefixedRecipeId(netherstarGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, netherstarGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(netheriteGenerator.getBlockSupplier().get()), has(netheriteGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Items.WITHER_SKELETON_SKULL)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(netherstarGenerator.getUpgradeSupplier().get())), prefixedRecipeId(netherstarGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var obsidianGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "obsidian"));
        shaped(RecipeCategory.MISC, obsidianGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.OBSIDIANS)
                .define('G', Ingredient.of(diamondGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(obsidianGenerator.getBlockSupplier().get())), prefixedRecipeId(obsidianGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, obsidianGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.OBSIDIANS)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(obsidianGenerator.getUpgradeSupplier().get())), prefixedRecipeId(obsidianGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var magmaticGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic"));
        shaped(RecipeCategory.MISC, magmaticGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.BUCKETS_LAVA)
                .save(pRecipeOutput.withConditions(exists(magmaticGenerator.getBlockSupplier().get())), prefixedRecipeId(magmaticGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, magmaticGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.BUCKETS_LAVA)
                .save(pRecipeOutput.withConditions(exists(magmaticGenerator.getUpgradeSupplier().get())), prefixedRecipeId(magmaticGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var enchantmentGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "enchantment"));
        shaped(RecipeCategory.MISC, enchantmentGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Items.ENCHANTED_BOOK)
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Items.ENCHANTING_TABLE)
                .save(pRecipeOutput.withConditions(exists(enchantmentGenerator.getBlockSupplier().get())), prefixedRecipeId(enchantmentGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, enchantmentGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Items.ENCHANTED_BOOK)
                .define('F', Items.ITEM_FRAME)
                .define('R', Items.ENCHANTING_TABLE)
                .save(pRecipeOutput.withConditions(exists(enchantmentGenerator.getUpgradeSupplier().get())), prefixedRecipeId(enchantmentGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var enderGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "ender"));
        shaped(RecipeCategory.MISC, enderGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.ENDER_PEARLS)
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(enderGenerator.getBlockSupplier().get())), prefixedRecipeId(enderGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, enderGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.ENDER_PEARLS)
                .define('F', Items.ITEM_FRAME)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(pRecipeOutput.withConditions(exists(enderGenerator.getUpgradeSupplier().get())), prefixedRecipeId(enderGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var halitosisGenerator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "halitosis"));
        shaped(RecipeCategory.MISC, halitosisGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(enderGenerator.getBlockSupplier().get()), has(enderGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Tags.Items.GEMS_AMETHYST)
                .define('G', Ingredient.of(enderGenerator.getBlockSupplier().get()))
                .define('R', Items.END_ROD)
                .save(pRecipeOutput.withConditions(exists(halitosisGenerator.getBlockSupplier().get())), prefixedRecipeId(halitosisGenerator.getBlockSupplier().get(), "generators/"));
        shaped(RecipeCategory.MISC, halitosisGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(enderGenerator.getBlockSupplier().get()), has(enderGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Tags.Items.GEMS_AMETHYST)
                .define('F', Items.ITEM_FRAME)
                .define('R', Items.END_ROD)
                .save(pRecipeOutput.withConditions(exists(halitosisGenerator.getUpgradeSupplier().get())), prefixedRecipeId(halitosisGenerator.getUpgradeSupplier().get(), "upgrades/"));

        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            var base = BuiltInRegistries.BLOCK.getKey(generatorObject.getBlockSupplier().get());
            var gen8x = BuiltInRegistries.BLOCK.getOptional(base.withPath(p -> p + "_8x")).orElse(null);
            var gen64x = BuiltInRegistries.BLOCK.getOptional(base.withPath(p -> p + "_64x")).orElse(null);
            if (gen8x == null || gen64x == null) {
                return;
            }

            shaped(RecipeCategory.MISC, gen8x, 1)
                    .unlockedBy(getHasName(generatorObject.getBlockSupplier().get()), has(generatorObject.getBlockSupplier().get()))
                    .pattern("III").pattern("IFI").pattern("III")
                    .define('I', Ingredient.of(generatorObject.getBlockSupplier().get()))
                    .define('F', Items.ECHO_SHARD)
                    .save(pRecipeOutput.withConditions(exists(gen8x)), prefixedRecipeId(gen8x, "8x/"));

            shaped(RecipeCategory.MISC, gen64x, 1)
                    .unlockedBy(getHasName(gen8x), has(gen8x))
                    .pattern("III").pattern("IFI").pattern("III")
                    .define('I', gen8x)
                    .define('F', Items.CONDUIT)
                    .save(pRecipeOutput.withConditions(exists(gen64x)), prefixedRecipeId(gen64x, "64x/"));
        });
    }

    private static ResourceKey<Recipe<?>> prefixedRecipeId(ItemLike item, String prefix) {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(item.asItem()).withPath(path -> prefix + path));
    }

    private static ICondition exists(ItemLike item) {
        return NeoForgeConditions.itemRegistered(BuiltInRegistries.ITEM.getKey(item.asItem()));
    }

    public static class Runner extends net.minecraft.data.recipes.RecipeProvider.Runner
    {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Generator Galore Recipes";
        }
    }
}
