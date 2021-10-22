package caldera.common.recipe.brew.generic.component.action;

import caldera.common.recipe.brew.generic.GenericBrew;
import caldera.common.recipe.brew.generic.component.GenericBrewTypeComponent;

import java.util.function.Consumer;

public interface Action extends Consumer<GenericBrew>, GenericBrewTypeComponent.Instance {

}
