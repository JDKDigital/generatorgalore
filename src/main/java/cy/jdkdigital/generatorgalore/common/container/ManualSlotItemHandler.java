package cy.jdkdigital.generatorgalore.common.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ManualSlotItemHandler extends SlotItemHandler
{
    ManualItemHandler handler;

    public ManualSlotItemHandler(ManualItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        handler = itemHandler;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return handler.isItemValid(this.getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !this.handler.extractItem(this.getSlotIndex(), 1, true, false).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        return this.handler.extractItem(this.getSlotIndex(), amount, false, false);
    }
}
