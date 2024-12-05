package org.fipro.oneshot.command;

import java.util.Hashtable;

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

	@Reference(target = "(&(component.factory=fipro.oneshot.factory)(organization=marine))")
    private ComponentFactory<OneShot> factory;

    public void shoot(String target) {
        // create a new service instance
    	Hashtable<String, Object> properties = new Hashtable<>();
//        properties.put("shooter.name", "Hitman Agent 47");
        ComponentInstance<OneShot> instance = this.factory.newInstance(properties); 
        OneShot shooter = instance.getInstance();
        try {
            shooter.shoot(target);
        } finally {
            // destroy the service instance
            instance.dispose();
        }
    }
}