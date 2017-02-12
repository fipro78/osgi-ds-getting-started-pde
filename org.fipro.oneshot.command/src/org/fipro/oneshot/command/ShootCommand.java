package org.fipro.oneshot.command;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=shoot"},
    service=ShootCommand.class
)
public class ShootCommand {

    private ComponentFactory factory;
    
    @Reference(target = "(component.factory=fipro.oneshot.factory)")
    void setComponentFactory(ComponentFactory factory) {
    	this.factory = factory;
    }

    public void shoot(String target) {
        // create a new service instance
        ComponentInstance instance = this.factory.newInstance(null);
        OneShot shooter = (OneShot) instance.getInstance();
        try {
            shooter.shoot(target);
        } finally {
            // destroy the service instance
            instance.dispose();
        }
    }
}