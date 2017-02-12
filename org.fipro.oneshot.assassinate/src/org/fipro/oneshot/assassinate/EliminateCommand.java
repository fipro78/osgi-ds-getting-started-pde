package org.fipro.oneshot.assassinate;

import org.fipro.oneshot.OneShot;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=eliminate"},
    service=EliminateCommand.class
)
public class EliminateCommand {

    private ComponentContext context;
    private ServiceReference<OneShot> sr;

    @Activate
    void activate(ComponentContext context) {
        this.context = context;
    }

    @Reference(name="hitman")
    void setOneShotReference(ServiceReference<OneShot> sr) {
        this.sr = sr;
    }

    public void eliminate(String target) {
        OneShot hitman = (OneShot) this.context.locateService("hitman", sr);
        hitman.shoot(target);
    }
}