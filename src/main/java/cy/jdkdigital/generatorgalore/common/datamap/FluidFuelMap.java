package cy.jdkdigital.generatorgalore.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.List;

public record FluidFuelMap(List<FluidFuel> fuels) {
    public static final Codec<FluidFuelMap> CODEC =
            RecordCodecBuilder.create(in -> in.group(
                    FluidFuel.CODEC.listOf().fieldOf("fuels").forGetter(FluidFuelMap::fuels)
            ).apply(in, FluidFuelMap::new));

    public record FluidFuel(FluidIngredient fluid, double consumptionRate, double generationRate) {
        public static final Codec<FluidFuel> CODEC =
                RecordCodecBuilder.create(in -> in.group(
                        FluidIngredient.CODEC.fieldOf("fluid").forGetter(FluidFuel::fluid),
                        Codec.DOUBLE.fieldOf("consumptionRate").forGetter(FluidFuel::consumptionRate),
                        Codec.DOUBLE.fieldOf("generationRate").forGetter(FluidFuel::generationRate)
                ).apply(in, FluidFuel::new));
    }
}
