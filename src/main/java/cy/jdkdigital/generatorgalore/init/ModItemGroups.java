package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ModItemGroups
{
    public static final CreativeModeTab GENERATORGALORE = new ModItemGroup(GeneratorGalore.MODID, () -> new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratorGalore.MODID, "iron_generator"))));

    public static class ModItemGroup extends CreativeModeTab
    {
        private final Supplier<ItemStack> iconSupplier;

        public ModItemGroup(@Nonnull final String name, @Nonnull final Supplier<ItemStack> iconSupplier) {
            super(name);
            this.iconSupplier = iconSupplier;
        }

        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return iconSupplier.get();
        }

        @Override
        public void fillItemList(@Nonnull NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    }
}
