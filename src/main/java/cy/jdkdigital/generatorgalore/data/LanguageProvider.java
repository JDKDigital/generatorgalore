package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;

public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider
{
    public LanguageProvider(PackOutput output) {
        super(output, GeneratorGalore.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.generatorgalore", "Generator Galore");
        add("generatorgalore.screen.empty", "Empty");
        add("generatorgalore.screen.energy_level", "Energy: %s");
        add("generatorgalore.screen.fluid_level", "%s: %s");
        add("generatorgalore.screen.fuel_time", "Remaining fuel time: %s");
        add("generatorgalore.screen.generation_rate", "Rate: %sFE/t");
        add("generatorgalore.screen.transfer_rate", "Transfer rate: %sFE/t");
        add("generatorgalore.screen.max_energy", "Max energy: %sFE");
        add("generatorgalore.screen.fuel_type", "Fuel type: %s");
        add("generatorgalore.recipe.solid_fuel", "Solid Fuel");
        add("generatorgalore.recipe.fluid_fuel", "Fluid Fuel");
        add("generatorgalore.recipe.rate", "Rate: %sFE/t");
        add("generatorgalore.recipe.burn_rate", "Burn rate: %smB/t");
        add("generatorgalore.recipe.burn_time", "Burntime: %s");
        add("generatorgalore.recipe.total_bucket", "Total: %sFE/B");
        add("generatorgalore.recipe.total", "Total: %sFE");

        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            add("block.generatorgalore." + resourceLocation.getPath() + "_generator", capName(resourceLocation.getPath()) + " Generator");
            add("block.generatorgalore." + resourceLocation.getPath() + "_generator_8x", "8x " + capName(resourceLocation.getPath()) + " Generator");
            add("block.generatorgalore." + resourceLocation.getPath() + "_generator_64x", "64x " + capName(resourceLocation.getPath()) + " Generator");
            if (generatorObject.getUpgradeSupplier() != null) {
                var upgradeItem = BuiltInRegistries.ITEM.getKey(generatorObject.getUpgradeSupplier().get());
                add("item.generatorgalore." + upgradeItem.getPath(), capName(upgradeItem.getPath()));
            }
        });
    }

    @Override
    public String getName() {
        return "Generator Galore translation provider";
    }

    public static String capName(String name) {
        String[] nameParts = name.split("_");

        for(int i = 0; i < nameParts.length; ++i) {
            if (nameParts[i].equals("to")) continue;
            String firstChar = nameParts[i].substring(0, 1).toUpperCase();
            nameParts[i] = firstChar + nameParts[i].substring(1);
        }

        return String.join(" ", nameParts);
    }
}
