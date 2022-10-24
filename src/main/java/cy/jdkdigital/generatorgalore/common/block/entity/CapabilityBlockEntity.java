package cy.jdkdigital.generatorgalore.common.block.entity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public abstract class CapabilityBlockEntity extends BlockEntity implements MenuProvider, Nameable
{
    public CapabilityBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.loadPacketNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.savePacketNBT(tag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithId();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.loadPacketNBT(pkt.getTag());
        if (level instanceof ClientLevel) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }

    public void savePacketNBT(CompoundTag tag) {
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            CompoundTag compound = ((ItemStackHandler) inv).serializeNBT();
            tag.put("inv", compound);
        });

        getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
            tag.put("energy", ((EnergyStorage) handler).serializeNBT());
        });

        getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluid -> {
            CompoundTag nbt = new CompoundTag();
            ((FluidTank) fluid).writeToNBT(nbt);
            tag.put("fluid", nbt);
        });
    }

    public void loadPacketNBT(CompoundTag tag) {
        if (tag.contains("inv")) {
            getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> ((ItemStackHandler) inv).deserializeNBT(tag.getCompound("inv")));
        }

        if (tag.contains("energy")) {
            getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
                ((EnergyStorage) handler).deserializeNBT(tag.get("energy"));
            });
        }

        if (tag.contains("fluid")) {
            getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluid -> {
                ((FluidTank) fluid).readFromNBT(tag.getCompound("fluid"));
            });
        }
    }
}
