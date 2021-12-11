package caldera.common.brew.generic;

import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.trigger.Triggers;
import caldera.common.recipe.Cauldron;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
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

    public void changeColor(int newColor, int transitionTime) {
        colorInfo.changeColor(newColor, transitionTime);
        getCauldron().setChanged();
    }

    @Override
    public void onBrewed() {
        Triggers.BREW_CREATED.get().trigger(this);
    }

    @Override
    public void tick() {
        if (!colorInfo.hasSettled()) {
            getCauldron().setChanged();
        }
        colorInfo.tick();

        for (Effect effect : new ArrayList<>(effects.values())) {
            if (effects.containsValue(effect)) {
                effect.tick();
            }
        }
    }

    public void startEffect(String identifier) {
        effects.put(identifier, getType().getEffects().get(identifier).create(this, identifier));
        getCauldron().setChanged();
    }

    public void removeEffect(String identifier) {
        effects.remove(identifier);
        getCauldron().setChanged();
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
            Effect effect = getType().getEffects().get(identifier).loadEffect(this, tag, identifier);
            effects.put(identifier, effect);
        }
    }
}
