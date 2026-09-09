package cy.jdkdigital.generatorgalore.common.container;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ManualItemHandler extends ItemStacksResourceHandler
{
    public ManualItemHandler(int size) {
        super(size);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (isValid(index, resource)) {
            return 0;
        }
        return super.extract(index, resource, amount, transaction);
    }

    public int extractInternal(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return super.extract(index, resource, amount, transaction);
    }
}
