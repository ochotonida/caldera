package caldera.common.brew.generic.component.action;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.GenericBrewTypeComponent;

import java.util.function.Consumer;

public interface Action extends Consumer<GenericBrew>, GenericBrewTypeComponent.Instance {

}
