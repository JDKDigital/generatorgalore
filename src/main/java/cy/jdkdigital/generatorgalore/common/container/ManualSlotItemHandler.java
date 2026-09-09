package cy.jdkdigital.generatorgalore.common.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class ManualSlotItemHandler extends ResourceHandlerSlot
{
    private final ManualItemHandler handler;

    public ManualSlotItemHandler(ManualItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
        this.handler = itemHandler;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !handler.getResource(this.getSlotIndex()).isEmpty();
    }
}
