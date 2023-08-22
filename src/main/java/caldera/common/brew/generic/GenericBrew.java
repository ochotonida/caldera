package caldera.common.brew.generic;

import caldera.Caldera;
import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.init.ModTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GenericBrew extends Brew {

    private final ColorInfo colorInfo = new ColorInfo();
    private final Map<String, Effect> effects = new LinkedHashMap<>();

    public GenericBrew(BrewType brewType, Cauldron cauldron) {
        super(brewType, cauldron);
    }

    @Override
    public GenericBrewType getType() {
        return (GenericBrewType) super.getType();
    }

    private List<Effect> getEffects() {
        return new ArrayList<>(effects.values());
    }

    @Override
    public int getColor(float partialTicks) {
        return colorInfo.getColor(partialTicks);
    }

    public void changeColor(int newColor, int transitionTime) {
        colorInfo.changeColor(newColor, transitionTime);
        getCauldron().setChanged();
    }

    @Override
    public void onBrewed() {
        ModTriggers.BREW_CREATED.get().trigger(this);
    }

    @Override
    public void onPlayerAboutToDestroy(Player player) {
        if (getCauldron().getLevel() != null && !getCauldron().getLevel().isClientSide()) {
            ModTriggers.CAULDRON_BROKEN.get().trigger(this, player);
        }
    }

    /**
     * Applies the consumer to each active effect, in the order the effects were added to the brew.
     * The consumer will not be applied to effects that are added while executing the method, and any
     * effects that are removed while executing this method will be skipped.
     */
    private void forEachEffect(Consumer<Effect> consumer) {
        for (Effect effect : getEffects()) {
            if (effects.containsValue(effect)) {
                consumer.accept(effect);
            }
        }
    }

    @Override
    public void tick() {
        if (!colorInfo.hasSettled()) {
            getCauldron().setChanged();
        }
        colorInfo.tick();

        forEachEffect(Effect::tick);
    }

    @Override
    public void onEntityInside(Entity entity, double yOffset) {
        if (getCauldron().getLevel() == null) {
            return;
        }
        Level level = getCauldron().getLevel();

        if (!level.isClientSide() && entity instanceof ItemEntity item && item.getDeltaMovement().y() <= 0 && yOffset < 0.2) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            forEachEffect(effect -> {
                if (!item.getItem().isEmpty()) {
                    effect.consumeItem(item);
                }
            });
            if (item.getItem().isEmpty()) {
                return;
            }
            getCauldron().discardItem(item.getItem(), item.getDeltaMovement());
        }
    }

    /**
     * Executes the action with the specified identifier
     */
    public void executeAction(String identifier) {
        Action action = getType().getAction(identifier);
        if (action != null) {
            action.accept(this);
        } else {
            throw new IllegalArgumentException("Invalid action identifier: " + identifier);
        }
    }

    /**
     * Starts the effect with the specified identifier.
     */
    public void startEffect(String identifier) {
        Effect effect = getType().getEffects().get(identifier).create(this);
        effects.put(identifier, effect);
        getCauldron().setChanged();
        sendEffectAdded(identifier);
    }

    /**
     * Removes the effect with the specified identifier, does nothing if no such effect is active.
     */
    public void removeEffect(String identifier) {
        effects.remove(identifier);
        getCauldron().setChanged();
        sendEffectRemoved(identifier);
    }

    /**
     * Removes the effect with the specified identifier,
     * and triggers the effect ended trigger for the specified identifier.
     *
     * If no effect with the specified identifier exists, nothing happens.
     *
     * @throws IllegalStateException the method was called from the client
     */
    public void endEffect(String identifier) {
        if (getCauldron().getLevel() != null && getCauldron().getLevel().isClientSide()) {
            throw new IllegalStateException("Effects should only be ended server-side");
        }
        if (effects.containsKey(identifier)) {
            removeEffect(identifier);
            ModTriggers.EFFECT_ENDED.get().trigger(this, identifier);
        }
    }

    public void sendColorUpdate() {
        CompoundTag tag = new CompoundTag();
        tag.put("ColorInfo", colorInfo.save());
        sendUpdate(tag);
    }

    public void sendEffectAdded(String identifier) {
        CompoundTag tag = new CompoundTag();
        tag.put("AddedEffect", saveEffect(identifier));
        sendUpdate(tag);
    }

    public void sendEffectRemoved(String identifier) {
        CompoundTag tag = new CompoundTag();
        tag.putString("RemovedEffect", identifier);
        sendUpdate(tag);
    }

    @Override
    public void onUpdate(CompoundTag tag) {
        if (tag.contains("ColorInfo")) {
            colorInfo.load(tag.getCompound("ColorInfo"));
        } else if (tag.contains("AddedEffect")) {
            loadEffect(tag.getCompound("AddedEffect"));
        } else if (tag.contains("RemovedEffect")) {
            effects.remove(tag.getString("RemovedEffect"));
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("ColorInfo", colorInfo.save());
        tag.put("ActiveEffects", saveEffects());
    }

    @Override
    public void load(CompoundTag tag) {
        colorInfo.load(tag.getCompound("ColorInfo"));
        loadEffects(tag.getList("ActiveEffects", Tag.TAG_COMPOUND));
    }

    private ListTag saveEffects() {
        ListTag result = new ListTag();
        effects.keySet().forEach(identifier -> result.add(saveEffect(identifier)));
        return result;
    }

    private CompoundTag saveEffect(String identifier) {
        CompoundTag tag = new CompoundTag();
        effects.get(identifier).save(tag);
        tag.putString("Identifier", identifier);
        return tag;
    }

    private void loadEffects(ListTag list) {
        effects.clear();
        for (Tag element : list) {
            loadEffect((CompoundTag) element);
        }
    }

    private void loadEffect(CompoundTag tag) {
        String identifier = tag.getString("Identifier");
        EffectProvider provider = getType().getEffects().get(identifier);
        if (provider == null) {
            Caldera.LOGGER.warn("Skipped loading unknown effect '%s' for brew '%s' in cauldron at '%s'".formatted(identifier, getType().getId(), getCauldron().getBlockPos()));
        } else {
            effects.put(identifier, provider.loadEffect(this, tag));
        }
    }
}
