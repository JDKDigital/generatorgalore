package cy.jdkdigital.generatorgalore.cap;

import net.minecraftforge.energy.EnergyStorage;

public class ControlledEnergyStorage extends EnergyStorage
{
    public ControlledEnergyStorage(int capacity) {
        super(capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return receiveEnergy(maxReceive, simulate, false);
    }

    public int receiveEnergy(int maxReceive, boolean simulate, boolean internal) {
        return internal ? super.receiveEnergy(maxReceive, simulate) : 0;
    }
}
