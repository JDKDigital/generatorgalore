package cy.jdkdigital.generatorgalore.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record SolidFuelMap(List<SolidFuel> fuels) {
    public static final Codec<SolidFuelMap> CODEC =
            RecordCodecBuilder.create(in -> in.group(
                    SolidFuel.CODEC.listOf().fieldOf("fuels").forGetter(SolidFuelMap::fuels)
            ).apply(in, SolidFuelMap::new));

    public record SolidFuel(Ingredient item, float consumptionRate, int burnTime, int generationRate) {
        public static final Codec<SolidFuel> CODEC =
                RecordCodecBuilder.create(in -> in.group(
                        Ingredient.CODEC.fieldOf("item").forGetter(SolidFuel::item),
                        Codec.FLOAT.fieldOf("consumptionRate").forGetter(SolidFuel::consumptionRate),
                        Codec.INT.fieldOf("burnTime").forGetter(SolidFuel::burnTime),
                        Codec.INT.fieldOf("generationRate").forGetter(SolidFuel::generationRate)
                ).apply(in, SolidFuel::new));
    }
}
