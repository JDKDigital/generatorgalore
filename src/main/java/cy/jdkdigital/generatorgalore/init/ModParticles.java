package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.client.particle.RisingEnchantParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = GeneratorGalore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModParticles
{
    public static final RegistryObject<ParticleType<RisingEnchantParticleType>> RISING_ENCHANT_PARTICLE = GeneratorGalore.PARTICLE_TYPES.register("rising_enchant_particle", RisingEnchantParticleType::new);
}
