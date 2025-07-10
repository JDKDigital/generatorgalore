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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
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
    public final FluidTank fluidInventory;
    private List<IEnergyStorage> recipients = new ArrayList<>();
    private boolean hasLoaded = false;

    public GeneratorBlockEntity(GeneratorObject generator, BlockPos blockPos, BlockState blockState) {
        super(generator.getBlockEntityType().get(), blockPos, blockState);
        this.generator = generator;

        this.modifier = blockState.getBlock() instanceof Generator generatorBlock ? generatorBlock.getModifier() : 1;
        this.energyHandler = new ControlledEnergyStorage(generator.getBufferCapacity() * this.modifier);
        this.inventoryHandler = new ManualItemHandler(2) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == GeneratorMenu.SLOT_CHARGE) {
                    return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
                }

                return generator.isValidFuelItem(stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.fluidInventory = new FluidTank(10000) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return generator.isValidFuelFluid(stack) || super.isFluidValid(stack);
            }

            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                    fluidId = BuiltInRegistries.FLUID.getId(getFluid().getFluid());
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
                ItemStack fuelStack = blockEntity.inventoryHandler.getStackInSlot(GeneratorMenu.SLOT_FUEL);
                if (!blockEntity.isLit() && !fuelStack.isEmpty() && blockEntity.inventoryHandler.isItemValid(GeneratorMenu.SLOT_FUEL, fuelStack) && blockEntity.energyHandler.getEnergyStored() < blockEntity.energyHandler.getMaxEnergyStored()) {
                    Pair<Float, Integer> rate = blockEntity.generator.getGenerationRateForItem(blockEntity.level, fuelStack);

                    // Check if energy storage has room for the entire burn or is half full
                    boolean shouldBurn =
                            blockEntity.energyHandler.getEnergyStored() < (blockEntity.energyHandler.getMaxEnergyStored() / 2) ||
                            (rate.getFirst() * rate.getSecond()) <= (blockEntity.energyHandler.getMaxEnergyStored() - blockEntity.energyHandler.getEnergyStored());

                    if (shouldBurn) {
                        blockEntity.generator.setGenerationRate(rate.getFirst());
                        blockEntity.litTime = Config.SERVER.increasedConsumption.get() ? rate.getSecond() / blockEntity.modifier : rate.getSecond();

                        // Do burn
                        if (blockEntity.litTime == 0) {
                            blockEntity.litTime = (int) (Config.SERVER.increasedConsumption.get() ? blockEntity.generator.getConsumptionRate() / blockEntity.modifier : blockEntity.generator.getConsumptionRate());
                        }
                        blockEntity.litDuration = blockEntity.litTime;
                        if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                            // strip enchantments
                            blockEntity.inventoryHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, new ItemStack(fuelStack.getItem() instanceof EnchantedBookItem ? Items.BOOK : fuelStack.getItem()));
                        } else if (!fuelStack.getCraftingRemainingItem().isEmpty() && fuelStack.getCount() == 1) {
                            blockEntity.inventoryHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, fuelStack.getCraftingRemainingItem());
                        } else {
                            fuelStack.shrink(1);
                        }
                    }
                }
                // Generate power
                if (blockEntity.isLit()) {
                    hasConsumedFuel.set(true);
                }
            } else if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) && blockEntity.energyHandler.getEnergyStored() + inputPowerAmount <= blockEntity.energyHandler.getMaxEnergyStored()) {
                var fluidStack = blockEntity.fluidInventory.getFluidInTank(0);
                Pair<Double, Double> rate = blockEntity.generator.getGenerationRateForFluid(fluidStack);
                double fluidConsumeAmount = rate.getSecond() * tickRate * blockEntity.modifier;
                if (blockEntity.fluidInventory.getFluidInTank(0).getAmount() >= fluidConsumeAmount) {
                    blockEntity.fluidInventory.drain((int) fluidConsumeAmount, IFluidHandler.FluidAction.EXECUTE);
                    blockEntity.generator.setGenerationRate(rate.getFirst());
                    blockEntity.generator.setConsumptionRate(rate.getSecond());
                    hasConsumedFuel.set(true);
                }
            }

            if (hasConsumedFuel.get()) {
                inputPowerAmount = blockEntity.getGenerationRate() * tickRate; // recalculate
                // If the generated FE is not divisible by the tickRate, save the excess for next tick
                inputPowerAmount = (inputPowerAmount + blockEntity.remainder);
                int addedPower = (int) inputPowerAmount;
                blockEntity.remainder = inputPowerAmount - addedPower;

                blockEntity.energyHandler.receiveEnergy(addedPower, false, true);
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
        if (level != null && !level.isClientSide) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.LIT, isOn));
        }
    }

    private void sendOutPower(int amount) {
        if (this.level != null) {
            AtomicInteger capacity = new AtomicInteger(energyHandler.getEnergyStored());
            if (capacity.get() > 0) {
                AtomicBoolean dirty = new AtomicBoolean(false);

                if (generator.hasChargeSlot()) {
                    var chargeItem = inventoryHandler.getStackInSlot(GeneratorMenu.SLOT_CHARGE);
                    if (!chargeItem.isEmpty()) {
                        var chargeItemHandler = chargeItem.getCapability(Capabilities.EnergyStorage.ITEM);
                        if (chargeItemHandler != null) {
                            int received = chargeItemHandler.receiveEnergy(Math.min(capacity.get(), amount), false);
                            capacity.addAndGet(-received);
                            energyHandler.extractEnergy(received, false);
                            dirty.set(true);
                        }
                    }
                }

                for (IEnergyStorage handler : recipients) {
                    boolean doContinue = capacity.get() > 0;
                    if (handler.canReceive() && doContinue) {
                        int received = handler.receiveEnergy(Math.min(capacity.get(), amount), false);
                        capacity.addAndGet(-received);
                        energyHandler.extractEnergy(received, false);
                        dirty.set(true);
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu(id, inventory, this);
    }

    public void refreshConnectedTileEntityCache() {
        if (level instanceof ServerLevel) {
            List<IEnergyStorage> recipients = new ArrayList<>();
            Direction[] directions = Direction.values();
            for (Direction direction : directions) {
                IEnergyStorage energyCap = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(direction), direction.getOpposite());
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
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        litTime = pTag.getInt("litTime");
        litDuration = pTag.getInt("litDuration");
        if (pTag.contains("generationRate")) {
            generator.setGenerationRate(pTag.getDouble("generationRate"));
            generator.setConsumptionRate(pTag.getDouble("consumptionRate"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putInt("litTime", litTime);
        pTag.putInt("litDuration", litDuration);
        if (generator.getGenerationRate() != generator.getOriginalGenerationRate()) {
            pTag.putDouble("generationRate", generator.getGenerationRate());
        }
        if (generator.getConsumptionRate() != generator.getOriginalConsumptionRate()) {
            pTag.putDouble("consumptionRate", generator.getConsumptionRate());
        }
    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inv", inventoryHandler.serializeNBT(pRegistries));

        tag.put("energy", energyHandler.serializeNBT(pRegistries));

        CompoundTag nbt = new CompoundTag();
        fluidInventory.writeToNBT(pRegistries, nbt);
        tag.put("item", nbt);
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider pRegistries) {
        if (tag.contains("inv")) {
            inventoryHandler.deserializeNBT(pRegistries, tag.getCompound("inv"));
        }

        if (tag.contains("energy")) {
            energyHandler.deserializeNBT(pRegistries, tag.get("energy"));
        }

        if (tag.contains("item")) {
            fluidInventory.readFromNBT(pRegistries, tag.getCompound("item"));
        }

        // set item ID for screens
        if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            Fluid fluid = fluidInventory.getFluidInTank(0).getFluid();
            fluidId = BuiltInRegistries.FLUID.getId(fluid);
        }
    }
}
