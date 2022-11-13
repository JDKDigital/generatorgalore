package cy.jdkdigital.generatorgalore.common.recipe;

import com.google.gson.JsonObject;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.init.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record SolidFuelRecipe(ResourceLocation id,
                              List<Ingredient> fuels,
                              Ingredient generator, float rate,
                              int burnTime) implements Recipe<Container>
{

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container pContainer) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOLID_FUEL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SOLID_FUEL_TYPE.get();
    }

    public static class Serializer<T extends SolidFuelRecipe> implements RecipeSerializer<T>
    {
        final IRecipeFactory<T> factory;

        public Serializer(Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Nonnull
        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            Ingredient generator = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "generator"));
            float rate = GsonHelper.getAsFloat(json, "rate");
            int burnTime = GsonHelper.getAsInt(json, "consumptionRate");
            return this.factory.create(id, new ArrayList<>(), generator, rate, burnTime);
        }

        public T fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            try {
                List<Ingredient> fuels = new ArrayList<>();
                IntStream.range(0, buffer.readInt()).forEach(
                    i -> fuels.add(Ingredient.fromNetwork(buffer))
                );
                return this.factory.create(id, fuels, Ingredient.fromNetwork(buffer), buffer.readFloat(), buffer.readInt());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error reading solid fuels recipe from packet. " + id, e);
                throw e;
            }
        }

        public void toNetwork(@Nonnull FriendlyByteBuf buffer, T recipe) {
            try {
                buffer.writeInt(recipe.fuels().size());
                recipe.fuels().forEach(fuel -> {
                    fuel.toNetwork(buffer);
                });
                recipe.generator().toNetwork(buffer);
                buffer.writeFloat(recipe.rate());
                buffer.writeInt(recipe.burnTime());
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("Error writing solid fuels recipe to packet. " + recipe.getId(), e);
                throw e;
            }
        }

        public interface IRecipeFactory<T extends SolidFuelRecipe>
        {
            T create(ResourceLocation id, List<Ingredient> fuel, Ingredient generator, float rate, int burnTime);
        }
    }
}
