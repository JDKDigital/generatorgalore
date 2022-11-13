package cy.jdkdigital.generatorgalore.common.block.entity;

import cy.jdkdigital.generatorgalore.Config;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.cap.ControlledEnergyStorage;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import cy.jdkdigital.generatorgalore.common.container.ManualItemHandler;
import cy.jdkdigital.generatorgalore.init.ModTags;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
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
    private final LazyOptional<ControlledEnergyStorage> energyHandler;
    private final LazyOptional<IItemHandlerModifiable> inventoryHandler;
    private final LazyOptional<IFluidHandler> fluidInventory;
    private List<IEnergyStorage> recipients = new ArrayList<>();
    private boolean hasLoaded = false;

    public GeneratorBlockEntity(GeneratorObject generator, BlockPos blockPos, BlockState blockState) {
        super(generator.getBlockEntityType().get(), blockPos, blockState);
        this.generator = generator;

        this.energyHandler = LazyOptional.of(() -> new ControlledEnergyStorage(generator.getBufferCapacity()));
        this.inventoryHandler = LazyOptional.of(() -> new ManualItemHandler(2) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot != GeneratorMenu.SLOT_FUEL) return false;

                if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                    return stack.is(ModTags.getItemTag(generator.getFuelTag()));
                }
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
                    return stack.getItem().getFoodProperties(stack, null) != null;
                }
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                    return EnchantmentHelper.getEnchantments(stack).size() > 0;
                }
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
                    return stack.getItem() instanceof PotionItem;
                }
                if (generator.getFuelList() != null) {
                    return generator.getFuelList().containsKey(ForgeRegistries.ITEMS.getKey(stack.getItem()));
                }
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        });
        this.fluidInventory = LazyOptional.of(() -> new FluidTank(10000) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                if (!generator.getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
                    return stack.getFluid().is(ModTags.getFluidTag(generator.getFuelTag()));
                }
                return super.isFluidValid(stack);
            }

            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                    fluidId = Registry.FLUID.getId(getFluid().getFluid());
                    setChanged();
                }
            }
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity blockEntity) {
        int tickRate = Config.SERVER.tickRate.get();

        if (!blockEntity.hasLoaded) {
            blockEntity.refreshConnectedTileEntityCache();
            blockEntity.hasLoaded = true;
        }
        if (++blockEntity.tickCounter % tickRate == 0) {
            blockEntity.energyHandler.ifPresent(energyHandler -> {
                double inputPowerAmount = blockEntity.generator.getGenerationRate() * tickRate;
                AtomicBoolean hasConsumedFuel = new AtomicBoolean(false);

                if (!blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
                    blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                        if (blockEntity.isLit()) {
                            blockEntity.litTime = Math.max(0, blockEntity.litTime - tickRate);
                        }
                        // Consume fuels
                        ItemStack fuelStack = itemHandler.getStackInSlot(GeneratorMenu.SLOT_FUEL);
                        if (!blockEntity.isLit() && !fuelStack.isEmpty() && itemHandler.isItemValid(GeneratorMenu.SLOT_FUEL, fuelStack) && energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored()) {
                            if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
                                var rate = GeneratorUtil.calculateEnchantmentGenerationRate(blockEntity.generator, fuelStack);
                                blockEntity.generator.setGenerationRate(rate.getFirst());
                                blockEntity.litTime = rate.getSecond();
                            } else if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
                                var rate = GeneratorUtil.calculatePotionGenerationRate(blockEntity.generator, fuelStack);
                                blockEntity.generator.setGenerationRate(rate.getFirst());
                                blockEntity.litTime = rate.getSecond();
                            } else if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
                                var rate = GeneratorUtil.calculateFoodGenerationRate(blockEntity.generator, fuelStack);
                                blockEntity.generator.setGenerationRate(rate.getFirst());
                                blockEntity.litTime = rate.getSecond();
                            } else if (blockEntity.generator.getFuelList() != null) {
                                var fuel = blockEntity.generator.getFuelList().get(ForgeRegistries.ITEMS.getKey(fuelStack.getItem()));
                                blockEntity.generator.setGenerationRate(fuel.rate() > 0 ? fuel.rate() : blockEntity.generator.getOriginalGenerationRate());
                                blockEntity.litTime = fuel.burnTime();
                            } else {
                                blockEntity.litTime = (int) (ForgeHooks.getBurnTime(fuelStack, null) * blockEntity.generator.getConsumptionRate());
                            }

                            // Do burn
                            if (blockEntity.litTime == 0) {
                                blockEntity.litTime = (int) blockEntity.generator.getConsumptionRate();
                            }
                            blockEntity.litDuration = blockEntity.litTime;
                            if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT) && itemHandler instanceof ItemStackHandler stackHandler) {
                                // strip enchantments
                                stackHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, new ItemStack(fuelStack.getItem() instanceof EnchantedBookItem ? Items.BOOK : fuelStack.getItem()));
                            } else if (!fuelStack.getCraftingRemainingItem().isEmpty() && fuelStack.getCount() == 1 && itemHandler instanceof ItemStackHandler stackHandler) {
                                stackHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, fuelStack.getCraftingRemainingItem());
                            } else {
                                fuelStack.shrink(1);
                            }
                        }
                        // Generate power
                        if (blockEntity.isLit()) {
                            hasConsumedFuel.set(true);
                        }
                    });
                } else if (blockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) && energyHandler.getEnergyStored() + inputPowerAmount <= energyHandler.getMaxEnergyStored()) {
                    blockEntity.fluidInventory.ifPresent(fluidHandler -> blockEntity.energyHandler.ifPresent(handler -> {
                        double fluidConsumeAmount = blockEntity.generator.getConsumptionRate() * tickRate;
                        if (fluidHandler.getFluidInTank(0).getAmount() >= fluidConsumeAmount) {
                            fluidHandler.drain((int) fluidConsumeAmount, IFluidHandler.FluidAction.EXECUTE);
                            hasConsumedFuel.set(true);
                        }
                    }));
                }

                if (hasConsumedFuel.get()) {
                    inputPowerAmount = blockEntity.generator.getGenerationRate() * tickRate; // recalculate
                    // If the generated FE is not divisible by the tickRate, save the excess for next tick
                    inputPowerAmount = (inputPowerAmount + blockEntity.remainder);
                    int addedPower = (int) inputPowerAmount;
                    blockEntity.remainder = inputPowerAmount - addedPower;

                    energyHandler.receiveEnergy(addedPower, false, true);
                    blockEntity.setOn(true);
                } else {
                    blockEntity.setOn(false);
                }
            });

            blockEntity.sendOutPower((int) blockEntity.generator.getTransferRate() * tickRate);
        }
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
            energyHandler.ifPresent(energyHandler -> {
                AtomicInteger capacity = new AtomicInteger(energyHandler.getEnergyStored());
                if (capacity.get() > 0) {
                    AtomicBoolean dirty = new AtomicBoolean(false);
                    for (IEnergyStorage handler : recipients) {
                        boolean doContinue = true;
                        if (handler.canReceive()) {
                            int received = handler.receiveEnergy(Math.min(capacity.get(), amount), false);
                            capacity.addAndGet(-received);
                            energyHandler.extractEnergy(received, false);
                            dirty.set(true);
                            doContinue = capacity.get() > 0;
                        }

                        if (!doContinue) {
                            break;
                        }
                    }
                    if (dirty.get()) {
                        this.setChanged();
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu(id, inventory, this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryHandler.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER && generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            return fluidInventory.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void refreshConnectedTileEntityCache() {
        if (level instanceof ServerLevel) {
            List<IEnergyStorage> recipients = new ArrayList<>();
            Direction[] directions = Direction.values();
            for (Direction direction : directions) {
                BlockEntity te = level.getBlockEntity(worldPosition.relative(direction));
                if (te != null) {
                    te.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(recipients::add);
                }
            }
            this.recipients = recipients;
        }
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("block." + GeneratorGalore.MODID + "." + generator.getId().getPath().toLowerCase(Locale.ENGLISH) + "_generator");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        litTime = tag.getInt("litTime");
        litDuration = tag.getInt("litDuration");
        if (tag.contains("generationRate")) {
            generator.setGenerationRate(tag.getDouble("generationRate"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("litTime", litTime);
        tag.putInt("litDuration", litDuration);
        if (generator.getGenerationRate() != generator.getOriginalGenerationRate()) {
            tag.putDouble("generationRate", generator.getGenerationRate());
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag) {
        super.loadPacketNBT(tag);

        // set fluid ID for screens
        if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID)) {
            Fluid fluid = fluidInventory.map(fluidHandler -> fluidHandler.getFluidInTank(0).getFluid()).orElse(Fluids.EMPTY);
            fluidId = Registry.FLUID.getId(fluid);
        }
    }
}
