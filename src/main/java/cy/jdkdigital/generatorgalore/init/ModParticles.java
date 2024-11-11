package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.client.particle.RisingEnchantParticleType;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModParticles
{
    public static final DeferredHolder<ParticleType<?>, ParticleType<RisingEnchantParticleType>> RISING_ENCHANT_PARTICLE = GeneratorGalore.PARTICLE_TYPES.register("rising_enchant_particle", RisingEnchantParticleType::new);
}
