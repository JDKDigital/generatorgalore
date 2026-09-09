package cy.jdkdigital.generatorgalore.cap;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ControlledEnergyStorage extends SimpleEnergyHandler
{
    public ControlledEnergyStorage(int capacity) {
        super(capacity);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    public int insertInternal(int amount, TransactionContext transaction) {
        return super.insert(amount, transaction);
    }
}
