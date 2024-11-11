package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.conditions.GeneratorExistsCondition;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder
{
    public RecipeProvider(PackOutput gen, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(gen, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        var copperGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "copper"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, copperGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(Items.FURNACE), has(Items.FURNACE))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_COPPER))
                .define('G', Ingredient.of(Items.FURNACE))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(copperGenerator.getId())), prefixedRecipeId(copperGenerator.getBlockSupplier().get(), "generators/"));

        var ironGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "iron"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ironGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(copperGenerator.getBlockSupplier().get()), has(copperGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('G', Ingredient.of(copperGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(ironGenerator.getId())), prefixedRecipeId(ironGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ironGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(copperGenerator.getBlockSupplier().get()), has(copperGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(ironGenerator.getId())), prefixedRecipeId(ironGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var goldGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "gold"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, goldGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(ironGenerator.getBlockSupplier().get()), has(ironGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_GOLD))
                .define('G', Ingredient.of(ironGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(goldGenerator.getId())), prefixedRecipeId(goldGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, goldGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(ironGenerator.getBlockSupplier().get()), has(ironGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_GOLD))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(goldGenerator.getId())), prefixedRecipeId(goldGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var culinaryGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "culinary"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, culinaryGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("ICI").pattern("IGI").pattern("ERE")
                .define('I', Ingredient.of(Tags.Items.CROPS))
                .define('C', Ingredient.of(Items.CAKE))
                .define('E', Ingredient.of(Tags.Items.EGGS))
                .define('G', Ingredient.of(goldGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(culinaryGenerator.getId())), prefixedRecipeId(culinaryGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, culinaryGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("ICI").pattern("IFI").pattern("ERE")
                .define('I', Ingredient.of(Tags.Items.CROPS))
                .define('C', Ingredient.of(Items.CAKE))
                .define('E', Ingredient.of(Tags.Items.EGGS))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(culinaryGenerator.getId())), prefixedRecipeId(culinaryGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var potionGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "potion"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, potionGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("BRB")
                .define('I', Ingredient.of(Tags.Items.DYED_PINK))
                .define('B', Ingredient.of(Items.BREWING_STAND))
                .define('G', Ingredient.of(culinaryGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(potionGenerator.getId())), prefixedRecipeId(potionGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, potionGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("BRB")
                .define('I', Ingredient.of(Tags.Items.DYED_PINK))
                .define('B', Ingredient.of(Items.BREWING_STAND))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(potionGenerator.getId())), prefixedRecipeId(potionGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var honeyGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "honey"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, honeyGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("BRB")
                .define('I', Ingredient.of(Items.HONEY_BLOCK))
                .define('B', Ingredient.of(Items.BUCKET))
                .define('G', Ingredient.of(culinaryGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(honeyGenerator.getId())), prefixedRecipeId(honeyGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, honeyGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(culinaryGenerator.getBlockSupplier().get()), has(culinaryGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("BRB")
                .define('I', Ingredient.of(Items.HONEY_BLOCK))
                .define('B', Ingredient.of(Items.BUCKET))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(honeyGenerator.getId())), prefixedRecipeId(honeyGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var diamondGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "diamond"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, diamondGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_DIAMOND))
                .define('G', Ingredient.of(goldGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(diamondGenerator.getId())), prefixedRecipeId(diamondGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, diamondGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(goldGenerator.getBlockSupplier().get()), has(goldGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_DIAMOND))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(diamondGenerator.getId())), prefixedRecipeId(diamondGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var emeraldGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "emerald"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, emeraldGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_EMERALD))
                .define('G', Ingredient.of(diamondGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(emeraldGenerator.getId())), prefixedRecipeId(emeraldGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, emeraldGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_EMERALD))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(emeraldGenerator.getId())), prefixedRecipeId(emeraldGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var netheriteGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "netherite"));
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(diamondGenerator.getBlockSupplier().get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC,
                        netheriteGenerator.getBlockSupplier().get().asItem()
                )
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .unlocks(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(netheriteGenerator.getId())), prefixedRecipeId(netheriteGenerator.getBlockSupplier().get(), "generators/"));
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(diamondGenerator.getUpgradeSupplier().get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC,
                        netheriteGenerator.getUpgradeSupplier().get()
                )
                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .unlocks(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(netheriteGenerator.getId())), prefixedRecipeId(netheriteGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var netherstarGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "netherstar"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, netherstarGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(netheriteGenerator.getBlockSupplier().get()), has(netheriteGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Items.WITHER_SKELETON_SKULL))
                .define('G', Ingredient.of(netheriteGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(netherstarGenerator.getId())), prefixedRecipeId(netherstarGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, netherstarGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(netheriteGenerator.getBlockSupplier().get()), has(netheriteGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Items.WITHER_SKELETON_SKULL))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(netherstarGenerator.getId())), prefixedRecipeId(netherstarGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var obsidianGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "obsidian"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, obsidianGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.OBSIDIANS))
                .define('G', Ingredient.of(diamondGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(obsidianGenerator.getId())), prefixedRecipeId(obsidianGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, obsidianGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(diamondGenerator.getBlockSupplier().get()), has(diamondGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.OBSIDIANS))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(obsidianGenerator.getId())), prefixedRecipeId(obsidianGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var magmaticGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, magmaticGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_GOLD))
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.BUCKETS_LAVA))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(magmaticGenerator.getId())), prefixedRecipeId(magmaticGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, magmaticGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_GOLD))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.BUCKETS_LAVA))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(magmaticGenerator.getId())), prefixedRecipeId(magmaticGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var enchantmentGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "enchantment"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, enchantmentGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Items.ENCHANTED_BOOK))
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Items.ENCHANTING_TABLE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(enchantmentGenerator.getId())), prefixedRecipeId(enchantmentGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, enchantmentGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Items.ENCHANTED_BOOK))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Items.ENCHANTING_TABLE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(enchantmentGenerator.getId())), prefixedRecipeId(enchantmentGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var enderGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "ender"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, enderGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.ENDER_PEARLS))
                .define('G', Ingredient.of(obsidianGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(enderGenerator.getId())), prefixedRecipeId(enderGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, enderGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(obsidianGenerator.getBlockSupplier().get()), has(obsidianGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.ENDER_PEARLS))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(enderGenerator.getId())), prefixedRecipeId(enderGenerator.getUpgradeSupplier().get(), "upgrades/"));

        var halitosisGenerator = GeneratorRegistry.generators.get(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "halitosis"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, halitosisGenerator.getBlockSupplier().get(), 1)
                .unlockedBy(getHasName(enderGenerator.getBlockSupplier().get()), has(enderGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IGI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_AMETHYST))
                .define('G', Ingredient.of(enderGenerator.getBlockSupplier().get()))
                .define('R', Ingredient.of(Items.END_ROD))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(halitosisGenerator.getId())), prefixedRecipeId(halitosisGenerator.getBlockSupplier().get(), "generators/"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, halitosisGenerator.getUpgradeSupplier().get(), 1)
                .unlockedBy(getHasName(enderGenerator.getBlockSupplier().get()), has(enderGenerator.getBlockSupplier().get()))
                .pattern("III").pattern("IFI").pattern("IRI")
                .define('I', Ingredient.of(Tags.Items.GEMS_AMETHYST))
                .define('F', Ingredient.of(Items.ITEM_FRAME))
                .define('R', Ingredient.of(Items.END_ROD))
                .save(pRecipeOutput.withConditions(new GeneratorExistsCondition(halitosisGenerator.getId())), prefixedRecipeId(halitosisGenerator.getUpgradeSupplier().get(), "upgrades/"));

        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            var base = BuiltInRegistries.BLOCK.getKey(generatorObject.getBlockSupplier().get());
            var gen8x = BuiltInRegistries.BLOCK.get(base.withPath(p -> p + "_8x"));
            var gen64x = BuiltInRegistries.BLOCK.get(base.withPath(p -> p + "_64x"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gen8x, 1)
                    .unlockedBy(getHasName(generatorObject.getBlockSupplier().get()), has(generatorObject.getBlockSupplier().get()))
                    .pattern("III").pattern("IFI").pattern("III")
                    .define('I', Ingredient.of(generatorObject.getBlockSupplier().get()))
                    .define('F', Ingredient.of(Items.ECHO_SHARD))
                    .save(generatorObject.has8x() ? pRecipeOutput.withConditions(new GeneratorExistsCondition(generatorObject.getId())) : pRecipeOutput.withConditions(modLoaded("removethisconditiontoenable")), prefixedRecipeId(gen8x, "8x/"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gen64x, 1)
                    .unlockedBy(getHasName(gen8x), has(gen8x))
                    .pattern("III").pattern("IFI").pattern("III")
                    .define('I', Ingredient.of(gen8x))
                    .define('F', Ingredient.of(Items.CONDUIT))
                    .save(generatorObject.has64x() ? pRecipeOutput.withConditions(new GeneratorExistsCondition(generatorObject.getId())) : pRecipeOutput.withConditions(modLoaded("removethisconditiontoenable")), prefixedRecipeId(gen64x, "64x/"));
        });
    }

    private static ResourceLocation prefixedRecipeId(ItemLike item, String prefix) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).withPath(path ->  prefix + path);
    }
}
