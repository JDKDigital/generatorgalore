package cy.jdkdigital.generatorgalore.data;

import com.mojang.math.Quadrant;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockstateProvider extends ModelProvider
{
    private static final TextureSlot FACE = TextureSlot.create("face");

    public BlockstateProvider(PackOutput packOutput) {
        super(packOutput, GeneratorGalore.MODID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        List<Holder<Block>> known = new ArrayList<>();
        forEachGeneratorBlock(block -> known.add(block.builtInRegistryHolder()));
        return known.stream();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        List<Holder<Item>> known = new ArrayList<>();
        forEachGeneratorBlock(block -> known.add(block.asItem().builtInRegistryHolder()));
        return known.stream();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            var block = generatorObject.getBlockSupplier().get();
            var baseGen = BuiltInRegistries.BLOCK.getKey(block);

            makeGeneratorBlock(blockModels, block, "block/generator_base");
            variant(baseGen, "_8x").ifPresent(b -> makeGeneratorBlock(blockModels, b, "block/generator_base_8x"));
            variant(baseGen, "_64x").ifPresent(b -> makeGeneratorBlock(blockModels, b, "block/generator_base_64x"));
        });
    }

    private void makeGeneratorBlock(BlockModelGenerators blockModels, Block block, String baseModel) {
        Identifier offModel = new ModelTemplate(Optional.of(ggId(baseModel)), Optional.empty(), TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, FACE)
                .create(block, offTextures(block), blockModels.modelOutput);

        Identifier onModel = new ModelTemplate(Optional.of(offModel), Optional.empty(), TextureSlot.FRONT, TextureSlot.TOP)
                .createWithSuffix(block, "_on", onTextures(block), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.LIT)
                                .select(false, plainVariant(offModel))
                                .select(true, plainVariant(onModel)))
                        .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                                .select(Direction.NORTH, BlockModelGenerators.NOP)
                                .select(Direction.EAST, VariantMutator.Y_ROT.withValue(Quadrant.R90))
                                .select(Direction.SOUTH, VariantMutator.Y_ROT.withValue(Quadrant.R180))
                                .select(Direction.WEST, VariantMutator.Y_ROT.withValue(Quadrant.R270))));

        blockModels.registerSimpleItemModel(block, offModel);
    }

    private TextureMapping offTextures(Block block) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, new Material(extend(block, "_side")))
                .put(TextureSlot.TOP, new Material(extend(block, "_top_off")))
                .put(TextureSlot.BOTTOM, new Material(extend(block, "_bottom")))
                .put(FACE, new Material(extend(block, "_front")));
    }

    private TextureMapping onTextures(Block block) {
        return new TextureMapping()
                .put(TextureSlot.FRONT, new Material(ggId("block/generator_on_glow")))
                .put(TextureSlot.TOP, new Material(extend(block, "_top_on")));
    }

    private Identifier extend(Block block, String suffix) {
        return BuiltInRegistries.BLOCK.getKey(block).withPath(p -> "block/" + p.replace("_8x", "").replace("_64x", "") + suffix);
    }

    private Optional<Block> variant(Identifier base, String suffix) {
        return BuiltInRegistries.BLOCK.getOptional(base.withPath(p -> p + suffix));
    }

    private void forEachGeneratorBlock(java.util.function.Consumer<Block> action) {
        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            var block = generatorObject.getBlockSupplier().get();
            var baseGen = BuiltInRegistries.BLOCK.getKey(block);
            action.accept(block);
            variant(baseGen, "_8x").ifPresent(action);
            variant(baseGen, "_64x").ifPresent(action);
        });
    }

    private static Identifier ggId(String path) {
        return Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, path);
    }

    private static MultiVariant plainVariant(Identifier modelLocation) {
        return new MultiVariant(WeightedList.of(new Variant(modelLocation)));
    }
}
