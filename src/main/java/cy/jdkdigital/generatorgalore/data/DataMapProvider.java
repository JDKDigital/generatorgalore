package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.datamap.FluidFuelMap;
import cy.jdkdigital.generatorgalore.common.datamap.PotionComponentIngredient;
import cy.jdkdigital.generatorgalore.common.datamap.SolidFuelMap;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataMapProvider extends net.neoforged.neoforge.common.data.DataMapProvider
{
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        final var fluidFuels = builder(GeneratorGalore.FLUID_FUEL_MAP);
        final var solidFuels = builder(GeneratorGalore.SOLID_FUEL_MAP);

        var lavaGen = generatorBlockId("magmatic");
        fluidFuels.add(lavaGen,
                new FluidFuelMap(List.of(
                        new FluidFuelMap.FluidFuel(FluidIngredient.of(provider.lookupOrThrow(Registries.FLUID).getOrThrow(FluidTags.create(Identifier.withDefaultNamespace("lava")))), 0.4d, 40d)
                )), false, NeoForgeConditions.itemRegistered(lavaGen));

        var enderGen = generatorBlockId("ender");
        solidFuels.add(enderGen,
                new SolidFuelMap(List.of(
                        new SolidFuelMap.SolidFuel(Ingredient.of(Items.ENDER_PEARL), 1.0f, 1600, 96),
                        new SolidFuelMap.SolidFuel(Ingredient.of(Items.ENDER_EYE), 1.0f, 3200, 80)
                )), false, NeoForgeConditions.itemRegistered(enderGen));

        var halitosisGen = generatorBlockId("halitosis");
        solidFuels.add(halitosisGen,
                new SolidFuelMap(List.of(
                        new SolidFuelMap.SolidFuel(Ingredient.of(Items.DRAGON_BREATH), 1.0f, 12000, 128)
                )), false, NeoForgeConditions.itemRegistered(halitosisGen));

//        List<SolidFuelMap.SolidFuel> potionFuels = new ArrayList<>();
//        provider.lookup(Registries.POTION).ifPresent(
//            potionRegistryLookup -> {
//                generatePotionEffectTypes(
//                        potionFuels,
//                        potionRegistryLookup,
//                        Items.POTION
//                );
//                generatePotionEffectTypes(
//                        potionFuels,
//                        potionRegistryLookup,
//                        Items.SPLASH_POTION
//                );
//                generatePotionEffectTypes(
//                        potionFuels,
//                        potionRegistryLookup,
//                        Items.LINGERING_POTION
//                );
//            }
//        );
//        var potionGen = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "potion");
//        solidFuels.add(BuiltInRegistries.BLOCK.getKey(GeneratorRegistry.generators.get(potionGen).getBlockSupplier().get().builtInRegistryHolder().value()),
//                new SolidFuelMap(potionFuels), false, new GeneratorExistsCondition(potionGen));
    }

    private static Identifier generatorBlockId(String name) {
        var generator = GeneratorRegistry.generators.get(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, name));
        return BuiltInRegistries.BLOCK.getKey(generator.getBlockSupplier().get().builtInRegistryHolder().value());
    }
}
