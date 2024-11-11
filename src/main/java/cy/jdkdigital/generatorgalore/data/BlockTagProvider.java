package cy.jdkdigital.generatorgalore.data;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider
{
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper) {
        super(output, provider, GeneratorGalore.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var pickaxeMineable = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var infiniburn = tag(BlockTags.INFINIBURN_OVERWORLD);

        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            pickaxeMineable.addOptional(resourceLocation.withPath(p -> p + "_generator"));
            pickaxeMineable.addOptional(resourceLocation.withPath(p -> p + "_generator_8x"));
            pickaxeMineable.addOptional(resourceLocation.withPath(p -> p + "_generator_64x"));
        });

        infiniburn.addOptional(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator"));
        infiniburn.addOptional(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator_8x"));
        infiniburn.addOptional(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "magmatic_generator_64x"));
    }

    @Override
    public String getName() {
        return "Generator Galore Block Tags Provider";
    }
}
