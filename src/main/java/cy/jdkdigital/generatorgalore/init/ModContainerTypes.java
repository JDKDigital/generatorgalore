package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import java.util.function.Supplier;

public class ModContainerTypes
{
    public static <E extends AbstractContainerMenu> Supplier<MenuType<E>> register(String id, IMenuTypeExtension<E> item) {
        return GeneratorGalore.CONTAINER_TYPES.register(id, () -> IMenuTypeExtension.create(item::create));
    }
}
