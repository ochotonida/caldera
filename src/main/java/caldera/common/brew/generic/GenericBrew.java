package caldera.common.brew.generic;

import caldera.Caldera;
import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.EffectAction;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.init.ModTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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

    private final List<String> queuedUpdates = new ArrayList<>();

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
     * Does not notify clients.
     */
    public void startEffect(String identifier) {
        Effect effect = getType().getEffects().get(identifier).create(this);
        effects.put(identifier, effect);
        getCauldron().setChanged();
    }

    /**
     * Removes the effect with the specified identifier, does nothing if no such effect is active.
     * Does not notify clients.
     */
    public void removeEffect(String identifier) {
        effects.remove(identifier);
        getCauldron().setChanged();
    }

    /**
     * Removes the effect with the specified identifier, queues the action to be executed on tracking clients,
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
            sendActionExecuted(EffectAction.remove(identifier));
            ModTriggers.EFFECT_ENDED.get().trigger(this, identifier);
        }
    }

    /**
     * Queues an action to be executed on tracking clients,
     * Does nothing client-side.
     */
    public void sendActionExecuted(String identifier) {
        if (getCauldron().getLevel() != null && !getCauldron().getLevel().isClientSide()) {
            queuedUpdates.add(identifier);
        }
    }

    @Override
    public boolean hasUpdate() {
        return !queuedUpdates.isEmpty();
    }

    @Override
    public void clearUpdate() {
        queuedUpdates.clear();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String identifier : queuedUpdates) {
            list.add(StringTag.valueOf(identifier));
        }
        tag.put("actions", list);

        return tag;
    }

    @Override
    public void onUpdate(CompoundTag tag) {
        ListTag list = tag.getList("actions", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String identifier = list.getString(i);
            executeAction(identifier);
            if (getCauldron().isRemoved() || getCauldron().getBrew() != this) {
                Caldera.LOGGER.error("Brew in cauldron at %s was removed client-side while executing action %s".formatted(getCauldron().getBlockPos(), identifier));
                break;
            }
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
        effects.forEach((identifier, effect) -> {
            CompoundTag tag = new CompoundTag();
            effect.save(tag);
            tag.putString("Identifier", identifier);
            result.add(tag);
        });
        return result;
    }

    private void loadEffects(ListTag list) {
        effects.clear();
        for (Tag element : list) {
            CompoundTag tag = (CompoundTag) element;
            String identifier = tag.getString("Identifier");
            EffectProvider provider = getType().getEffects().get(identifier);
            if (provider == null) {
                Caldera.LOGGER.warn("Skipped loading unknown effect '%s' for brew '%s' in cauldron at '%s'".formatted(identifier, getType().getId(), getCauldron().getBlockPos()));
            } else {
                effects.put(identifier, provider.loadEffect(this, tag));
            }
        }
    }
}
