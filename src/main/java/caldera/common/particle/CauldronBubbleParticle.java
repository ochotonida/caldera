package caldera.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class CauldronBubbleParticle extends AnimatedParticle {

    protected CauldronBubbleParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet sprite) {
        super(level, x, y, z, sprite);

        rCol = (float) r;
        gCol = (float) g;
        bCol = (float) b;

        lifetime = 15;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double r, double g, double b) {
            CauldronBubbleParticle particle = new CauldronBubbleParticle(level, x, y, z, r, g, b, sprite);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
