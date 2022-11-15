package cy.jdkdigital.generatorgalore.client.event;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.client.particle.RisingEnchantParticle;
import cy.jdkdigital.generatorgalore.common.container.GeneratorScreen;
import cy.jdkdigital.generatorgalore.init.ModParticles;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GeneratorGalore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
                MenuScreens.register(generatorObject.getMenuType().get(), GeneratorScreen::new);
                ItemBlockRenderTypes.setRenderLayer(generatorObject.getBlockSupplier().get(), RenderType.cutoutMipped());
            });
        });
    }

    @SubscribeEvent
    public static void registerParticles(final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticles.RISING_ENCHANT_PARTICLE.get(), RisingEnchantParticle.Provider::new);
    }
}
