package cy.jdkdigital.generatorgalore.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.init.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record SolidFuelRecipe(List<Ingredient> fuels, ItemStack generator, float rate, float consumptionRate) implements Recipe<RecipeInput>
{
    @Override
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput pContainer, HolderLookup.Provider registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOLID_FUEL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SOLID_FUEL_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<SolidFuelRecipe>
    {
        private static final MapCodec<SolidFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Ingredient.CODEC.listOf().fieldOf("fuels").orElse(List.of()).forGetter(recipe -> recipe.fuels),
                                ItemStack.CODEC.fieldOf("generator").forGetter(recipe -> recipe.generator),
                                Codec.FLOAT.fieldOf("rate").forGetter(recipe -> recipe.rate),
                                Codec.FLOAT.fieldOf("consumptionRate").forGetter(recipe -> recipe.consumptionRate)
                        )
                        .apply(builder, SolidFuelRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SolidFuelRecipe> STREAM_CODEC = StreamCodec.of(
                SolidFuelRecipe.Serializer::toNetwork, SolidFuelRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<SolidFuelRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SolidFuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static SolidFuelRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
            try {
                return new SolidFuelRecipe(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat(), buffer.readInt());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error reading solid fuels recipe from packet. ", e);
                throw e;
            }
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, SolidFuelRecipe recipe) {
            try {
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fuels());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.generator());
                buffer.writeFloat(recipe.rate());
                buffer.writeFloat(recipe.consumptionRate());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error writing solid fuels recipe to packet.", e);
                throw e;
            }
        }
    }
}
