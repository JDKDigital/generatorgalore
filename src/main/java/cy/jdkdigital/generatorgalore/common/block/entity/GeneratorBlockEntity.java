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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import cy.jdkdigital.generatorgalore.network.FluidSyncPacket;
import cy.jdkdigital.generatorgalore.network.ModPackets;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final int generationRate;
    private final int consumptionRate;
    public final ControlledEnergyStorage energyHandler;
    public final ManualItemHandler inventoryHandler;
    public final FluidTank fluidInventory;
    private boolean hasLoaded = false;

    public GeneratorBlockEntity(GeneratorObject generator, BlockPos blockPos, BlockState blockState) {
        super(generator.getBlockEntityType().get(), blockPos, blockState);
        this.generator = generator;
        this.modifier = blockState.getBlock() instanceof Generator generatorBlock ? generatorBlock.getModifier() : 1;
        this.generationRate = (int) (generator.getOriginalGenerationRate() * this.modifier);
        this.consumptionRate = (int) (generator.getOriginalConsumptionRate() * this.modifier);
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
                    return generator.isValidFuelFluid(stack);
                }

                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                        fluidId = BuiltInRegistries.FLUID.getId(getFluid().getFluid());
                        syncFluidToClients();
                        setChanged();
                    }
                }
            };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity blockEntity) {
        int tickRate = Config.SERVER.tickRate.get();

        if (!blockEntity.hasLoaded) {
            blockEntity.hasLoaded = true;
        }

        if (++blockEntity.tickCounter % tickRate == 0) {
            // Cache generation and consumption rates to avoid redundant calculations
            int generationRate = blockEntity.getGenerationRate();
            int consumptionRate = blockEntity.getConsumptionRate();
            int inputPowerAmount = generationRate * tickRate;
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
                        blockEntity.generator.setGenerationRate(rate.getFirst().intValue());
                        blockEntity.litTime = Config.SERVER.increasedConsumption.get() ? rate.getSecond() / blockEntity.modifier : rate.getSecond();

                        // Do burn
                        if (blockEntity.litTime == 0) {
                            blockEntity.litTime = Config.SERVER.increasedConsumption.get() ? consumptionRate / blockEntity.modifier : consumptionRate;
                        }
                        blockEntity.litDuration = blockEntity.litTime;
                        if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                            // strip enchantments
                            blockEntity.inventoryHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, new ItemStack(fuelStack.getItem() instanceof EnchantedBookItem ? Items.BOOK : fuelStack.getItem()));
                        } else if (fuelStack.hasCraftingRemainingItem() && fuelStack.getCount() == 1) {
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
                FluidStack fluidStack = blockEntity.fluidInventory.getFluidInTank(0);
                Pair<Integer, Integer> rate = blockEntity.generator.getGenerationRateForFluid(fluidStack);
                if (rate != null) {
                    int fluidConsumeAmount = rate.getSecond() * tickRate * blockEntity.modifier;
                    if (blockEntity.fluidInventory.getFluidInTank(0).getAmount() >= fluidConsumeAmount) {
                        blockEntity.fluidInventory.drain(fluidConsumeAmount, IFluidHandler.FluidAction.EXECUTE);
                        blockEntity.syncFluidToClients();
                        blockEntity.generator.setGenerationRate(rate.getFirst());
                        blockEntity.generator.setConsumptionRate(rate.getSecond());
                        hasConsumedFuel.set(true);
                    }
                }
            }

            if (hasConsumedFuel.get()) {
                inputPowerAmount = generationRate * tickRate; // recalculate with cached rate
                // If the generated FE is not divisible by the tickRate, save the excess for next tick
                double tempPowerAmount = inputPowerAmount + blockEntity.remainder;
                int addedPower = (int) tempPowerAmount;
                blockEntity.remainder = tempPowerAmount - addedPower;

                blockEntity.energyHandler.receiveEnergy(addedPower, false, true);
                blockEntity.setOn(true);
            } else {
                blockEntity.setOn(false);
            }

            blockEntity.sendOutPower((int) blockEntity.generator.getTransferRate() * tickRate * blockEntity.modifier);
            blockEntity.setChanged();
        }
    }

    public int getGenerationRate() {
        return this.generationRate;
    }

    public int getConsumptionRate() {
        return this.consumptionRate;
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

                // Lazy evaluation - only process charge slot if generator has charge slot
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

                // Direct neighbor capability query instead of cached list
                if (capacity.get() > 0) {
                    Direction[] directions = Direction.values();
                    for (Direction direction : directions) {
                        var energyCap = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(direction), direction.getOpposite());
                        if (energyCap != null && energyCap.canReceive()) {
                            int received = energyCap.receiveEnergy(Math.min(capacity.get(), amount), false);
                            capacity.addAndGet(-received);
                            energyHandler.extractEnergy(received, false);
                            dirty.set(true);

                            if (capacity.get() <= 0) {
                                break;
                            }
                        }
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

    public void syncFluidToClients() {
        if (level != null && !level.isClientSide && generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            FluidStack fluidStack = fluidInventory.getFluidInTank(0);
            FluidSyncPacket packet = new FluidSyncPacket(worldPosition, fluidStack);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                ModPackets.sendToClient(packet, player);
            }
        }
    }

    // Removed refreshConnectedTileEntityCache() method and recipients list
    // as we now use direct capability queries per tick

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
            generator.setGenerationRate(pTag.getInt("generationRate"));
            generator.setConsumptionRate(pTag.getInt("consumptionRate"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putInt("litTime", litTime);
        pTag.putInt("litDuration", litDuration);
        if (generator.getGenerationRate() != generator.getOriginalGenerationRate()) {
            pTag.putInt("generationRate", generator.getGenerationRate());
        }
        if (generator.getConsumptionRate() != generator.getOriginalConsumptionRate()) {
            pTag.putInt("consumptionRate", generator.getConsumptionRate());
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

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        // Store energy and fluid data in components
        components.set(GeneratorGalore.ENERGY_COMPONENT.get(), energyHandler.getEnergyStored());
        components.set(GeneratorGalore.FLUID_COMPONENT.get(), fluidInventory.getFluid());
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        // Restore energy and fluid data from components
        Integer energy = componentInput.get(GeneratorGalore.ENERGY_COMPONENT.get());
        FluidStack fluid = componentInput.get(GeneratorGalore.FLUID_COMPONENT.get());

        if (energy != null) {
            // Use receiveEnergy with internal flag to set energy directly
            energyHandler.receiveEnergy(energy, false, true);
        }

        if (fluid != null && !fluid.isEmpty() && generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            fluidInventory.setFluid(fluid);
        }
    }
}
