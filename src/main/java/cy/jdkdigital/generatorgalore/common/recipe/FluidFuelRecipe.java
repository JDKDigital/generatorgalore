package cy.jdkdigital.generatorgalore.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.init.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public record FluidFuelRecipe(List<FluidStack> fuels, ItemStack generator, float rate, float consumptionRate) implements Recipe<RecipeInput>
{
    public static final MapCodec<FluidFuelRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            FluidStack.CODEC.listOf().fieldOf("fuels").orElse(List.of()).forGetter(recipe -> recipe.fuels),
                            ItemStack.CODEC.fieldOf("generator").forGetter(recipe -> recipe.generator),
                            Codec.FLOAT.fieldOf("rate").forGetter(recipe -> recipe.rate),
                            Codec.FLOAT.fieldOf("consumptionRate").forGetter(recipe -> recipe.consumptionRate)
                    )
                    .apply(builder, FluidFuelRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFuelRecipe> STREAM_CODEC = StreamCodec.of(
            FluidFuelRecipe::toNetwork, FluidFuelRecipe::fromNetwork
    );

    public static final RecipeSerializer<FluidFuelRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<FluidFuelRecipe> getSerializer() {
        return ModRecipeTypes.FLUID_FUEL.get();
    }

    @Override
    public RecipeType<FluidFuelRecipe> getType() {
        return ModRecipeTypes.FLUID_FUEL_TYPE.get();
    }

    public static FluidFuelRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        try {
            return new FluidFuelRecipe(FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readFloat(), buffer.readFloat());
        } catch (Exception e) {
            GeneratorGalore.LOGGER.error("Error reading fluid fuels recipe from packet. ", e);
            throw e;
        }
    }

    public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, FluidFuelRecipe recipe) {
        try {
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fuels());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.generator());
            buffer.writeFloat(recipe.rate());
            buffer.writeFloat(recipe.consumptionRate());
        } catch (Exception e) {
            GeneratorGalore.LOGGER.error("Error writing fluid fuels recipe to packet.", e);
            throw e;
        }
    }
}
