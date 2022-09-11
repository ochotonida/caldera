package caldera.common.brew.generic.component.trigger;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.block.cauldron.CauldronBlockEntity;
import caldera.common.init.ModTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class TriggerEvents {

    public TriggerEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
    }

    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();
        BlockPos pos = new BlockPos(entity.position());
        CauldronBlockEntity.getCauldron(entity.level, pos)
                .map(CauldronBlockEntity::getController)
                .filter(cauldron -> cauldron.isInsideBrew(entity))
                .flatMap(Cauldron::getGenericBrew)
                .ifPresent(brew -> {
                    LivingEntity killer = null;
                    if (event.getSource().getEntity() instanceof LivingEntity) {
                        killer = (LivingEntity) event.getSource().getEntity();
                    }
                    ModTriggers.ENTITY_DIED.get().trigger(brew, entity, killer);
        });
    }
}
