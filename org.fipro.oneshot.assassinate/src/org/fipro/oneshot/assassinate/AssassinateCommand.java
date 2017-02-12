package org.fipro.oneshot.assassinate;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=assassinate"},
    service=AssassinateCommand.class
)
public class AssassinateCommand {

    private OneShot hitman;

    @Reference
    void setOneShot(OneShot oneShot) {
        this.hitman = oneShot;
    }

    public void assassinate(String target) {
        hitman.shoot(target);
    }
}