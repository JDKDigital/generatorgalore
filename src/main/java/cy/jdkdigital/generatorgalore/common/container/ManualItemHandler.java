package cy.jdkdigital.generatorgalore.common.container;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ManualItemHandler extends ItemStackHandler
{
    public ManualItemHandler(int size) {
        super(size);
    }

    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean fromAutomation) {
        if (fromAutomation && this.isItemValid(slot, this.getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItem(slot, amount, simulate, true);
    }
}
