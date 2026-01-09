package cy.jdkdigital.generatorgalore.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import cy.jdkdigital.generatorgalore.GeneratorGalore;

public class ModPackets {
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(GeneratorGalore.MODID).versioned("1.0");
        registrar.playToClient(FluidSyncPacket.TYPE, FluidSyncPacket.STREAM_CODEC, ModPackets::handle);
    }

    public static void handle(FluidSyncPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.pos()) instanceof cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity blockEntity) {
                blockEntity.fluidInventory.setFluid(packet.fluidStack());
            }
        });
    }

    public static void sendToClient(FluidSyncPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}