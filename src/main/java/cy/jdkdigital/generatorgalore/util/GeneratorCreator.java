package cy.jdkdigital.generatorgalore.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.Generator;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import cy.jdkdigital.generatorgalore.common.item.UpgradeItem;
import cy.jdkdigital.generatorgalore.init.ModBlockEntityTypes;
import cy.jdkdigital.generatorgalore.init.ModContainerTypes;
import cy.jdkdigital.generatorgalore.init.ModItemGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;

public class GeneratorCreator
{
    public static GeneratorObject create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        var generatorOptional = GeneratorObject.codec(id).parse(JsonOps.INSTANCE, json).result();

        if (generatorOptional.isPresent()) {
            var generator = generatorOptional.get();
            var name = String.format("%s_%s", generator.getId().getPath(), "generator");

            generator.setBlockSupplier(GeneratorGalore.BLOCKS.register(name, () -> new Generator(BlockBehaviour.Properties.copy(Blocks.FURNACE), generator)));
            generator.setBlockEntityType(ModBlockEntityTypes.register(name, () -> ModBlockEntityTypes.createBlockEntityType((pos, state) -> new GeneratorBlockEntity(generator, pos, state), generator.getBlockSupplier().get())));
            generator.setMenuType(ModContainerTypes.register(name, GeneratorMenu::new));

            GeneratorGalore.ITEMS.register(name, () -> new BlockItem(generator.getBlockSupplier().get(), new Item.Properties().tab(ModItemGroups.GENERATORGALORE)));

            if (json.has("previousTier")) {
                String previousTier = json.get("previousTier").getAsString();
                GeneratorGalore.ITEMS.register(previousTier + "_to_" + generator.getId().getPath() + "_upgrade", () -> new UpgradeItem(new Item.Properties().tab(ModItemGroups.GENERATORGALORE), previousTier, generator));
            }

            // Custom fuels list
            if (json.has("fuelList")) {
                generator.setFuelList(parseFuelList(generator, json.get("fuelList")));
            }

            return generator;
        } else {
            GeneratorGalore.LOGGER.info("failed to read generator configuration for " + id);
        }
        return null;
    }

    private static Map<ResourceLocation, Fuel> parseFuelList(GeneratorObject generator, JsonElement fuelList) {
        Map<ResourceLocation, GeneratorCreator.Fuel> fuels = new HashMap<>();
        for (JsonElement jsonElement : fuelList.getAsJsonArray()) {
            var el = jsonElement.getAsJsonObject();
            var id = new ResourceLocation(el.get("item").getAsString());
            fuels.put(id, new Fuel(
                el.has("rate") ? el.get("rate").getAsFloat() : (float) generator.getGenerationRate(),
                el.get("burnTime").getAsInt()
            ));
        }
        return fuels;
    }

    public record Fuel(float rate, int burnTime) {}
}
