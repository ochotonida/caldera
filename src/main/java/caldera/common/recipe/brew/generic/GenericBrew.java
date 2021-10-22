package caldera.common.recipe.brew.generic;

import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.Brew;
import caldera.common.recipe.brew.BrewType;
import caldera.common.recipe.brew.generic.component.effect.Effect;
import caldera.common.recipe.brew.generic.component.trigger.Triggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class GenericBrew extends Brew {

    private final ColorInfo colorInfo = new ColorInfo();
    private final Map<String, Effect> effects = new HashMap<>();

    public GenericBrew(BrewType brewType, Cauldron cauldron) {
        super(brewType, cauldron);
    }

    @Override
    public GenericBrewType getType() {
        return (GenericBrewType) super.getType();
    }

    @Override
    public int getColor(float partialTicks) {
        return colorInfo.getColor(partialTicks);
    }

    @Override
    public void onBrewed() {
        Triggers.BREW_CREATED.trigger(this);
    }

    @Override
    public void tick() {
        if (getCauldron().getLevel() != null) {
            if (getCauldron().getLevel().isClientSide()) {
                colorInfo.tick();
            }
        }

        effects.values().forEach(Effect::tick);
    }

    public void startEffect(String identifier) {
        effects.put(identifier, getType().getEffects().get(identifier).create(this));
    }

    public void removeEffect(String identifier) {
        effects.remove(identifier);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("Color", colorInfo.getTargetColor());
        tag.put("ActiveEffects", saveEffects());
    }

    @Override
    public void load(CompoundTag tag) {
        colorInfo.start(tag.getInt("Color"));
        loadEffects(tag.getList("ActiveEffects", Tag.TAG_COMPOUND));
    }

    private Tag saveEffects() {
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
            Effect effect = getType().getEffects().get(identifier).loadEffect(this, tag);
            effects.put(identifier, effect);
        }
    }
}
