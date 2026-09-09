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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public record SolidFuelRecipe(List<Ingredient> fuels, ItemStack generator, float rate, float consumptionRate) implements Recipe<RecipeInput>
{
    public static final MapCodec<SolidFuelRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC.listOf().fieldOf("fuels").orElse(List.of()).forGetter(recipe -> recipe.fuels),
                            ItemStack.CODEC.fieldOf("generator").forGetter(recipe -> recipe.generator),
                            Codec.FLOAT.fieldOf("rate").forGetter(recipe -> recipe.rate),
                            Codec.FLOAT.fieldOf("consumptionRate").forGetter(recipe -> recipe.consumptionRate)
                    )
                    .apply(builder, SolidFuelRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SolidFuelRecipe> STREAM_CODEC = StreamCodec.of(
            SolidFuelRecipe::toNetwork, SolidFuelRecipe::fromNetwork
    );

    public static final RecipeSerializer<SolidFuelRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

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
    public RecipeSerializer<SolidFuelRecipe> getSerializer() {
        return ModRecipeTypes.SOLID_FUEL.get();
    }

    @Override
    public RecipeType<SolidFuelRecipe> getType() {
        return ModRecipeTypes.SOLID_FUEL_TYPE.get();
    }

    public static SolidFuelRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        try {
            return new SolidFuelRecipe(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readFloat(), buffer.readFloat());
        } catch (Exception e) {
            GeneratorGalore.LOGGER.error("Error reading solid fuels recipe from packet. ", e);
            throw e;
        }
    }

    public static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, SolidFuelRecipe recipe) {
        try {
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fuels());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.generator());
            buffer.writeFloat(recipe.rate());
            buffer.writeFloat(recipe.consumptionRate());
        } catch (Exception e) {
            GeneratorGalore.LOGGER.error("Error writing solid fuels recipe to packet.", e);
            throw e;
        }
    }
}
