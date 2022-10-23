package cy.jdkdigital.generatorgalore.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class AbstractContainer extends AbstractContainerMenu
{
    protected AbstractContainer(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            final ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            final int containerSlots = this.slots.size() - player.getInventory().items.size();

            // Move from container to player inventory.
            if (index < containerSlots) {
                if (!moveItemStackTo(slotStack, containerSlots, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int upgradeSlotCount = 0;
                // Move from player inv into container
//                int upgradeSlotCount = this.getTileEntity() instanceof UpgradeableBlockEntity ? 4 : 0;
//                if (upgradeSlotCount > 0 && slotStack.getItem() instanceof UpgradeItem) {
//                    if (!moveItemStackTo(slotStack, containerSlots - upgradeSlotCount, containerSlots, false)) {
//                        return ItemStack.EMPTY;
//                    }
//                } else {
                    if (!moveItemStackTo(slotStack, 0, containerSlots - upgradeSlotCount, false)) {
                        return ItemStack.EMPTY;
                    }
//                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            if (slotStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return returnStack;
    }

    protected int addSlotRange(Container handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    protected void addSlotBox(Container handler, int startIndex, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            startIndex = addSlotRange(handler, startIndex, x, y, horAmount, dx);
            y += dy;
        }
    }

    protected void layoutPlayerInventorySlots(Inventory inventory, int startIndex, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(inventory, startIndex + 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(inventory, startIndex, leftCol, topRow, 9, 18);
    }
}
