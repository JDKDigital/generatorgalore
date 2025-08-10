package cy.jdkdigital.generatorgalore.common.datamap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Arrays;

public class PotionComponentIngredient extends DataComponentIngredient
{
    public static final MapCodec<PotionComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(PotionComponentIngredient::items),
                            DataComponentPredicate.CODEC.fieldOf("components").forGetter(PotionComponentIngredient::components)
                    )
                    .apply(builder, PotionComponentIngredient::new));


    public PotionComponentIngredient(HolderSet<Item> items, DataComponentPredicate components) {
        super(items, components, false);
    }

    @Override
    public IngredientType<?> getType() {
        return GeneratorGalore.POTIOM_INGREDIENT_TYPE.get();
    }

    public static Ingredient of(ItemStack stack) {
        var builder = DataComponentMap.builder();
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            builder.set(DataComponents.POTION_CONTENTS, stack.get(DataComponents.POTION_CONTENTS));
        }
        return of(DataComponentPredicate.allOf(builder.build()), stack.getItem());
    }

    public static Ingredient of(DataComponentPredicate predicate, ItemLike... items) {
        return of(predicate, HolderSet.direct(Arrays.stream(items).map(ItemLike::asItem).map(Item::builtInRegistryHolder).toList()));
    }

    public static Ingredient of(DataComponentPredicate predicate, HolderSet<Item> items) {
        return new PotionComponentIngredient(items, predicate).toVanilla();
    }
}
