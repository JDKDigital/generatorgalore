package cy.jdkdigital.generatorgalore.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.fluids.FluidStack;
import cy.jdkdigital.generatorgalore.GeneratorGalore;

public record FluidSyncPacket(BlockPos pos, FluidStack fluidStack) implements CustomPacketPayload {
    public static final Type<FluidSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "fluid_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FluidSyncPacket::pos,
            FluidStack.STREAM_CODEC,
            FluidSyncPacket::fluidStack,
            FluidSyncPacket::new
    );

    @Override
    public Type<FluidSyncPacket> type() {
        return TYPE;
    }
}