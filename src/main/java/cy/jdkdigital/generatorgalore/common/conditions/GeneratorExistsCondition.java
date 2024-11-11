package cy.jdkdigital.generatorgalore.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public record GeneratorExistsCondition(ResourceLocation generatorName) implements ICondition
{
    public static MapCodec<GeneratorExistsCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(ResourceLocation.CODEC.fieldOf("generator").forGetter(GeneratorExistsCondition::generatorName))
                    .apply(builder, GeneratorExistsCondition::new));

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(IContext context) {
        return GeneratorRegistry.generators.containsKey(generatorName);
    }

    @Override
    public String toString() {
        return "generator_exists(\"" + generatorName + "\")";
    }
}