package caldera.common.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;

public abstract class AnimatedParticle extends SpriteTexturedParticle {

    protected final IAnimatedSprite sprite;

    protected AnimatedParticle(ClientWorld level, double x, double y, double z, IAnimatedSprite sprite) {
        super(level, x, y, z);
        this.sprite = sprite;
        setSpriteFromAge();
    }

    protected AnimatedParticle(ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprite = sprite;
        setSpriteFromAge();
    }

    @Override
    public void tick() {
        setSpriteFromAge();
        super.tick();
    }

    public void setSpriteFromAge() {
        setSpriteFromAge(sprite);
    }
}
