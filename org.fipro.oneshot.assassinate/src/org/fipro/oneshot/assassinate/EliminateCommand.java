package org.fipro.oneshot.assassinate;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=eliminate"},
    service=EliminateCommand.class,
    reference = @Reference(
    	name="hitman",
    	service=OneShot.class,
        scope=ReferenceScope.PROTOTYPE_REQUIRED)
)
public class EliminateCommand {

	@Activate
    private ComponentContext context;

    public void eliminate(String target) {
        OneShot hitman = (OneShot) this.context.locateService("hitman");
        hitman.shoot(target);
    }
}