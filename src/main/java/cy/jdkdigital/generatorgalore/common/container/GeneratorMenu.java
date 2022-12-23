package cy.jdkdigital.generatorgalore.common.container;

import cy.jdkdigital.generatorgalore.cap.ControlledEnergyStorage;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Objects;

public class GeneratorMenu extends AbstractContainer
{
    public final GeneratorBlockEntity blockEntity;

    public static final int SLOT_FUEL = 0;

    public GeneratorMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    public GeneratorMenu(int id, Inventory inventory, GeneratorBlockEntity blockEntity) {
        super(blockEntity.generator.getMenuType().get(), id);

        this.blockEntity = blockEntity;

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.litTime;
                    case 1 -> blockEntity.litDuration;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> blockEntity.litTime = value;
                    case 1 -> blockEntity.litDuration = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        // Energy
        addDataSlot(new DataSlot()
        {
            @Override
            public int get() {
                return blockEntity.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
            }

            @Override
            public void set(int value) {
                blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                    if (handler.getEnergyStored() > 0) {
                        handler.extractEnergy(handler.getEnergyStored(), false);
                    }
                    if (value > 0 && handler instanceof ControlledEnergyStorage controlledEnergyStorage) {
                        controlledEnergyStorage.receiveEnergy(value, false, true);
                    }
                });
            }
        });

        if (this.blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            // Fluid
            addDataSlots(new ContainerData()
            {
                @Override
                public int get(int i) {
                    return i == 0 ? blockEntity.fluidId : blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).map(fluidHandler -> fluidHandler.getFluidInTank(0).getAmount()).orElse(0);
                }

                @Override
                public void set(int i, int value) {
                    switch (i) {
                        case 0:
                            blockEntity.fluidId = value;
                        case 1:
                            blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidHandler -> {
                                FluidStack fluid = fluidHandler.getFluidInTank(0);
                                if (fluid.isEmpty()) {
                                    fluidHandler.fill(new FluidStack(BuiltInRegistries.FLUID.byId(blockEntity.fluidId), value), IFluidHandler.FluidAction.EXECUTE);
                                } else {
                                    fluid.setAmount(value);
                                }
                            });
                    }
                }

                @Override
                public int getCount() {
                    return 2;
                }
            });
        }

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            if (inv instanceof ManualItemHandler itemHandler) {
                addSlot(new ManualSlotItemHandler(itemHandler, SLOT_FUEL, 80, 54));
            }
        });

        layoutPlayerInventorySlots(inventory, 0, 8, 84);
    }

    private static GeneratorBlockEntity getTileEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        final BlockEntity tileAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof GeneratorBlockEntity) {
            return (GeneratorBlockEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null && player.distanceToSqr(this.blockEntity.getBlockPos().getX() + 0.5D, this.blockEntity.getBlockPos().getY() + 0.5D, this.blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    public int getLitProgress() {
        int i = this.blockEntity.litDuration;
        if (i == 0) {
            i = 200;
        }
        return this.blockEntity.litTime * 13 / i;
    }
}
