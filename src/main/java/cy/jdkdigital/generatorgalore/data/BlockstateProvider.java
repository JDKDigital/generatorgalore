package cy.jdkdigital.generatorgalore.data;

import com.google.gson.JsonElement;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockstateProvider extends BlockStateProvider
{
    protected final PackOutput packOutput;

    protected final Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();

    public BlockstateProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, GeneratorGalore.MODID, exFileHelper);
        this.packOutput = packOutput;
    }

    @Override
    protected void registerStatesAndModels() {
        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            var block = generatorObject.getBlockSupplier().get();
            makeGeneratorBlock(block, "block/generator_base");
            var baseGen = BuiltInRegistries.BLOCK.getKey(block);

            makeGeneratorBlock(BuiltInRegistries.BLOCK.get(baseGen.withPath(p -> p + "_8x")), "block/generator_base_8x");
            makeGeneratorBlock(BuiltInRegistries.BLOCK.get(baseGen.withPath(p -> p + "_64x")), "block/generator_base_64x");
        });
    }

    private void makeGeneratorBlock(Block block, String baseModel) {
        var generatorParentModel = generatorTextureMap(block, models().withExistingParent(blockTexture(block).toString(), ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, baseModel)));
        var generatorOnParentModel = generatorOnTextureMap(block, models().withExistingParent(blockTexture(block) + "_on", generatorParentModel.getLocation()));

        this.horizontalBlock(block, blockState -> blockState.getValue(BlockStateProperties.LIT) ? generatorOnParentModel : generatorParentModel);

        this.simpleBlockItem(block, generatorParentModel);
    }

    private BlockModelBuilder generatorTextureMap(Block pBlock, BlockModelBuilder modelBuilder) {
        return modelBuilder
                .texture("side", extend(pBlock, "_side"))
                .texture("top", extend(pBlock, "_top_off"))
                .texture("bottom", extend(pBlock, "_bottom"))
                .texture("face", extend(pBlock, "_front"));
    }

    private BlockModelBuilder generatorOnTextureMap(Block pBlock, BlockModelBuilder modelBuilder) {
        return modelBuilder
                .texture("front", ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "block/generator_on_glow"))
                .texture("top", extend(pBlock, "_top_on"));
    }

    private ResourceLocation extend(Block pBlock, String suffix) {
        return blockTexture(pBlock).withPath(p -> p.replace("_8x", "").replace("_64x", "") + suffix);
    }

    private ResourceLocation blockKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    private ResourceLocation itemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
