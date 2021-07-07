package caldera.common.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class CauldronBubbleParticle extends AnimatedParticle {

    protected CauldronBubbleParticle(ClientWorld level, double x, double y, double z, double r, double g, double b, IAnimatedSprite sprite) {
        super(level, x, y, z, sprite);

        rCol = (float) r;
        gCol = (float) g;
        bCol = (float) b;

        lifetime = 15;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double r, double g, double b) {
            CauldronBubbleParticle particle = new CauldronBubbleParticle(level, x, y, z, r, g, b, sprite);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
