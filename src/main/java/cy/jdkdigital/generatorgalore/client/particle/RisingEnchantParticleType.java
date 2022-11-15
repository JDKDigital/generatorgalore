package cy.jdkdigital.generatorgalore.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class RisingEnchantParticleType extends ParticleType<RisingEnchantParticleType> implements ParticleOptions
{
    private static final Deserializer<RisingEnchantParticleType> DESERIALIZER = new Deserializer<>()
    {
        @Nonnull
        @Override
        public RisingEnchantParticleType fromCommand(@Nonnull ParticleType<RisingEnchantParticleType> particleType, @Nonnull StringReader stringReader) throws CommandSyntaxException {
            return (RisingEnchantParticleType) particleType;
        }

        @Nonnull
        @Override
        public RisingEnchantParticleType fromNetwork(@Nonnull ParticleType<RisingEnchantParticleType> particleType, @Nonnull FriendlyByteBuf buffer) {
            return (RisingEnchantParticleType) particleType;
        }
    };

    public RisingEnchantParticleType() {
        super(false, DESERIALIZER);
    }

    private final Codec<RisingEnchantParticleType> codec = Codec.unit(this::getType);

    @Override
    public RisingEnchantParticleType getType() {
        return this;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {

    }

    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this).toString();
    }

    @Override
    public Codec<RisingEnchantParticleType> codec() {
        return codec;
    }
}
