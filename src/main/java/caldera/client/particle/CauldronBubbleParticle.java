package caldera.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class CauldronBubbleParticle extends SpriteTexturedParticle {

    protected CauldronBubbleParticle(ClientWorld level, double x, double y, double z, double r, double g, double b) {
        super(level, x, y, z);
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
            CauldronBubbleParticle particle = new CauldronBubbleParticle(level, x, y, z, r, g, b);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
