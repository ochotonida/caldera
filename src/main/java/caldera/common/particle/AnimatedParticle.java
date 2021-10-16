package caldera.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public abstract class AnimatedParticle extends TextureSheetParticle {

    protected final SpriteSet sprite;

    protected AnimatedParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite) {
        super(level, x, y, z);
        this.sprite = sprite;
        setSpriteFromAge();
    }

    protected AnimatedParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
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
