package caldera.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CauldronSplashParticle extends WaterDropParticle {

    private CauldronSplashParticle(ClientLevel level, double x, double y, double z, double r, double g, double b) {
        super(level, x, y, z);
        gravity = 0.04F;

        rCol = (float) r;
        gCol = (float) g;
        bCol = (float) b;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double r, double g, double b) {
            CauldronSplashParticle particle = new CauldronSplashParticle(level, x, y, z, r, g, b);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
