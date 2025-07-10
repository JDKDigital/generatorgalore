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
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public record FluidFuelRecipe(List<FluidStack> fuels, ItemStack generator, float rate, float consumptionRate) implements Recipe<RecipeInput>
{
    @Override
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput p_345149_, HolderLookup.Provider p_346030_) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.FLUID_FUEL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FLUID_FUEL_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FluidFuelRecipe>
    {
        private static final MapCodec<FluidFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                    FluidStack.CODEC.listOf().fieldOf("fuels").orElse(List.of()).forGetter(recipe -> recipe.fuels),
                    ItemStack.CODEC.fieldOf("generator").forGetter(recipe -> recipe.generator),
                    Codec.FLOAT.fieldOf("rate").forGetter(recipe -> recipe.rate),
                    Codec.FLOAT.fieldOf("consumptionRate").forGetter(recipe -> recipe.consumptionRate)
                )
                .apply(builder, FluidFuelRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidFuelRecipe> STREAM_CODEC = StreamCodec.of(
                FluidFuelRecipe.Serializer::toNetwork, FluidFuelRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<FluidFuelRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidFuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static FluidFuelRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
            try {
                return new FluidFuelRecipe(FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat(), buffer.readFloat());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error reading item fuels recipe from packet.", e);
                throw e;
            }
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, FluidFuelRecipe recipe) {
            try {
                FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fuels());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.generator());
                buffer.writeFloat(recipe.rate());
                buffer.writeFloat(recipe.consumptionRate());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error writing item fuels recipe to packet.", e);
                throw e;
            }
        }
    }
}
