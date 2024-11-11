package cy.jdkdigital.generatorgalore.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class RisingEnchantParticleType extends ParticleType<RisingEnchantParticleType> implements ParticleOptions
{
    public RisingEnchantParticleType() {
        super(false);
    }

    private final MapCodec<RisingEnchantParticleType> CODEC = MapCodec.unit(this::getType);
    private final StreamCodec<RegistryFriendlyByteBuf, RisingEnchantParticleType> STREAM_CODEC = StreamCodec.unit(this);

    @Override
    public RisingEnchantParticleType getType() {
        return this;
    }

    @Override
    public MapCodec<RisingEnchantParticleType> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, RisingEnchantParticleType> streamCodec() {
        return STREAM_CODEC;
    }
}
