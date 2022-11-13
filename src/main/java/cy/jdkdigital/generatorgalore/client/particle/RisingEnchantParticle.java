package cy.jdkdigital.generatorgalore.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RisingEnchantParticle extends TextureSheetParticle
{
    public RisingEnchantParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.xd *= 0.8F;
        this.yd *= 0.8F;
        this.zd *= 0.8F;
        this.yd = this.random.nextFloat() * 0.4F + 0.05F;
        this.quadSize *= this.random.nextFloat() * 1.1F + 0.2F;
        this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f);
    }

    @Override
    public int getLightColor(float pPartialTick) {
        int i = super.getLightColor(pPartialTick);
        float f = (float)this.age / (float)this.lifetime;
        f *= f;
        f *= f;
        int j = i & 255;
        int k = i >> 16 & 255;
        k += (int)(f * 15.0F * 16.0F);
        if (k > 240) {
            k = 240;
        }

        return j | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float f = (float)this.age / (float)this.lifetime;
            if (this.random.nextFloat() > f) {
                this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<RisingEnchantParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(RisingEnchantParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            RisingEnchantParticle particle = new RisingEnchantParticle(pLevel, pX, pY, pZ);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
