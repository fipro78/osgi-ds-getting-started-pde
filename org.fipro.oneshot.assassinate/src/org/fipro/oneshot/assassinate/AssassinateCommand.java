package org.fipro.oneshot.assassinate;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=assassinate"},
    service=AssassinateCommand.class
)
public class AssassinateCommand {

	@Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED)
    private OneShot hitman;

    public void assassinate(String target) {
        hitman.shoot(target);
    }
}