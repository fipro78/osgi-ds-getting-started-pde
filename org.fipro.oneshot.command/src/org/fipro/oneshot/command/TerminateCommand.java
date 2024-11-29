package org.fipro.oneshot.command;

import org.fipro.oneshot.OneShot;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
    property= {
        "osgi.command.scope=fipro",
        "osgi.command.function=terminate"
    },
    service=TerminateCommand.class )
public class TerminateCommand {

    // get a factory for creating prototype scoped service instances
    @Reference(scope=ReferenceScope.PROTOTYPE_REQUIRED)
    private ComponentServiceObjects<OneShot> oneShotFactory;

    public void terminate(String target) {
        // create a new service instance OneShot 
    	OneShot oneShot = oneShotFactory.getService(); 
        try { 
            oneShot.shoot(target); 
        } 
        finally {
            // destroy the service instance 
            oneShotFactory.ungetService(oneShot); 
        } 
    } 
}