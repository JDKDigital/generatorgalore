package cy.jdkdigital.generatorgalore.client.event;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.container.GeneratorScreen;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = GeneratorGalore.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void onClientSetup(RegisterMenuScreensEvent event) {
        GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
            event.register(generatorObject.getMenuType().get(), GeneratorScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
//        event.registerSpecial(ModParticles.RISING_ENCHANT_PARTICLE.get(), RisingEnchantParticle.Provider::new);
    }
}
