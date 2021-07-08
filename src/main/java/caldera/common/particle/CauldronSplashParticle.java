package caldera.common.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RainParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CauldronSplashParticle extends RainParticle {

    private CauldronSplashParticle(ClientWorld level, double x, double y, double z, double r, double g, double b) {
        super(level, x, y, z);
        gravity = 0.04F;

        rCol = (float) r;
        gCol = (float) g;
        bCol = (float) b;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double r, double g, double b) {
            CauldronSplashParticle particle = new CauldronSplashParticle(level, x, y, z, r, g, b);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
