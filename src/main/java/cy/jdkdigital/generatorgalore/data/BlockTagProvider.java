package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider
{
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, GeneratorGalore.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var pickaxeMineable = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var infiniburn = tag(BlockTags.INFINIBURN_OVERWORLD);

        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            pickaxeMineable.add(TagEntry.optionalElement(resourceLocation.withPath(p -> p + "_generator")));
            pickaxeMineable.add(TagEntry.optionalElement(resourceLocation.withPath(p -> p + "_generator_8x")));
            pickaxeMineable.add(TagEntry.optionalElement(resourceLocation.withPath(p -> p + "_generator_64x")));
        });

        infiniburn.add(TagEntry.optionalElement(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator")));
        infiniburn.add(TagEntry.optionalElement(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator_8x")));
        infiniburn.add(TagEntry.optionalElement(Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator_64x")));
    }

    @Override
    public String getName() {
        return "Generator Galore Block Tags Provider";
    }
}
