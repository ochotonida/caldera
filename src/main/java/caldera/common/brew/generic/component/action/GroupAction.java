package caldera.common.brew.generic.component.action;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;

import java.util.*;

public record GroupAction(List<String> actions) implements Action {

    @Override
    public void accept(GenericBrew brew) {
        Cauldron cauldron = brew.getCauldron();
        if (cauldron.getLevel() != null && cauldron.getLevel().isClientSide()) {
            return;
        }
        for (String identifier : actions()) {
            if (cauldron.isRemoved() || cauldron.getBrew() != brew) {
                break; // do not execute the remaining actions if a previous action removed the brew
            }
            brew.executeAction(identifier);
        }
    }

    @Override
    public JsonElement toJson() {
        return JsonHelper.toArray(actions);
    }

    public static GroupAction fromJson(String identifier, JsonArray array, Set<String> existingEffects, Set<String> existingActions) {
        List<String> actions = new ArrayList<>();
        for (JsonElement element : array) {
            String action = GsonHelper.convertToString(element, "array element");
            if (!existingActions.contains(action) && !EffectAction.isEffectAction(action, existingEffects)) {
                throw new JsonParseException("Undefined action in %s: %s".formatted(identifier, action));
            }
            actions.add(action);
        }
        return new GroupAction(actions);
    }

    public static void validateGroups(Map<String, GroupAction> groups) {
        Stack<String> visitedGroups = new Stack<>();
        for (String identifier : groups.keySet()) {
            validateGroup(groups, identifier, visitedGroups);
        }
    }

    private static void validateGroup(Map<String, GroupAction> groups, String identifier, Stack<String> visitedGroups) {
        if (visitedGroups.contains(identifier)) {
            visitedGroups.push(identifier);
            throw new JsonParseException("Cyclical group definition: " + visitedGroups);
        }

        for (String action : groups.get(identifier).actions()) {
            if (groups.containsKey(action)) {
                visitedGroups.push(identifier);
                validateGroup(groups, action, visitedGroups);
                visitedGroups.pop();
            }
        }
    }
}
