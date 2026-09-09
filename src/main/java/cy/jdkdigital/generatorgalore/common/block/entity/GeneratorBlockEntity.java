package cy.jdkdigital.generatorgalore.common.block.entity;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.generatorgalore.Config;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.cap.ControlledEnergyStorage;
import cy.jdkdigital.generatorgalore.common.block.Generator;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import cy.jdkdigital.generatorgalore.common.container.ManualItemHandler;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneratorBlockEntity extends CapabilityBlockEntity
{
    private int tickCounter = 0;
    public int litTime;
    public int litDuration;
    public double remainder = 0;
    public int fluidId = 0;
    public final GeneratorObject generator;
    private final int modifier;
    public final ControlledEnergyStorage energyHandler;
    public final ManualItemHandler inventoryHandler;
    public final FluidStacksResourceHandler fluidInventory;
    private List<EnergyHandler> recipients = new ArrayList<>();
    private boolean hasLoaded = false;

    public GeneratorBlockEntity(GeneratorObject generator, BlockPos blockPos, BlockState blockState) {
        super(generator.getBlockEntityType().get(), blockPos, blockState);
        this.generator = generator;

        this.modifier = blockState.getBlock() instanceof Generator generatorBlock ? generatorBlock.getModifier() : 1;
        this.energyHandler = new ControlledEnergyStorage(generator.getBufferCapacity() * this.modifier);
        this.inventoryHandler = new ManualItemHandler(2) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                if (resource.isEmpty()) {
                    return false;
                }
                if (index == GeneratorMenu.SLOT_CHARGE) {
                    return ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
                }

                return generator.isValidFuelItem(level, resource.toStack());
            }

            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                setChanged();
            }
        };
        this.fluidInventory = new FluidStacksResourceHandler(1, 10000) {
            @Override
            public boolean isValid(int index, FluidResource resource) {
                return generator.isValidFuelFluid(resource.toStack(1));
            }

            @Override
            protected void onContentsChanged(int index, FluidStack previousContents) {
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                    fluidId = BuiltInRegistries.FLUID.getId(getResource(0).getFluid());
                    setChanged();
                }
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity blockEntity) {
        int tickRate = Config.SERVER.tickRate.get();

        if (!blockEntity.hasLoaded || blockEntity.tickCounter%111 == 0) {
            blockEntity.refreshConnectedTileEntityCache();
            blockEntity.hasLoaded = true;
        }
        if (++blockEntity.tickCounter % tickRate == 0) {
            double inputPowerAmount = blockEntity.getGenerationRate() * tickRate;
            AtomicBoolean hasConsumedFuel = new AtomicBoolean(false);

            if (!blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                if (blockEntity.isLit()) {
                    blockEntity.litTime = Math.max(0, blockEntity.litTime - tickRate);
                }
                // Consume fuels
                ItemStack fuelStack = ItemUtil.getStack(blockEntity.inventoryHandler, GeneratorMenu.SLOT_FUEL);
                if (!blockEntity.isLit() && !fuelStack.isEmpty() && blockEntity.inventoryHandler.isValid(GeneratorMenu.SLOT_FUEL, ItemResource.of(fuelStack)) && blockEntity.energyHandler.getAmountAsInt() < blockEntity.energyHandler.getCapacityAsInt()) {
                    Pair<Float, Integer> rate = blockEntity.generator.getGenerationRateForItem(blockEntity.level, fuelStack);

                    // Check if energy storage has room for the entire burn or is half full
                    boolean shouldBurn =
                            blockEntity.energyHandler.getAmountAsInt() < (blockEntity.energyHandler.getCapacityAsInt() / 2) ||
                            (rate.getFirst() * rate.getSecond()) <= (blockEntity.energyHandler.getCapacityAsInt() - blockEntity.energyHandler.getAmountAsInt());

                    if (shouldBurn) {
                        blockEntity.generator.setGenerationRate(rate.getFirst());
                        blockEntity.litTime = Config.SERVER.increasedConsumption.get() ? rate.getSecond() / blockEntity.modifier : rate.getSecond();

                        // Do burn
                        if (blockEntity.litTime == 0) {
                            blockEntity.litTime = (int) (Config.SERVER.increasedConsumption.get() ? blockEntity.generator.getConsumptionRate() / blockEntity.modifier : blockEntity.generator.getConsumptionRate());
                        }
                        blockEntity.litDuration = blockEntity.litTime;
                        if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                            ItemStack stripped = new ItemStack(fuelStack.is(Items.ENCHANTED_BOOK) ? Items.BOOK : fuelStack.getItem());
                            blockEntity.inventoryHandler.set(GeneratorMenu.SLOT_FUEL, ItemResource.of(stripped), stripped.getCount());
                        } else if (fuelStack.getCraftingRemainder() != null && !fuelStack.getCraftingRemainder().create().isEmpty() && fuelStack.getCount() == 1) {
                            ItemStack remaining = fuelStack.getCraftingRemainder().create();
                            blockEntity.inventoryHandler.set(GeneratorMenu.SLOT_FUEL, ItemResource.of(remaining), remaining.getCount());
                        } else {
                            try (Transaction tx = Transaction.openRoot()) {
                                blockEntity.inventoryHandler.extractInternal(GeneratorMenu.SLOT_FUEL, ItemResource.of(fuelStack), 1, tx);
                                tx.commit();
                            }
                        }
                    }
                }
                // Generate power
                if (blockEntity.isLit()) {
                    hasConsumedFuel.set(true);
                }
            } else if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) && blockEntity.energyHandler.getAmountAsInt() + inputPowerAmount <= blockEntity.energyHandler.getCapacityAsInt()) {
                var fluidStack = blockEntity.fluidInventory.getResource(0).toStack((int) blockEntity.fluidInventory.getAmountAsLong(0));
                Pair<Double, Double> rate = blockEntity.generator.getGenerationRateForFluid(fluidStack);
                if (rate != null) {
                    double fluidConsumeAmount = rate.getSecond() * tickRate * blockEntity.modifier;
                    if (blockEntity.fluidInventory.getAmountAsLong(0) >= fluidConsumeAmount) {
                        try (Transaction tx = Transaction.openRoot()) {
                            blockEntity.fluidInventory.extract(0, blockEntity.fluidInventory.getResource(0), (int) fluidConsumeAmount, tx);
                            tx.commit();
                        }
                        blockEntity.generator.setGenerationRate(rate.getFirst());
                        blockEntity.generator.setConsumptionRate(rate.getSecond());
                        hasConsumedFuel.set(true);
                    }
                }
            }

            if (hasConsumedFuel.get()) {
                inputPowerAmount = blockEntity.getGenerationRate() * tickRate; // recalculate
                // If the generated FE is not divisible by the tickRate, save the excess for next tick
                inputPowerAmount = (inputPowerAmount + blockEntity.remainder);
                int addedPower = (int) inputPowerAmount;
                blockEntity.remainder = inputPowerAmount - addedPower;

                try (Transaction tx = Transaction.openRoot()) {
                    blockEntity.energyHandler.insertInternal(addedPower, tx);
                    tx.commit();
                }
                blockEntity.setOn(true);
            } else {
                blockEntity.setOn(false);
            }

            blockEntity.sendOutPower((int) blockEntity.generator.getTransferRate() * tickRate * blockEntity.modifier);
            blockEntity.setChanged();
        }
    }

    public double getGenerationRate() {
        return generator.getGenerationRate() * this.modifier;
    }

    public double getConsumptionRate() {
        return generator.getConsumptionRate() * this.modifier;
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    private void setOn(boolean isOn) {
        if (level != null && !level.isClientSide()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.LIT, isOn));
        }
    }

    private void sendOutPower(int amount) {
        if (this.level != null) {
            AtomicInteger capacity = new AtomicInteger(energyHandler.getAmountAsInt());
            if (capacity.get() > 0) {
                AtomicBoolean dirty = new AtomicBoolean(false);

                if (generator.hasChargeSlot()) {
                    var chargeItem = ItemUtil.getStack(inventoryHandler, GeneratorMenu.SLOT_CHARGE);
                    if (!chargeItem.isEmpty()) {
                        var chargeItemHandler = ItemAccess.forHandlerIndex(inventoryHandler, GeneratorMenu.SLOT_CHARGE).getCapability(Capabilities.Energy.ITEM);
                        if (chargeItemHandler != null) {
                            int received = transferTo(chargeItemHandler, Math.min(capacity.get(), amount));
                            capacity.addAndGet(-received);
                            dirty.set(received > 0);
                        }
                    }
                }

                for (EnergyHandler handler : recipients) {
                    boolean doContinue = capacity.get() > 0;
                    if (doContinue) {
                        int received = transferTo(handler, Math.min(capacity.get(), amount));
                        capacity.addAndGet(-received);
                        if (received > 0) {
                            dirty.set(true);
                        }
                    }

                    if (!doContinue) {
                        break;
                    }
                }
                if (dirty.get()) {
                    this.setChanged();
                }
            }
        }
    }

    private int transferTo(EnergyHandler recipient, int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int received = recipient.insert(amount, tx);
            if (received > 0 && energyHandler.extract(received, tx) == received) {
                tx.commit();
                return received;
            }
        }
        return 0;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu(id, inventory, this);
    }

    public void refreshConnectedTileEntityCache() {
        if (level instanceof ServerLevel) {
            List<EnergyHandler> recipients = new ArrayList<>();
            Direction[] directions = Direction.values();
            for (Direction direction : directions) {
                EnergyHandler energyCap = level.getCapability(Capabilities.Energy.BLOCK, worldPosition.relative(direction), direction.getOpposite());
                if (energyCap != null) {
                    recipients.add(energyCap);
                }
            }
            this.recipients = recipients;
        }
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block." + GeneratorGalore.MODID + "." + generator.getId().getPath().toLowerCase(Locale.ENGLISH) + "_generator" + (this.modifier > 1 ? "_" + this.modifier + "x" : ""));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        litTime = input.getIntOr("litTime", 0);
        litDuration = input.getIntOr("litDuration", 0);
        double generationRate = input.getDoubleOr("generationRate", 0);
        if (generationRate > 0) {
            generator.setGenerationRate(generationRate);
            generator.setConsumptionRate(input.getDoubleOr("consumptionRate", generator.getConsumptionRate()));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("litTime", litTime);
        output.putInt("litDuration", litDuration);
        if (generator.getGenerationRate() != generator.getOriginalGenerationRate()) {
            output.putDouble("generationRate", generator.getGenerationRate());
        }
        if (generator.getConsumptionRate() != generator.getOriginalConsumptionRate()) {
            output.putDouble("consumptionRate", generator.getConsumptionRate());
        }
    }

    @Override
    public void savePacketNBT(ValueOutput output) {
        inventoryHandler.serialize(output.child("inv"));
        energyHandler.serialize(output.child("energy"));
        fluidInventory.serialize(output.child("item"));
    }

    @Override
    public void loadPacketNBT(ValueInput input) {
        input.child("inv").ifPresent(inventoryHandler::deserialize);
        input.child("energy").ifPresent(energyHandler::deserialize);
        input.child("item").ifPresent(fluidInventory::deserialize);

        // set item ID for screens
        if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            Fluid fluid = fluidInventory.getResource(0).getFluid();
            fluidId = BuiltInRegistries.FLUID.getId(fluid);
        }
    }
}
