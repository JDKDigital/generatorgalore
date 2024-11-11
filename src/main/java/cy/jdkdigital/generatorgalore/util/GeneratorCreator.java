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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GeneratorCreator
{
    public static GeneratorObject create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        var generatorOptional = GeneratorObject.codec(id).parse(JsonOps.INSTANCE, json).result();

        if (generatorOptional.isPresent()) {
            var generator = generatorOptional.get();
            var name = String.format("%s_%s", generator.getId().getPath(), "generator");

            Supplier<Block> generatorBlock = GeneratorGalore.BLOCKS.register(name, () -> new Generator(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE), generator, 1));
            generator.setBlockSupplier(generatorBlock);
            List<Supplier<Block>> generatorBlocks = new ArrayList<>();
            generatorBlocks.add(generatorBlock);
            if (generator.has8x() || !FMLEnvironment.production) {
                Supplier<Block> gen8x = GeneratorGalore.BLOCKS.register(name + "_8x", () -> new Generator(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE), generator, 8));
                generatorBlocks.add(gen8x);
                GeneratorGalore.ITEMS.register(name + "_8x", () -> new BlockItem(gen8x.get(), new Item.Properties()));
            }
            if (generator.has64x() || !FMLEnvironment.production) {
                Supplier<Block> gen64x = GeneratorGalore.BLOCKS.register(name + "_64x", () -> new Generator(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE), generator, 64));
                generatorBlocks.add(gen64x);
                GeneratorGalore.ITEMS.register(name + "_64x", () -> new BlockItem(gen64x.get(), new Item.Properties()));
            }
            generator.setBlockEntityType(ModBlockEntityTypes.register(name, () -> ModBlockEntityTypes.createBlockEntityType((pos, state) -> new GeneratorBlockEntity(generator, pos, state), generatorBlocks.stream().map(Supplier::get).toList().toArray(new Block[0]))));

            generator.setMenuType(ModContainerTypes.register(name, GeneratorMenu::new));

            GeneratorGalore.ITEMS.register(name, () -> new BlockItem(generator.getBlockSupplier().get(), new Item.Properties()));

            if (json.has("previousTier")) {
                String previousTier = json.get("previousTier").getAsString();
                generator.setUpgradeSupplier(GeneratorGalore.ITEMS.register(previousTier + "_to_" + generator.getId().getPath() + "_upgrade", () -> new UpgradeItem(new Item.Properties(), previousTier, generator)));
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
            var id = ResourceLocation.parse(el.get("item").getAsString());
            fuels.put(id, new Fuel(
                el.has("rate") ? el.get("rate").getAsFloat() : (float) generator.getGenerationRate(),
                el.get("burnTime").getAsInt()
            ));
        }
        return fuels;
    }

    public record Fuel(float rate, int burnTime) {}
}
